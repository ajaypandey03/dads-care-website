package com.dadscare.backend.platform;

import com.dadscare.backend.common.TempPasswordGenerator;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.EmailAlreadyExistsException;
import com.dadscare.backend.user.Role;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserAdminDto;
import com.dadscare.backend.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-tenant org onboarding, restricted to {@link TenantContext#isPlatformAdmin()}. Every
 * public method here MUST call {@link #requirePlatformAdmin()} first — unlike the rest of the
 * codebase, these queries are deliberately NOT organizationId-scoped, since the whole point is
 * to operate across every tenant.
 */
@Service
@RequiredArgsConstructor
public class PlatformOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<OrganizationDto> listOrganizations() {
        requirePlatformAdmin();
        return organizationRepository.findAll().stream().map(OrganizationDto::from).toList();
    }

    @Transactional
    public CreateOrganizationResponse createOrganization(CreateOrganizationRequest request) {
        requirePlatformAdmin();

        if (organizationRepository.findBySlug(request.slug()).isPresent()) {
            throw new SlugAlreadyExistsException(request.slug());
        }
        if (userRepository.findByEmail(request.adminEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.adminEmail());
        }

        Organization organization = new Organization();
        organization.setName(request.name());
        organization.setSlug(request.slug());
        organization.setCodePrefix(request.codePrefix());
        organization.setActive(true);
        organizationRepository.save(organization);

        String temporaryPassword = TempPasswordGenerator.generate();

        User admin = new User();
        admin.setOrganization(organization);
        admin.setName(request.adminName());
        admin.setEmail(request.adminEmail());
        admin.setPhone(request.adminPhone());
        admin.setRole(Role.ORG_ADMIN);
        admin.setStatus("ACTIVE");
        admin.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        userRepository.save(admin);

        return new CreateOrganizationResponse(OrganizationDto.from(organization), UserAdminDto.from(admin), temporaryPassword);
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new PlatformAdminRequiredException();
        }
    }
}
