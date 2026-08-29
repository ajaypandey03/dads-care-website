package com.dadscare.backend.notification;

import com.dadscare.backend.alert.Alert;
import com.dadscare.backend.alert.EventDirection;
import com.dadscare.backend.forms.GodownForm;
import com.dadscare.backend.forms.GodownFormRepository;
import com.dadscare.backend.forms.GodownFormService;
import com.dadscare.backend.forms.StockLine;
import com.dadscare.backend.forms.TruckEntry;
import com.dadscare.backend.telemetry.RawEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds the tabulated WhatsApp-style alert message — format taken from the Godown
 * Operational Workflow page in Confluence (Dad's Care's own original template, kept
 * as-is). Every message carries the alert's sequential reference code, per the
 * "Sequential WhatsApp reference codes" requirement on the Dad's Care Platform Design
 * page. CONFIRMED alerts additionally carry the inventory/truck/labor context from the
 * submitted {@link GodownForm}, when one was submitted — this is the concrete
 * "operator, inventory, and truck context attached" promise from the Lock vs Shutter page.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertMessageTemplate {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Kolkata"));

    private final GodownFormRepository godownFormRepository;
    private final GodownFormService godownFormService;
    private final ObjectMapper objectMapper;

    public String build(Alert alert) {
        RawEvent event = alert.getRawEvent();
        String statusWord = switch (alert.getDirection()) {
            case OPEN -> "OPEN";
            case CLOSE -> "CLOSED";
            case ALARM -> "ALARM";
        };
        String godownLabel = event.getDevice().getShutterUnit() != null
                ? event.getDevice().getShutterUnit().getSite().getGodownCode()
                : event.getDevice().getVelosyssDeviceRef();

        StringBuilder sb = new StringBuilder();
        sb.append("STATUS: ").append(statusWord);
        sb.append(" | GODOWN: ").append(godownLabel);
        sb.append(" | TIME: ").append(TIME_FORMAT.format(event.getEventTimestamp()));
        if (alert.getUnlockRequest() != null && alert.getUnlockRequest().getRequestedBy() != null) {
            sb.append(" | USER: ").append(alert.getUnlockRequest().getRequestedBy().getName());
        }
        sb.append("\n");
        sb.append(classificationHeadline(alert));

        if (alert.getDirection() == EventDirection.ALARM) {
            sb.append("\nALARM: ")
                    .append(event.getAlarmCode())
                    .append(event.getAlarmDescription() != null ? " — " + event.getAlarmDescription() : "");
        }

        appendFormSections(sb, alert);

        sb.append("\nRef: ").append(alert.getSequenceCode());
        return sb.toString();
    }

    private void appendFormSections(StringBuilder sb, Alert alert) {
        if (alert.getUnlockRequest() == null) {
            return;
        }
        GodownForm form = godownFormRepository
                .findByUnlockRequestId(alert.getUnlockRequest().getId())
                .orElse(null);
        if (form == null) {
            return;
        }

        List<StockLine> stockLines = godownFormService.stockLinesFor(form.getId());
        if (!stockLines.isEmpty()) {
            sb.append("\nINVENTORY:");
            for (StockLine line : stockLines) {
                sb.append("\n| ")
                        .append(line.getProduct().getName())
                        .append(" | ")
                        .append(line.getQuantity())
                        .append(" | ")
                        .append(line.getUnit())
                        .append(" |");
            }
        }

        List<TruckEntry> truckEntries = godownFormService.truckEntriesFor(form.getId());
        if (!truckEntries.isEmpty()) {
            sb.append("\nLOGISTICS (Trucks):");
            for (TruckEntry entry : truckEntries) {
                sb.append("\n| ")
                        .append(entry.getVehicleNo())
                        .append(" | ")
                        .append(entry.getTransporter().getName())
                        .append(" | ")
                        .append(entry.getProduct().getName())
                        .append(" | ")
                        .append(entry.getQuantity())
                        .append(" | ")
                        .append(entry.getWaitingSince() == null ? "-" : TIME_FORMAT.format(entry.getWaitingSince()))
                        .append(" |");
            }
        }

        if (form.getLaborCount() != null || (form.getRemarks() != null && !form.getRemarks().isBlank())) {
            sb.append("\nLABOR: ").append(form.getLaborCount() == null ? "-" : form.getLaborCount());
            sb.append(" | REMARKS: ").append(form.getRemarks() == null ? "-" : form.getRemarks());
        }

        appendCustomFields(sb, form);
    }

    private void appendCustomFields(StringBuilder sb, GodownForm form) {
        if (form.getCustomFieldsJson() == null) {
            return;
        }
        try {
            List<Map<String, String>> fields =
                    objectMapper.readValue(form.getCustomFieldsJson(), new TypeReference<>() {});
            for (Map<String, String> field : fields) {
                sb.append("\nCUSTOM: ").append(field.get("heading")).append(": ").append(field.get("value"));
            }
        } catch (Exception e) {
            log.warn("Failed to render custom fields for godownFormId={}: {}", form.getId(), e.getMessage());
        }
    }

    private String classificationHeadline(Alert alert) {
        if (alert.getDirection() == EventDirection.ALARM) {
            return "🚨 Device alarm — verify immediately.";
        }
        return switch (alert.getClassification()) {
            case CONFIRMED -> "Confirmed — operated via the Dad's Care app.";
            case UNEXPLAINED_HIGH -> "⚠️ Unexplained lock access — not initiated through the app. Please verify.";
            case UNEXPLAINED_VERIFY -> "Lock access detected (low confidence) — please verify if convenient.";
            case SUPPRESSED -> ""; // never actually sent — SUPPRESSED alerts are log-only
        };
    }
}
