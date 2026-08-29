package com.dadscare.backend.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.dadscare.backend.telemetry.RawEvent;
import com.dadscare.backend.telemetry.RawEventRepository;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
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
class SiteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ShutterUnitRepository shutterUnitRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RawEventRepository rawEventRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private SiteService siteService;
    private Organization organization;

    @BeforeEach
    void setUp() {
        siteService = new SiteService(
                siteRepository, shutterUnitRepository, deviceRepository, rawEventRepository, organizationRepository);
        organization = new Organization();
        organization.setId(7L);
        TenantContext.set(7L, 1L, "ORG_ADMIN", false);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Site site() {
        Site site = new Site();
        site.setId(1L);
        site.setOrganization(organization);
        site.setName("Indore Godown");
        return site;
    }

    private ShutterUnit unit(Site site) {
        ShutterUnit unit = new ShutterUnit();
        unit.setId(5L);
        unit.setSite(site);
        unit.setLabel("Main Shutter");
        unit.setStatus("ACTIVE");
        return unit;
    }

    private Device device() {
        Device device = new Device();
        device.setId(9L);
        device.setOrganization(organization);
        device.setVelosyssDeviceRef("VLS-DL-0001");
        return device;
    }

    @Test
    void createsASite() {
        when(organizationRepository.findById(7L)).thenReturn(Optional.of(organization));

        SiteDto result = siteService.createSite(new CreateSiteRequest("Indore Godown", "DC-IND-01", "Indore, MP"));

        assertThat(result.name()).isEqualTo("Indore Godown");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void derivesOpenStateWhenTheLastEventIsALockOpen() {
        Site site = site();
        ShutterUnit unit = unit(site);
        Device device = device();
        when(siteRepository.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(site));
        when(shutterUnitRepository.findAllBySiteId(1L)).thenReturn(List.of(unit));
        when(deviceRepository.findByShutterUnitId(5L)).thenReturn(Optional.of(device));

        RawEvent openEvent = new RawEvent();
        openEvent.setEventTimestamp(Instant.parse("2026-08-21T10:00:00Z"));
        RawEvent closeEvent = new RawEvent();
        closeEvent.setEventTimestamp(Instant.parse("2026-08-21T08:00:00Z"));
        when(rawEventRepository.findFirstByDeviceIdAndEventTypeOrderByEventTimestampDesc(9L, RawEvent.EventType.LOCK_OPEN))
                .thenReturn(Optional.of(openEvent));
        when(rawEventRepository.findFirstByDeviceIdAndEventTypeOrderByEventTimestampDesc(9L, RawEvent.EventType.LOCK_CLOSE))
                .thenReturn(Optional.of(closeEvent));

        List<ShutterUnitDto> result = siteService.listShutterUnits(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).currentState()).isEqualTo(ShutterState.OPEN);
        assertThat(result.get(0).lastOpenedAt()).isEqualTo(openEvent.getEventTimestamp());
        assertThat(result.get(0).lastClosedAt()).isEqualTo(closeEvent.getEventTimestamp());
    }

    @Test
    void derivesUnknownStateWhenTheShutterHasNoDeviceMapped() {
        Site site = site();
        ShutterUnit unit = unit(site);
        when(siteRepository.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(site));
        when(shutterUnitRepository.findAllBySiteId(1L)).thenReturn(List.of(unit));
        when(deviceRepository.findByShutterUnitId(5L)).thenReturn(Optional.empty());

        List<ShutterUnitDto> result = siteService.listShutterUnits(1L);

        assertThat(result.get(0).currentState()).isEqualTo(ShutterState.UNKNOWN);
        assertThat(result.get(0).device()).isNull();
    }

    @Test
    void registersADeviceAndRejectsADuplicateVelosyssRef() {
        when(organizationRepository.findById(7L)).thenReturn(Optional.of(organization));
        when(deviceRepository.findByVelosyssDeviceRef("VLS-DL-0002")).thenReturn(Optional.empty());

        DeviceDto result = siteService.createDevice(new CreateDeviceRequest("VLS-DL-0002", null, null));
        assertThat(result.velosyssDeviceRef()).isEqualTo("VLS-DL-0002");

        when(deviceRepository.findByVelosyssDeviceRef("VLS-DL-0002")).thenReturn(Optional.of(device()));
        assertThatThrownBy(() -> siteService.createDevice(new CreateDeviceRequest("VLS-DL-0002", null, null)))
                .isInstanceOf(DeviceRefAlreadyExistsException.class);
    }

    @Test
    void rejectsMappingADeviceToAShutterAlreadyMappedToAnotherDevice() {
        Site site = site();
        ShutterUnit unit = unit(site);
        when(organizationRepository.findById(7L)).thenReturn(Optional.of(organization));
        when(deviceRepository.findByVelosyssDeviceRef("VLS-DL-0003")).thenReturn(Optional.empty());
        when(shutterUnitRepository.findByIdAndOrganizationId(5L, 7L)).thenReturn(Optional.of(unit));
        Device occupying = device();
        occupying.setId(99L);
        when(deviceRepository.findByShutterUnitId(5L)).thenReturn(Optional.of(occupying));

        assertThatThrownBy(() -> siteService.createDevice(new CreateDeviceRequest("VLS-DL-0003", null, 5L)))
                .isInstanceOf(ShutterUnitAlreadyMappedException.class);
    }

    @Test
    void rejectsUpdatingASiteThatDoesNotExist() {
        when(siteRepository.findByIdAndOrganizationId(404L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> siteService.updateSite(404L, new UpdateSiteRequest("X", "X", null, "ACTIVE")))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
