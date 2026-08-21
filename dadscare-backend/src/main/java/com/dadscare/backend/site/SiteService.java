package com.dadscare.backend.site;

import com.dadscare.backend.telemetry.RawEvent;
import com.dadscare.backend.telemetry.RawEventRepository;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final ShutterUnitRepository shutterUnitRepository;
    private final DeviceRepository deviceRepository;
    private final RawEventRepository rawEventRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<SiteDto> listSites() {
        return siteRepository.findAllByOrganizationId(TenantContext.organizationId()).stream()
                .map(SiteDto::from)
                .toList();
    }

    @Transactional
    public SiteDto createSite(CreateSiteRequest request) {
        Organization organization = organizationRepository
                .findById(TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Site site = new Site();
        site.setOrganization(organization);
        site.setName(request.name());
        site.setGodownCode(request.godownCode());
        site.setAddress(request.address());
        site.setStatus("ACTIVE");
        siteRepository.save(site);
        return SiteDto.from(site);
    }

    @Transactional
    public SiteDto updateSite(Long siteId, UpdateSiteRequest request) {
        Site site = requireSite(siteId);
        site.setName(request.name());
        site.setGodownCode(request.godownCode());
        site.setAddress(request.address());
        site.setStatus(request.status());
        return SiteDto.from(site);
    }

    @Transactional(readOnly = true)
    public List<ShutterUnitDto> listShutterUnits(Long siteId) {
        requireSite(siteId);
        return shutterUnitRepository.findAllBySiteId(siteId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ShutterUnitDto createShutterUnit(Long siteId, CreateShutterUnitRequest request) {
        Site site = requireSite(siteId);
        ShutterUnit unit = new ShutterUnit();
        unit.setSite(site);
        unit.setLabel(request.label());
        unit.setStatus("ACTIVE");
        shutterUnitRepository.save(unit);
        return toDto(unit);
    }

    @Transactional
    public ShutterUnitDto updateShutterUnit(Long shutterUnitId, UpdateShutterUnitRequest request) {
        ShutterUnit unit = shutterUnitRepository
                .findByIdAndOrganizationId(shutterUnitId, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Shutter unit " + shutterUnitId + " not found"));
        unit.setLabel(request.label());
        unit.setStatus(request.status());
        return toDto(unit);
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> listDevices() {
        return deviceRepository.findAllByOrganizationId(TenantContext.organizationId()).stream()
                .map(DeviceDto::from)
                .toList();
    }

    @Transactional
    public DeviceDto createDevice(CreateDeviceRequest request) {
        Long organizationId = TenantContext.organizationId();
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        if (deviceRepository.findByVelosyssDeviceRef(request.velosyssDeviceRef()).isPresent()) {
            throw new DeviceRefAlreadyExistsException(request.velosyssDeviceRef());
        }

        Device device = new Device();
        device.setOrganization(organization);
        device.setVelosyssDeviceRef(request.velosyssDeviceRef());
        device.setStatus("ACTIVE");
        if (request.shutterUnitId() != null) {
            device.setShutterUnit(requireUnassignedShutterUnit(request.shutterUnitId(), organizationId));
        }
        deviceRepository.save(device);
        return DeviceDto.from(device);
    }

    @Transactional
    public DeviceDto updateDevice(Long deviceId, UpdateDeviceRequest request) {
        Long organizationId = TenantContext.organizationId();
        Device device = deviceRepository
                .findByIdAndOrganizationId(deviceId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Device " + deviceId + " not found"));

        deviceRepository
                .findByVelosyssDeviceRef(request.velosyssDeviceRef())
                .filter(other -> !other.getId().equals(deviceId))
                .ifPresent(other -> {
                    throw new DeviceRefAlreadyExistsException(request.velosyssDeviceRef());
                });

        device.setVelosyssDeviceRef(request.velosyssDeviceRef());
        device.setStatus(request.status());
        if (request.shutterUnitId() == null) {
            device.setShutterUnit(null);
        } else if (device.getShutterUnit() == null || !device.getShutterUnit().getId().equals(request.shutterUnitId())) {
            ShutterUnit target = requireUnassignedShutterUnit(request.shutterUnitId(), organizationId);
            // Allow re-mapping onto a shutter this same device already occupies (a no-op), but
            // reject stealing a shutter another device currently owns.
            deviceRepository
                    .findByShutterUnitId(request.shutterUnitId())
                    .filter(other -> !other.getId().equals(deviceId))
                    .ifPresent(other -> {
                        throw new ShutterUnitAlreadyMappedException(request.shutterUnitId());
                    });
            device.setShutterUnit(target);
        }
        return DeviceDto.from(device);
    }

    private ShutterUnit requireUnassignedShutterUnit(Long shutterUnitId, Long organizationId) {
        ShutterUnit unit = shutterUnitRepository
                .findByIdAndOrganizationId(shutterUnitId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Shutter unit " + shutterUnitId + " not found"));
        deviceRepository
                .findByShutterUnitId(shutterUnitId)
                .ifPresent(existing -> {
                    throw new ShutterUnitAlreadyMappedException(shutterUnitId);
                });
        return unit;
    }

    private Site requireSite(Long siteId) {
        return siteRepository
                .findByIdAndOrganizationId(siteId, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Site " + siteId + " not found"));
    }

    private ShutterUnitDto toDto(ShutterUnit unit) {
        Device device = deviceRepository.findByShutterUnitId(unit.getId()).orElse(null);
        if (device == null) {
            return ShutterUnitDto.from(unit, null, ShutterState.UNKNOWN, null, null);
        }
        Optional<RawEvent> lastOpen = rawEventRepository.findFirstByDeviceIdAndEventTypeOrderByEventTimestampDesc(
                device.getId(), RawEvent.EventType.LOCK_OPEN);
        Optional<RawEvent> lastClose = rawEventRepository.findFirstByDeviceIdAndEventTypeOrderByEventTimestampDesc(
                device.getId(), RawEvent.EventType.LOCK_CLOSE);
        Instant lastOpenedAt = lastOpen.map(RawEvent::getEventTimestamp).orElse(null);
        Instant lastClosedAt = lastClose.map(RawEvent::getEventTimestamp).orElse(null);

        ShutterState state;
        if (lastOpenedAt == null && lastClosedAt == null) {
            state = ShutterState.UNKNOWN;
        } else if (lastOpenedAt != null && (lastClosedAt == null || lastOpenedAt.isAfter(lastClosedAt))) {
            state = ShutterState.OPEN;
        } else {
            state = ShutterState.CLOSED;
        }
        return ShutterUnitDto.from(unit, device, state, lastOpenedAt, lastClosedAt);
    }
}
