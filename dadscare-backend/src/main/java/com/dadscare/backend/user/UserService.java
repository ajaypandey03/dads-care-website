package com.dadscare.backend.user;

import com.dadscare.backend.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDto currentUser() {
        return UserDto.from(requireCurrentUser());
    }

    @Transactional
    public void registerPushToken(RegisterPushTokenRequest request) {
        User user = requireCurrentUser();
        user.setPushToken(request.token());
    }

    private User requireCurrentUser() {
        return userRepository
                .findByIdAndOrganizationId(TenantContext.userId(), TenantContext.organizationId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
