package com.dadscare.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dadscare.backend.security.JwtService;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.user.Role;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private User activeUser;
    private Organization organization;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);

        organization = new Organization();
        organization.setId(7L);
        organization.setName("Acme Logistics");
        organization.setSlug("acme-logistics");
        organization.setCodePrefix("AC");

        activeUser = new User();
        activeUser.setId(42L);
        activeUser.setOrganization(organization);
        activeUser.setEmail("ops@acme.example");
        activeUser.setPasswordHash("hashed-password");
        activeUser.setRole(Role.SITE_MANAGER);
        activeUser.setStatus("ACTIVE");
    }

    @Test
    void issuesATokenForCorrectCredentials() {
        when(userRepository.findByEmail("ops@acme.example")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.issueAccessToken(42L, 7L, "SITE_MANAGER")).thenReturn("signed.jwt.token");

        LoginResponse response = authService.login(new LoginRequest("ops@acme.example", "correct-password"));

        assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.organizationId()).isEqualTo(7L);
        assertThat(response.role()).isEqualTo("SITE_MANAGER");
    }

    @Test
    void rejectsAWrongPasswordWithoutIssuingAToken() {
        when(userRepository.findByEmail("ops@acme.example")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ops@acme.example", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsAnUnknownEmailWithoutLeakingWhetherItExists() {
        when(userRepository.findByEmail("nobody@acme.example")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@acme.example", "anything")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsADeactivatedUserEvenWithTheCorrectPassword() {
        activeUser.setStatus("SUSPENDED");
        when(userRepository.findByEmail("ops@acme.example")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ops@acme.example", "correct-password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
