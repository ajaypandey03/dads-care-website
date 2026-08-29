package com.dadscare.backend.site;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findAllByOrganizationId(Long organizationId);

    Optional<Device> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Device> findByShutterUnitId(Long shutterUnitId);

    /**
     * Looked up on every inbound webhook event, deliberately NOT tenant-scoped — the
     * device's own {@code organizationId} is exactly what tells us which tenant the
     * event belongs to (see WebhookService).
     */
    Optional<Device> findByVelosyssDeviceRef(String velosyssDeviceRef);

    /**
     * Looked up on every inbound webhook event (keyed by {@code terminalId}, not the
     * REST {@code velosyssDeviceRef}) — deliberately NOT tenant-scoped, same reasoning
     * as {@link #findByVelosyssDeviceRef}.
     */
    Optional<Device> findByVelosyssTerminalId(String velosyssTerminalId);

    /** Used by VelosyssPollingService to join {@code GET /locks/positions} rows back to devices. */
    java.util.List<Device> findAllByVelosyssTerminalIdIsNotNull();
}
