package com.dadscare.backend.site;

import com.dadscare.backend.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final ShutterUnitRepository shutterUnitRepository;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public List<SiteDto> listSites() {
        return siteRepository.findAllByOrganizationId(TenantContext.organizationId()).stream()
                .map(SiteDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShutterUnitDto> listShutterUnits(Long siteId) {
        Long organizationId = TenantContext.organizationId();
        // Confirms the site belongs to the caller's org before returning anything under it.
        siteRepository
                .findByIdAndOrganizationId(siteId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Site " + siteId + " not found"));

        return shutterUnitRepository.findAllBySiteId(siteId).stream()
                .map(unit -> ShutterUnitDto.from(unit, deviceRepository.findByShutterUnitId(unit.getId()).orElse(null)))
                .toList();
    }
}
