package com.dadscare.backend.site;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{siteId}/shutter-units")
    public List<ShutterUnitDto> listShutterUnits(@PathVariable Long siteId) {
        return siteService.listShutterUnits(siteId);
    }
}
