package com.dadscare.backend.user;

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

        String temporaryPassword = TempPasswordGenerator.generate();

        User user = new User();
        user.setOrganization(organization);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setStatus("ACTIVE");
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        return new CreateUserResponse(UserAdminDto.from(user), temporaryPassword);
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
