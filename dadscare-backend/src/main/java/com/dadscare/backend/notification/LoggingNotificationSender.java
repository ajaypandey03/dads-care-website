package com.dadscare.backend.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Stub sender: logs what would have been sent and reports success, so the rest of the
 * pipeline (Notification rows, statuses, retries) is fully exercised without a real
 * provider integration. One instance per channel — see {@link NotificationSenderConfig}.
 * Replace with a real provider client per channel once one is chosen (Open Decisions in
 * Confluence: WhatsApp Business API vendor, SMS gateway, push service).
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingNotificationSender implements NotificationChannelSender {

    private final NotificationChannel channel;

    @Override
    public NotificationChannel channel() {
        return channel;
    }

    @Override
    public boolean send(Notification notification) {
        log.info(
                "[STUB {} SEND] to={} body=\n{}",
                channel,
                notification.getRecipient(),
                notification.getBody());
        return true;
    }
}
