package com.dadscare.backend.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a {@link LoggingNotificationSender} for every channel that still has no real
 * provider integration. PUSH ({@link ExpoPushSender}) and WHATSAPP ({@link
 * RmlConnectWhatsAppSender}) are real and registered as their own {@code @Component}s
 * instead, since they need no stub.
 */
@Configuration
public class NotificationSenderConfig {

    @Bean
    NotificationChannelSender smsSender() {
        return new LoggingNotificationSender(NotificationChannel.SMS);
    }

    @Bean
    NotificationChannelSender emailSender() {
        return new LoggingNotificationSender(NotificationChannel.EMAIL);
    }
}
