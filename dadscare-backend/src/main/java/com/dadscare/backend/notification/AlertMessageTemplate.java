package com.dadscare.backend.notification;

import com.dadscare.backend.alert.Alert;
import com.dadscare.backend.telemetry.RawEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Builds the tabulated WhatsApp-style alert message — format taken from the Godown
 * Operational Workflow page in Confluence (Dad's Care's own original template, kept
 * as-is). Every message carries the alert's sequential reference code, per the
 * "Sequential WhatsApp reference codes" requirement on the Dad's Care Platform Design
 * page.
 */
public final class AlertMessageTemplate {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Kolkata"));

    private AlertMessageTemplate() {
    }

    public static String build(Alert alert) {
        RawEvent event = alert.getRawEvent();
        String statusWord = switch (alert.getDirection()) {
            case OPEN -> "OPEN";
            case CLOSE -> "CLOSED";
        };
        String godownLabel = event.getDevice().getShutterUnit() != null
                ? event.getDevice().getShutterUnit().getSite().getGodownCode()
                : event.getDevice().getVelosyssDeviceRef();

        StringBuilder sb = new StringBuilder();
        sb.append("STATUS: ").append(statusWord);
        sb.append(" | GODOWN: ").append(godownLabel);
        sb.append(" | TIME: ").append(TIME_FORMAT.format(event.getEventTimestamp()));
        sb.append("\n");
        sb.append(classificationHeadline(alert));
        sb.append("\nRef: ").append(alert.getSequenceCode());
        return sb.toString();
    }

    private static String classificationHeadline(Alert alert) {
        return switch (alert.getClassification()) {
            case CONFIRMED -> "Confirmed — operated via the Dad's Care app.";
            case UNEXPLAINED_HIGH -> "⚠️ Unexplained lock access — not initiated through the app. Please verify.";
            case UNEXPLAINED_VERIFY -> "Lock access detected (low confidence) — please verify if convenient.";
            case SUPPRESSED -> ""; // never actually sent — SUPPRESSED alerts are log-only
        };
    }
}
