package com.dadscare.backend.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dadscare.backend.notification.NotificationDispatcher;
import com.dadscare.backend.sequence.SequenceCounterService;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.telemetry.RawEvent;
import com.dadscare.backend.telemetry.RawEventRepository;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.unlock.CommandType;
import com.dadscare.backend.unlock.UnlockRequest;
import com.dadscare.backend.unlock.UnlockRequestRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RulesEngineServiceTest {

    @Mock
    private UnlockRequestRepository unlockRequestRepository;

    @Mock
    private RawEventRepository rawEventRepository;

    @Mock
    private DeviceCalibrationRepository calibrationRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private SequenceCounterService sequenceCounterService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    private RulesEngineService rulesEngineService;
    private Organization organization;
    private Device device;

    @BeforeEach
    void setUp() {
        rulesEngineService = new RulesEngineService(
                unlockRequestRepository,
                rawEventRepository,
                calibrationRepository,
                alertRepository,
                sequenceCounterService,
                notificationDispatcher);

        organization = new Organization();
        organization.setId(1L);
        organization.setCodePrefix("DC");

        device = new Device();
        device.setId(10L);
        device.setOrganization(organization);
        device.setVelosyssDeviceRef("VLS-DL-0001");
    }

    private RawEvent openEvent() {
        RawEvent event = new RawEvent();
        event.setId(100L);
        event.setOrganization(organization);
        event.setDevice(device);
        event.setEventType(RawEvent.EventType.LOCK_OPEN);
        event.setEventTimestamp(Instant.parse("2026-08-19T10:00:00Z"));
        return event;
    }

    @Test
    void matchingUnlockRequestProducesAConfirmedAlertWithNoScoring() {
        RawEvent event = openEvent();
        UnlockRequest match = new UnlockRequest();
        match.setId(500L);
        when(unlockRequestRepository.findCorrelationCandidates(
                        eq(10L), eq(CommandType.UNLOCK), any(), any()))
                .thenReturn(List.of(match));
        when(sequenceCounterService.nextAlertReferenceCode(1L, "DC")).thenReturn("DC-000001");

        Alert alert = rulesEngineService.evaluate(event);

        assertThat(alert.getClassification()).isEqualTo(AlertClassification.CONFIRMED);
        assertThat(alert.getUnlockRequest()).isSameAs(match);
        assertThat(alert.getConfidenceScore()).isNull();
        assertThat(alert.getSequenceCode()).isEqualTo("DC-000001");
        verify(notificationDispatcher).dispatch(alert);
    }

    @Test
    void noMatchWithTamperAndNoQuickRecloseEscalatesToUnexplainedHigh() {
        RawEvent event = openEvent();
        event.setTamperFlag(true);
        noCorrelationMatch();
        noCalibrationOverride();
        noQuickReclose();
        when(sequenceCounterService.nextAlertReferenceCode(1L, "DC")).thenReturn("DC-000002");

        Alert alert = rulesEngineService.evaluate(event);

        // base(40) + tamper(35) + no-quick-reclose(25) = 100
        assertThat(alert.getConfidenceScore()).isEqualTo(100);
        assertThat(alert.getClassification()).isEqualTo(AlertClassification.UNEXPLAINED_HIGH);
        assertThat(alert.getUnlockRequest()).isNull();
        verify(notificationDispatcher).dispatch(alert);
    }

    @Test
    void noMatchWithNoTamperAndAQuickRecloseOnlyReachesVerifyTier() {
        RawEvent event = openEvent();
        event.setTamperFlag(false);
        noCorrelationMatch();
        noCalibrationOverride();
        withQuickReclose();
        when(sequenceCounterService.nextAlertReferenceCode(1L, "DC")).thenReturn("DC-000003");

        Alert alert = rulesEngineService.evaluate(event);

        // base(40) only — no tamper/motion, and the reclose suppresses the duration bonus
        assertThat(alert.getConfidenceScore()).isEqualTo(40);
        assertThat(alert.getClassification()).isEqualTo(AlertClassification.UNEXPLAINED_VERIFY);
        verify(notificationDispatcher).dispatch(alert);
    }

    @Test
    void suppressedAlertsAreNeverAssignedASequenceCodeOrDispatched() {
        RawEvent event = openEvent();
        event.setTamperFlag(false);
        noCorrelationMatch();
        withQuickReclose();

        // A calibration override raising verifyThreshold above the bare base score (40)
        // demonstrates SUPPRESSED is reachable — with default weights the base score
        // alone always clears the default verify threshold (also 40), which is expected:
        // "a lock-open event happened at all" is never literally worthless information.
        DeviceCalibration strict = new DeviceCalibration();
        strict.setDevice(device);
        strict.setVerifyThreshold(50);
        when(calibrationRepository.findByDeviceId(10L)).thenReturn(Optional.of(strict));

        Alert alert = rulesEngineService.evaluate(event);

        assertThat(alert.getClassification()).isEqualTo(AlertClassification.SUPPRESSED);
        assertThat(alert.getSequenceCode()).isNull();
        verify(sequenceCounterService, never()).nextAlertReferenceCode(anyLong(), any());
        verify(notificationDispatcher, never()).dispatch(any());
    }

    @Test
    void alreadyAlertedEventsAreNotProcessedTwice() {
        RawEvent event = openEvent();
        when(alertRepository.existsByRawEventId(100L)).thenReturn(true);

        Alert alert = rulesEngineService.evaluate(event);

        assertThat(alert).isNull();
        verify(unlockRequestRepository, never()).findCorrelationCandidates(any(), any(), any(), any());
    }

    private void noCorrelationMatch() {
        when(unlockRequestRepository.findCorrelationCandidates(any(), any(), any(), any()))
                .thenReturn(List.of());
    }

    private void noCalibrationOverride() {
        when(calibrationRepository.findByDeviceId(10L)).thenReturn(Optional.empty());
    }

    private void noQuickReclose() {
        when(rawEventRepository.findAllByDeviceIdAndEventTypeAndEventTimestampBetween(
                        eq(10L), eq(RawEvent.EventType.LOCK_CLOSE), any(), any()))
                .thenReturn(List.of());
    }

    private void withQuickReclose() {
        RawEvent closeEvent = new RawEvent();
        closeEvent.setId(101L);
        when(rawEventRepository.findAllByDeviceIdAndEventTypeAndEventTimestampBetween(
                        eq(10L), eq(RawEvent.EventType.LOCK_CLOSE), any(), any()))
                .thenReturn(List.of(closeEvent));
    }
}
