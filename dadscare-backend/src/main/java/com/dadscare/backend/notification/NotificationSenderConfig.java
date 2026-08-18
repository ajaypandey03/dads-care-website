package com.dadscare.backend.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers a {@link LoggingNotificationSender} for every channel — see its javadoc. */
@Configuration
public class NotificationSenderConfig {

    @Bean
    NotificationChannelSender pushSender() {
        return new LoggingNotificationSender(NotificationChannel.PUSH);
    }

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
