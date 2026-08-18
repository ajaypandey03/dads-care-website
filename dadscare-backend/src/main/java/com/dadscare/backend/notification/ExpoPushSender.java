package com.dadscare.backend.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Real sender for {@link NotificationChannel#PUSH} — Expo's push API needs no account
 * setup or paid credentials (unlike the WhatsApp/SMS/email providers, which are still an
 * Open Decision), so unlike {@link LoggingNotificationSender} this one actually delivers.
 * {@code notification.getRecipient()} is expected to be an Expo push token (e.g.
 * {@code "ExponentPushToken[xxxxxxxx]"}), registered by dadscare-mobile via
 * {@code PUT /api/v1/me/push-token}.
 */
@Slf4j
@Component
public class ExpoPushSender implements NotificationChannelSender {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestClient restClient = RestClient.builder().baseUrl(EXPO_PUSH_URL).build();

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean send(Notification notification) {
        try {
            var body = new ExpoPushMessage(
                    notification.getRecipient(), "Dad's Care Alert", notification.getBody(), "default");
            var response = restClient
                    .post()
                    .body(body)
                    .retrieve()
                    .toEntity(ExpoPushResponse.class)
                    .getBody();

            boolean ok = response != null
                    && response.data() != null
                    && response.data().length > 0
                    && "ok".equals(response.data()[0].status());
            if (!ok) {
                log.warn("Expo push rejected for token {}: {}", notification.getRecipient(), response);
            }
            return ok;
        } catch (RestClientException e) {
            log.warn("Expo push send failed for token {}: {}", notification.getRecipient(), e.getMessage());
            return false;
        }
    }

    private record ExpoPushMessage(String to, String title, String body, String sound) {
    }

    private record ExpoPushResponse(ExpoPushTicket[] data) {
    }

    private record ExpoPushTicket(String status, String id, String message) {
    }
}
