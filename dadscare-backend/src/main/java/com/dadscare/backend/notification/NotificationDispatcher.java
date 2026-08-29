package com.dadscare.backend.notification;

import com.dadscare.backend.alert.Alert;
import com.dadscare.backend.site.Site;
import com.dadscare.backend.user.Role;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import com.dadscare.backend.user.UserSiteAccess;
import com.dadscare.backend.user.UserSiteAccessRepository;
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
 * every org user whose <em>effective</em> role for the alert's site is {@code ORG_ADMIN}
 * or {@code SITE_MANAGER}: a user with no {@link UserSiteAccess} rows at all uses their
 * org-wide {@link User#getRole()} (and is a recipient for every site, as before); a user
 * who has been scoped to specific sites only counts for those sites, using the role
 * granted for that site — see {@link UserSiteAccess}'s own javadoc. Per-user channel
 * preferences are still a later phase (mobile app local-config settings).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final UserRepository userRepository;
    private final UserSiteAccessRepository userSiteAccessRepository;
    private final NotificationRepository notificationRepository;
    private final List<NotificationChannelSender> senders;
    private final AlertMessageTemplate alertMessageTemplate;

    @Transactional
    public void dispatch(Alert alert) {
        Map<NotificationChannel, NotificationChannelSender> sendersByChannel =
                senders.stream().collect(Collectors.toMap(NotificationChannelSender::channel, Function.identity()));

        String body = alertMessageTemplate.build(alert);

        Site alertSite = alert.getDevice().getShutterUnit() != null
                ? alert.getDevice().getShutterUnit().getSite()
                : null;

        List<User> recipients = userRepository.findAllByOrganizationId(alert.getOrganization().getId()).stream()
                .filter(u -> hasNotifiableRole(u, alertSite))
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

    /**
     * True if this user's effective role for {@code alertSite} is ORG_ADMIN or
     * SITE_MANAGER — see this class's own javadoc for the org-wide-vs-site-scoped rule.
     */
    private boolean hasNotifiableRole(User user, Site alertSite) {
        List<UserSiteAccess> siteAccess = userSiteAccessRepository.findAllByUserId(user.getId());
        if (siteAccess.isEmpty()) {
            return user.getRole() == Role.ORG_ADMIN || user.getRole() == Role.SITE_MANAGER;
        }
        if (alertSite == null) {
            // Site-scoped user, but this alert's device isn't assigned to a site — nothing
            // to match against, so a scoped user is never a recipient (an org-wide user
            // with no UserSiteAccess rows still would be, per the branch above).
            return false;
        }
        return siteAccess.stream()
                .filter(access -> access.getSite().getId().equals(alertSite.getId()))
                .anyMatch(access -> access.getRole() == Role.ORG_ADMIN || access.getRole() == Role.SITE_MANAGER);
    }

    private boolean hasPhone(User user) {
        return user.getPhone() != null && !user.getPhone().isBlank();
    }

    private boolean hasPushToken(User user) {
        return user.getPushToken() != null && !user.getPushToken().isBlank();
    }
}
