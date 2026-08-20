package com.dadscare.backend.platform;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cross-tenant customer onboarding for Dad's Care's own staff — see {@link PlatformOrganizationService}. */
@RestController
@RequestMapping("/api/v1/platform/organizations")
@RequiredArgsConstructor
public class PlatformOrganizationController {

    private final PlatformOrganizationService platformOrganizationService;

    @GetMapping
    public List<OrganizationDto> list() {
        return platformOrganizationService.listOrganizations();
    }

    @PostMapping
    public CreateOrganizationResponse create(@Valid @RequestBody CreateOrganizationRequest request) {
        return platformOrganizationService.createOrganization(request);
    }
}
