package com.dadscare.backend.notification;

import com.dadscare.backend.alert.Alert;
import com.dadscare.backend.user.Role;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends a notification-worthy {@link Alert} (anything except SUPPRESSED — see
 * RulesEngineService) to every recipient, on every channel they've registered for:
 * WhatsApp if they have a phone number on file, push if they've registered an Expo push
 * token from dadscare-mobile (see {@code PUT /api/v1/me/push-token}). Recipients are
 * every {@code ORG_ADMIN}/{@code SITE_MANAGER} in the alert's org. Per-site recipient
 * lists and per-user channel preferences are a later phase (they belong to the mobile
 * app's local-config settings) — this is deliberately the simplest policy that's still
 * useful.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final List<NotificationChannelSender> senders;

    @Transactional
    public void dispatch(Alert alert) {
        Map<NotificationChannel, NotificationChannelSender> sendersByChannel =
                senders.stream().collect(Collectors.toMap(NotificationChannelSender::channel, Function.identity()));

        String body = AlertMessageTemplate.build(alert);

        List<User> recipients = userRepository.findAllByOrganizationId(alert.getOrganization().getId()).stream()
                .filter(u -> u.getRole() == Role.ORG_ADMIN || u.getRole() == Role.SITE_MANAGER)
                .filter(u -> hasPhone(u) || hasPushToken(u))
                .toList();

        if (recipients.isEmpty()) {
            log.warn(
                    "Alert {} has notification-worthy classification {} but no recipients with a phone number "
                            + "or push token on file for org {}",
                    alert.getId(),
                    alert.getClassification(),
                    alert.getOrganization().getId());
            return;
        }

        for (User recipient : recipients) {
            if (hasPhone(recipient)) {
                send(alert, body, NotificationChannel.WHATSAPP, recipient.getPhone(), sendersByChannel);
            }
            if (hasPushToken(recipient)) {
                send(alert, body, NotificationChannel.PUSH, recipient.getPushToken(), sendersByChannel);
            }
        }
    }

    private void send(
            Alert alert,
            String body,
            NotificationChannel channel,
            String recipientAddress,
            Map<NotificationChannel, NotificationChannelSender> sendersByChannel) {
        Notification notification = new Notification();
        notification.setOrganization(alert.getOrganization());
        notification.setAlert(alert);
        notification.setChannel(channel);
        notification.setRecipient(recipientAddress);
        notification.setBody(body);
        notificationRepository.save(notification);

        NotificationChannelSender sender = sendersByChannel.get(channel);
        boolean sent = sender != null && sender.send(notification);
        if (sent) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason("sender_unavailable_or_rejected");
        }
    }

    private boolean hasPhone(User user) {
        return user.getPhone() != null && !user.getPhone().isBlank();
    }

    private boolean hasPushToken(User user) {
        return user.getPushToken() != null && !user.getPushToken().isBlank();
    }
}
