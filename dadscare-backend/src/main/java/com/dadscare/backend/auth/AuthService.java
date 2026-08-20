package com.dadscare.backend.auth;

import com.dadscare.backend.security.JwtService;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.email())
                .filter(u -> "ACTIVE".equals(u.getStatus()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.issueAccessToken(
                user.getId(), user.getOrganization().getId(), user.getRole().name(), user.isPlatformAdmin());

        return LoginResponse.bearer(
                token, user.getId(), user.getOrganization().getId(), user.getRole().name(), user.isPlatformAdmin());
    }
}
