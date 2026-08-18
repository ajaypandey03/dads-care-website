package com.dadscare.backend.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a {@link LoggingNotificationSender} for every channel except PUSH, which has
 * a real implementation ({@link ExpoPushSender}, registered as its own {@code @Component}
 * since it needs no stub — see its javadoc).
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

    @Bean
    NotificationChannelSender whatsappSender() {
        return new LoggingNotificationSender(NotificationChannel.WHATSAPP);
    }
}
