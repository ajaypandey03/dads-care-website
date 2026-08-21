package com.dadscare.backend.user;

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

/**
 * Tenant-admin screen support: list/invite/update the org's own users. See {@link UserController} for /me.
 * ORG_ADMIN only, including the list — the team roster is identity/PII data (names, emails, phones), not
 * operational data, so it's scoped tighter than the rest of this codebase's "reads are open" convention.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORG_ADMIN')")
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public List<UserAdminDto> list() {
        return userService.listOrgUsers();
    }

    @PostMapping
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserAdminDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }
}
