package com.dadscare.backend.platform;

import com.dadscare.backend.user.CreateUserRequest;
import com.dadscare.backend.user.CreateUserResponse;
import com.dadscare.backend.user.UpdateUserRequest;
import com.dadscare.backend.user.UserAdminDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cross-tenant customer onboarding + support tooling for Dad's Care's own staff — see {@link PlatformOrganizationService}. */
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

    @GetMapping("/{id}")
    public OrganizationDto get(@PathVariable Long id) {
        return platformOrganizationService.getOrganization(id);
    }

    @PutMapping("/{id}")
    public OrganizationDto update(@PathVariable Long id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return platformOrganizationService.updateOrganization(id, request);
    }

    @GetMapping("/{id}/users")
    public List<UserAdminDto> listUsers(@PathVariable Long id) {
        return platformOrganizationService.listOrganizationUsers(id);
    }

    @PostMapping("/{id}/users")
    public CreateUserResponse createUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        return platformOrganizationService.createOrganizationUser(id, request);
    }

    @PutMapping("/{id}/users/{userId}")
    public UserAdminDto updateUser(
            @PathVariable Long id, @PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return platformOrganizationService.updateOrganizationUser(id, userId, request);
    }

    @PostMapping("/{id}/users/{userId}/reset-password")
    public ResetPasswordResponse resetUserPassword(@PathVariable Long id, @PathVariable Long userId) {
        return platformOrganizationService.resetOrganizationUserPassword(id, userId);
    }
}
