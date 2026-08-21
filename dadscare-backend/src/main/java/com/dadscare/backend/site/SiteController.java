package com.dadscare.backend.site;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping
    public List<SiteDto> list() {
        return siteService.listSites();
    }

    @PreAuthorize("hasAnyRole('ORG_ADMIN','SITE_MANAGER')")
    @PostMapping
    public SiteDto create(@Valid @RequestBody CreateSiteRequest request) {
        return siteService.createSite(request);
    }

    @PreAuthorize("hasAnyRole('ORG_ADMIN','SITE_MANAGER')")
    @PutMapping("/{id}")
    public SiteDto update(@PathVariable Long id, @Valid @RequestBody UpdateSiteRequest request) {
        return siteService.updateSite(id, request);
    }

    @GetMapping("/{siteId}/shutter-units")
    public List<ShutterUnitDto> listShutterUnits(@PathVariable Long siteId) {
        return siteService.listShutterUnits(siteId);
    }

    @PreAuthorize("hasAnyRole('ORG_ADMIN','SITE_MANAGER')")
    @PostMapping("/{siteId}/shutter-units")
    public ShutterUnitDto createShutterUnit(
            @PathVariable Long siteId, @Valid @RequestBody CreateShutterUnitRequest request) {
        return siteService.createShutterUnit(siteId, request);
    }
}
