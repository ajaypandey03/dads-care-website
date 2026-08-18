package com.dadscare.backend.alert;

import com.dadscare.backend.notification.NotificationDispatcher;
import com.dadscare.backend.sequence.SequenceCounterService;
import com.dadscare.backend.telemetry.RawEvent;
import com.dadscare.backend.telemetry.RawEventRepository;
import com.dadscare.backend.unlock.CommandType;
import com.dadscare.backend.unlock.UnlockRequest;
import com.dadscare.backend.unlock.UnlockRequestRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classifies every {@code LOCK_OPEN}/{@code LOCK_CLOSE} {@link RawEvent} into an
 * {@link Alert}, implementing the Authorized-Open Correlation fix — see "Lock vs Shutter"
 * in Confluence. Called by {@link com.dadscare.backend.telemetry.WebhookService} once a
 * whole batch has been persisted (not per-event mid-batch), so the quick-reclose check
 * below can see a LOCK_CLOSE that arrived just after its LOCK_OPEN in the same push.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RulesEngineService {

    /** Correlation window around an UnlockRequest's relayedAt — covers command/propagation latency. */
    private static final Duration CORRELATION_WINDOW = Duration.ofMinutes(2);

    /** Points awarded simply for a LOCK_OPEN/LOCK_CLOSE event having occurred at all — see weight table in Confluence. */
    private static final int BASE_SCORE = 40;

    private final UnlockRequestRepository unlockRequestRepository;
    private final RawEventRepository rawEventRepository;
    private final DeviceCalibrationRepository calibrationRepository;
    private final AlertRepository alertRepository;
    private final SequenceCounterService sequenceCounterService;
    private final NotificationDispatcher notificationDispatcher;

    @Transactional
    public Alert evaluate(RawEvent event) {
        if (alertRepository.existsByRawEventId(event.getId())) {
            // Defensive — should be unreachable given RawEvent's own eventId dedup upstream,
            // but an Alert is 1:1 with a RawEvent by DB constraint, so never double-create.
            log.debug("Alert already exists for rawEventId={}, skipping", event.getId());
            return null;
        }

        EventDirection direction = directionOf(event);
        CommandType matchingCommand = direction == EventDirection.OPEN ? CommandType.UNLOCK : CommandType.LOCK;

        UnlockRequest match = findMatch(event, matchingCommand);

        Alert alert = new Alert();
        alert.setOrganization(event.getOrganization());
        alert.setDevice(event.getDevice());
        alert.setRawEvent(event);
        alert.setDirection(direction);

        if (match != null) {
            alert.setClassification(AlertClassification.CONFIRMED);
            alert.setUnlockRequest(match);
        } else {
            DeviceCalibration calibration = calibrationRepository
                    .findByDeviceId(event.getDevice().getId())
                    .orElseGet(() -> defaultCalibration(event));
            int score = score(event, calibration);
            alert.setConfidenceScore(score);
            alert.setClassification(classify(score, calibration));
        }

        if (alert.getClassification() != AlertClassification.SUPPRESSED) {
            alert.setSequenceCode(sequenceCounterService.nextAlertReferenceCode(
                    event.getOrganization().getId(), event.getOrganization().getCodePrefix()));
        }

        alertRepository.save(alert);

        if (alert.getClassification() != AlertClassification.SUPPRESSED) {
            notificationDispatcher.dispatch(alert);
        } else {
            log.info(
                    "Alert suppressed (log-only): device={}, rawEventId={}, score={}",
                    event.getDevice().getVelosyssDeviceRef(),
                    event.getId(),
                    alert.getConfidenceScore());
        }

        return alert;
    }

    private UnlockRequest findMatch(RawEvent event, CommandType commandType) {
        List<UnlockRequest> candidates = unlockRequestRepository.findCorrelationCandidates(
                event.getDevice().getId(),
                commandType,
                event.getEventTimestamp().minus(CORRELATION_WINDOW),
                event.getEventTimestamp().plus(CORRELATION_WINDOW));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    /**
     * Secondary heuristic — see the weight table on "Lock vs Shutter" in Confluence.
     * Only meaningful for OPEN events (a LOCK_CLOSE has no "was it left open a while"
     * signal of its own); CLOSE events without a match score on tamper/motion alone.
     */
    private int score(RawEvent event, DeviceCalibration calibration) {
        int score = BASE_SCORE;

        boolean tamperOrMotion = Boolean.TRUE.equals(event.getTamperFlag())
                || (event.getMotionMagnitude() != null && event.getMotionMagnitude() > 0.3);
        if (tamperOrMotion) {
            score += calibration.getTamperMotionWeight();
        }

        if (!quickReclose(event, calibration.getQuickRecloseWindowSeconds())) {
            score += calibration.getDurationWeight();
        }

        return score;
    }

    /**
     * True if a LOCK_CLOSE for the same device landed within the configured window right
     * after this event — the "just fiddled with it and closed it again" case the plan
     * calls out as a weak/negative signal. Only ever checked for OPEN events.
     */
    private boolean quickReclose(RawEvent event, int windowSeconds) {
        if (directionOf(event) != EventDirection.OPEN) {
            return false;
        }
        Instant windowEnd = event.getEventTimestamp().plusSeconds(windowSeconds);
        return !rawEventRepository
                .findAllByDeviceIdAndEventTypeAndEventTimestampBetween(
                        event.getDevice().getId(),
                        RawEvent.EventType.LOCK_CLOSE,
                        event.getEventTimestamp(),
                        windowEnd)
                .isEmpty();
    }

    private AlertClassification classify(int score, DeviceCalibration calibration) {
        if (score >= calibration.getEscalateThreshold()) {
            return AlertClassification.UNEXPLAINED_HIGH;
        }
        if (score >= calibration.getVerifyThreshold()) {
            return AlertClassification.UNEXPLAINED_VERIFY;
        }
        return AlertClassification.SUPPRESSED;
    }

    private EventDirection directionOf(RawEvent event) {
        return event.getEventType() == RawEvent.EventType.LOCK_OPEN ? EventDirection.OPEN : EventDirection.CLOSE;
    }

    /** Transient, unsaved defaults — used when a device has no DeviceCalibration row yet. */
    private DeviceCalibration defaultCalibration(RawEvent event) {
        DeviceCalibration defaults = new DeviceCalibration();
        defaults.setDevice(event.getDevice());
        return defaults;
    }
}
