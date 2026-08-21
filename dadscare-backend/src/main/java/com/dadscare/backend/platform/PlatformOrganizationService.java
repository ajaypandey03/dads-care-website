package com.dadscare.backend.platform;

import com.dadscare.backend.common.TempPasswordGenerator;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.CreateUserRequest;
import com.dadscare.backend.user.CreateUserResponse;
import com.dadscare.backend.user.EmailAlreadyExistsException;
import com.dadscare.backend.user.Role;
import com.dadscare.backend.user.UpdateUserRequest;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserAdminDto;
import com.dadscare.backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-tenant org onboarding and support tooling, restricted to
 * {@link TenantContext#isPlatformAdmin()}. Every public method here MUST call
 * {@link #requirePlatformAdmin()} first — unlike the rest of the codebase, these queries are
 * deliberately NOT scoped to the caller's own organizationId, since the whole point is to
 * operate across every tenant. Methods that act on one specific org still take that org's id
 * as an explicit parameter and look it up for real (never trusting it blindly), which is the
 * cross-tenant equivalent of the tenant-scoping rule every other service follows.
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

    @Transactional(readOnly = true)
    public OrganizationDto getOrganization(Long organizationId) {
        requirePlatformAdmin();
        return OrganizationDto.from(requireOrganization(organizationId));
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

        boolean adminSetPassword = request.adminPassword() != null && !request.adminPassword().isBlank();
        String passwordToUse = adminSetPassword ? request.adminPassword() : TempPasswordGenerator.generate();

        User admin = new User();
        admin.setOrganization(organization);
        admin.setName(request.adminName());
        admin.setEmail(request.adminEmail());
        admin.setPhone(request.adminPhone());
        admin.setRole(Role.ORG_ADMIN);
        admin.setStatus("ACTIVE");
        admin.setPasswordHash(passwordEncoder.encode(passwordToUse));
        userRepository.save(admin);

        return new CreateOrganizationResponse(
                OrganizationDto.from(organization), UserAdminDto.from(admin), adminSetPassword ? null : passwordToUse);
    }

    @Transactional
    public OrganizationDto updateOrganization(Long organizationId, UpdateOrganizationRequest request) {
        requirePlatformAdmin();
        Organization organization = requireOrganization(organizationId);
        organization.setName(request.name());
        organization.setCodePrefix(request.codePrefix());
        organization.setActive(request.active());
        return OrganizationDto.from(organization);
    }

    @Transactional(readOnly = true)
    public List<UserAdminDto> listOrganizationUsers(Long organizationId) {
        requirePlatformAdmin();
        requireOrganization(organizationId);
        return userRepository.findAllByOrganizationId(organizationId).stream()
                .map(UserAdminDto::from)
                .toList();
    }

    @Transactional
    public CreateUserResponse createOrganizationUser(Long organizationId, CreateUserRequest request) {
        requirePlatformAdmin();
        Organization organization = requireOrganization(organizationId);

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException(request.email());
        }

        boolean adminSetPassword = request.password() != null && !request.password().isBlank();
        String passwordToUse = adminSetPassword ? request.password() : TempPasswordGenerator.generate();

        User user = new User();
        user.setOrganization(organization);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setStatus("ACTIVE");
        user.setPasswordHash(passwordEncoder.encode(passwordToUse));
        userRepository.save(user);

        return new CreateUserResponse(UserAdminDto.from(user), adminSetPassword ? null : passwordToUse);
    }

    @Transactional
    public UserAdminDto updateOrganizationUser(Long organizationId, Long userId, UpdateUserRequest request) {
        requirePlatformAdmin();
        User user = requireOrganizationUser(organizationId, userId);
        user.setRole(request.role());
        user.setStatus(request.status());
        return UserAdminDto.from(user);
    }

    /** Support tool: get a locked-out (or forgotten-password) customer admin back into their account. */
    @Transactional
    public ResetPasswordResponse resetOrganizationUserPassword(Long organizationId, Long userId) {
        requirePlatformAdmin();
        User user = requireOrganizationUser(organizationId, userId);
        String temporaryPassword = TempPasswordGenerator.generate();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        return new ResetPasswordResponse(UserAdminDto.from(user), temporaryPassword);
    }

    private Organization requireOrganization(Long organizationId) {
        return organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization " + organizationId + " not found"));
    }

    private User requireOrganizationUser(Long organizationId, Long userId) {
        requireOrganization(organizationId);
        return userRepository
                .findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found in organization " + organizationId));
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new PlatformAdminRequiredException();
        }
    }
}
