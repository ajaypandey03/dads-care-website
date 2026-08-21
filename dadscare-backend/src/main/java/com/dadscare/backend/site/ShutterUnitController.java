package com.dadscare.backend.site;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shutter-units")
@RequiredArgsConstructor
public class ShutterUnitController {

    private final SiteService siteService;

    @PreAuthorize("hasAnyRole('ORG_ADMIN','SITE_MANAGER')")
    @PutMapping("/{id}")
    public ShutterUnitDto update(@PathVariable Long id, @Valid @RequestBody UpdateShutterUnitRequest request) {
        return siteService.updateShutterUnit(id, request);
    }
}
