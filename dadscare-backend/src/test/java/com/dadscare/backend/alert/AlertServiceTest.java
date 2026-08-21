package com.dadscare.backend.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.Site;
import com.dadscare.backend.site.ShutterUnit;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private FeedbackEntryRepository feedbackEntryRepository;

    @Mock
    private UserRepository userRepository;

    private AlertService alertService;
    private Organization organization;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, feedbackEntryRepository, userRepository);
        organization = new Organization();
        organization.setId(7L);
        TenantContext.set(7L, 1L, "ORG_ADMIN", false);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Alert alertWithDeviceAndSite() {
        Site site = new Site();
        site.setId(2L);
        site.setName("Indore Godown");

        ShutterUnit unit = new ShutterUnit();
        unit.setSite(site);

        Device device = new Device();
        device.setId(9L);
        device.setVelosyssDeviceRef("VLS-DL-0001");
        device.setShutterUnit(unit);

        Alert alert = new Alert();
        alert.setId(1L);
        alert.setOrganization(organization);
        alert.setDevice(device);
        alert.setDirection(EventDirection.OPEN);
        alert.setClassification(AlertClassification.CONFIRMED);
        alert.setSequenceCode("DC-000001");
        alert.setCreatedAt(Instant.parse("2026-08-21T10:00:00Z"));
        return alert;
    }

    @Test
    void includesDeviceRefAndGodownOnEachAlert() {
        Alert alert = alertWithDeviceAndSite();
        when(alertRepository.findAllByOrganizationIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(alert));
        when(feedbackEntryRepository.findFirstByAlertIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());

        List<AlertDto> result = alertService.listForOrganization();

        assertThat(result).hasSize(1);
        AlertDto dto = result.get(0);
        assertThat(dto.deviceRef()).isEqualTo("VLS-DL-0001");
        assertThat(dto.siteId()).isEqualTo(2L);
        assertThat(dto.siteName()).isEqualTo("Indore Godown");
        assertThat(dto.feedbackCorrect()).isNull();
    }

    @Test
    void reflectsTheMostRecentFeedbackAnswer() {
        Alert alert = alertWithDeviceAndSite();
        when(alertRepository.findAllByOrganizationIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(alert));
        FeedbackEntry feedback = new FeedbackEntry();
        feedback.setWasCorrect(true);
        when(feedbackEntryRepository.findFirstByAlertIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(feedback));

        List<AlertDto> result = alertService.listForOrganization();

        assertThat(result.get(0).feedbackCorrect()).isTrue();
    }

    @Test
    void handlesADeviceWithNoShutterMappedYet() {
        Device device = new Device();
        device.setId(9L);
        device.setVelosyssDeviceRef("VLS-DL-0099");

        Alert alert = new Alert();
        alert.setId(1L);
        alert.setOrganization(organization);
        alert.setDevice(device);
        alert.setDirection(EventDirection.OPEN);
        alert.setClassification(AlertClassification.UNEXPLAINED_HIGH);
        alert.setCreatedAt(Instant.now());

        when(alertRepository.findAllByOrganizationIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(alert));
        when(feedbackEntryRepository.findFirstByAlertIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());

        List<AlertDto> result = alertService.listForOrganization();

        assertThat(result.get(0).siteId()).isNull();
        assertThat(result.get(0).siteName()).isNull();
    }

    @Test
    void submitsFeedbackForAnAlertInTheCallersOrganization() {
        Alert alert = alertWithDeviceAndSite();
        alert.setOrganization(organization);
        when(alertRepository.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(alert));
        User submitter = new User();
        submitter.setId(1L);
        when(userRepository.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(submitter));

        alertService.submitFeedback(1L, new SubmitFeedbackRequest(true, "Looks right"));

        verify(feedbackEntryRepository).save(org.mockito.ArgumentMatchers.any(FeedbackEntry.class));
    }
}
