package com.dadscare.backend.notification;

/**
 * One implementation per {@link NotificationChannel} (push/SMS/email/WhatsApp), so a real
 * provider integration for one channel can be swapped in without touching
 * {@link NotificationDispatcher} or the others. See {@link LoggingNotificationSender} —
 * the current default for every channel, since the actual provider (WhatsApp Business
 * API vendor, SMS gateway, push service) is still an open decision (see Open Decisions
 * in Confluence).
 */
public interface NotificationChannelSender {

    NotificationChannel channel();

    /** @return true if accepted for delivery by the provider; false leaves the Notification row FAILED. */
    boolean send(Notification notification);
}
