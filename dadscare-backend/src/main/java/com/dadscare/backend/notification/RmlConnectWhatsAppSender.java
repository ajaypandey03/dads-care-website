package com.dadscare.backend.notification;

import com.dadscare.backend.alert.Alert;
import com.dadscare.backend.alert.AlertClassification;
import com.dadscare.backend.alert.EventDirection;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.Site;
import com.dadscare.backend.telemetry.RawEvent;
import com.dadscare.backend.tenant.Organization;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Real {@link NotificationChannel#WHATSAPP} sender via RMLConnect (Route Mobile) — see
 * {@code https://developer.rmlconnect.net/route-mobile-project/docs/send-template-and-session-messages-api}.
 * Replaces the {@code whatsappSender()} stub bean that used to live in
 * {@link NotificationSenderConfig} (same pattern {@link ExpoPushSender} used for PUSH).
 *
 * <p>WhatsApp's Business API requires a pre-approved template for any business-initiated
 * message (i.e. every alert — these are never a reply inside a customer's own 24-hour
 * session), so unlike every other channel here this one does <em>not</em> send {@link
 * Notification#getBody()}'s free-text — it builds the template's 10 body variables
 * straight from the {@link Alert}. The mapping below matches the "template_with_cfa"
 * approved template's variable order exactly: Salutation, Greeting/Org name, Warehouse
 * Incharge, Location, District, Alert, Time, Any Exceptions, eLockID, CFA name. (The
 * footer "Tech Support-DAD'S CARE Logistics Solutions Pvt Ltd." is static text in the
 * approved template, not a variable.) Dad's Care's data model has no explicit "district",
 * "incharge" or "CFA name" field, so those are best-effort — adjust {@link
 * #buildBodyParams} once real field names/expectations are confirmed. "Any Exceptions" is
 * derived from {@link Alert#getClassification()}: CONFIRMED (operated via the app) reports
 * "No", the UNEXPLAINED_* classifications report "Yes". Salutation and CFA name are
 * per-organization settings ({@link com.dadscare.backend.tenant.Organization#getWhatsappSalutation()}
 * / {@code #getWhatsappCfaName()}), editable by Dad's Care platform admins — WhatsApp
 * rejects a blank/whitespace variable value, so CFA name can't be sent truly empty.
 */
@Slf4j
@Component
public class RmlConnectWhatsAppSender implements NotificationChannelSender {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a").withZone(ZoneId.of("Asia/Kolkata"));

    private final RestClient restClient;
    private final RmlConnectAuthClient authClient;
    private final String templateName;
    private final String templateLangCode;
    private final boolean configured;

    public RmlConnectWhatsAppSender(
            RestClient.Builder restClientBuilder,
            RmlConnectAuthClient authClient,
            @Value("${app.whatsapp.rmlconnect.base-url:https://apis.rmlconnect.net}") String baseUrl,
            @Value("${app.whatsapp.rmlconnect.template-name:}") String templateName,
            @Value("${app.whatsapp.rmlconnect.template-lang:en}") String templateLangCode) {
        this.authClient = authClient;
        this.templateName = templateName;
        this.templateLangCode = templateLangCode;
        this.configured = authClient.isConfigured() && !templateName.isBlank();
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public boolean send(Notification notification) {
        if (!configured) {
            log.warn(
                    "RMLConnect not configured (username/password/template-name) — cannot WhatsApp-send to {}",
                    notification.getRecipient());
            return false;
        }
        String jwt = authClient.token();
        if (jwt == null) {
            return false;
        }

        Alert alert = notification.getAlert();
        var body = new SendMessageRequest(
                notification.getRecipient(),
                notification.getId().toString(),
                // "media_template" — confirmed against RMLConnect's real API (not just the
                // docs) via a manual Postman test: "text_template" was rejected, this wasn't.
                new MediaTemplate("media_template", templateName, templateLangCode, buildBodyParams(alert)));

        try {
            restClient
                    .post()
                    .uri("/wba/v1/messages")
                    .header("Authorization", jwt)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            log.warn(
                    "RMLConnect rejected WhatsApp send to {}: HTTP {} — {}",
                    notification.getRecipient(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            log.warn("RMLConnect WhatsApp send failed for {}: {}", notification.getRecipient(), e.getMessage());
            return false;
        }
    }

    /** Order matches the approved template's {{1}}..{{10}} exactly — see this class's own javadoc. */
    private List<TextParam> buildBodyParams(Alert alert) {
        RawEvent event = alert.getRawEvent();
        Device device = alert.getDevice();
        Site site = device.getShutterUnit() != null ? device.getShutterUnit().getSite() : null;

        Organization organization = alert.getOrganization();
        String salutation = organization != null ? organization.getWhatsappSalutation() : "Customer";
        String greetingFrom = organization != null ? organization.getName() : "Dad's Care";
        String incharge = alert.getUnlockRequest() != null && alert.getUnlockRequest().getRequestedBy() != null
                ? alert.getUnlockRequest().getRequestedBy().getName()
                : "-";
        String location = site != null ? "Cement Godown - " + site.getName() : "-";
        String district = site != null && site.getAddress() != null
                ? site.getAddress() + ", Madhya Pradesh, India"
                : "-";
        String alertText = alert.getDirection() == EventDirection.ALARM
                ? "Alarm: " + event.getAlarmCode()
                : "Lock status " + (alert.getDirection() == EventDirection.OPEN ? "Open" : "Closed");
        String time = TIME_FORMAT.format(event.getEventTimestamp());
        String anyExceptions = alert.getClassification() == AlertClassification.CONFIRMED ? "No" : "Yes";
        String eLockId = device.getVelosyssTerminalId() != null
                ? device.getVelosyssTerminalId()
                : device.getVelosyssDeviceRef();
        String cfaName = organization != null ? organization.getWhatsappCfaName() : "-";

        return List.of(
                new TextParam(salutation),
                new TextParam(greetingFrom),
                new TextParam(incharge),
                new TextParam(location),
                new TextParam(district),
                new TextParam(alertText),
                new TextParam(time),
                new TextParam(anyExceptions),
                new TextParam(eLockId),
                new TextParam(cfaName));
    }

    private record TextParam(String text) {}

    private record MediaTemplate(String type, String template_name, String lang_code, List<TextParam> body) {}

    private record SendMessageRequest(String phone, String extra, MediaTemplate media) {}
}
