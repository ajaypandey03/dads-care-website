package com.dadscare.backend.user;

import com.dadscare.backend.common.TempPasswordGenerator;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDto currentUser() {
        return UserDto.from(requireCurrentUser());
    }

    @Transactional
    public void registerPushToken(RegisterPushTokenRequest request) {
        User user = requireCurrentUser();
        user.setPushToken(request.token());
    }

    /** Self-service password change — no email/reset-link flow exists, so this is the only way in-app. */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = requireCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IncorrectPasswordException();
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * Lists every teammate in the caller's org. Note: unlike most endpoints in this service,
     * this and the mutating methods below aren't yet role-gated to ORG_ADMIN — every controller
     * in this codebase currently only enforces tenant scoping, not per-role authorization. Real
     * hardening (reject non-admins with 403) is a follow-up, tracked in the tracker as a gap.
     */
    @Transactional(readOnly = true)
    public List<UserAdminDto> listOrgUsers() {
        return userRepository.findAllByOrganizationId(TenantContext.organizationId()).stream()
                .map(UserAdminDto::from)
                .toList();
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException(request.email());
        }
        Organization organization = organizationRepository
                .findById(TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        // Admin can set the password directly, or leave it blank to get a generated one —
        // either way there's no email/SMTP integration, so it's relayed out-of-band regardless.
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
    public UserAdminDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository
                .findByIdAndOrganizationId(userId, TenantContext.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));
        user.setRole(request.role());
        user.setStatus(request.status());
        return UserAdminDto.from(user);
    }

    private User requireCurrentUser() {
        return userRepository
                .findByIdAndOrganizationId(TenantContext.userId(), TenantContext.organizationId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
