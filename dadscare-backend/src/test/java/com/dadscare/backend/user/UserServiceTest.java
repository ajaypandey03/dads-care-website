package com.dadscare.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.tenant.OrganizationRepository;
import com.dadscare.backend.tenant.TenantContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private Organization organization;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, organizationRepository, passwordEncoder);

        organization = new Organization();
        organization.setId(7L);
        organization.setName("Acme Logistics");
        organization.setSlug("acme-logistics");
        organization.setCodePrefix("AC");

        TenantContext.set(7L, 42L, "ORG_ADMIN", false);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listsOnlyUsersInTheCallersOrganization() {
        User user = new User();
        user.setId(1L);
        user.setOrganization(organization);
        user.setName("Ops Person");
        user.setEmail("ops@acme.example");
        user.setRole(Role.SITE_MANAGER);
        when(userRepository.findAllByOrganizationId(7L)).thenReturn(List.of(user));

        List<UserAdminDto> result = userService.listOrgUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("ops@acme.example");
    }

    @Test
    void createsAUserWithAGeneratedTemporaryPasswordThatIsHashedBeforeStorage() {
        when(userRepository.findByEmail("new@acme.example")).thenReturn(Optional.empty());
        when(organizationRepository.findById(7L)).thenReturn(Optional.of(organization));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-temp-password");

        CreateUserResponse response = userService.createUser(
                new CreateUserRequest("New Person", "new@acme.example", "9999999999", Role.OPERATOR));

        assertThat(response.temporaryPassword()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("new@acme.example");
        assertThat(response.user().role()).isEqualTo(Role.OPERATOR);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(response.temporaryPassword());
    }

    @Test
    void rejectsCreatingAUserWithAnEmailThatAlreadyExists() {
        when(userRepository.findByEmail("dup@acme.example")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(
                        new CreateUserRequest("Dup", "dup@acme.example", null, Role.VIEWER)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updatesRoleAndStatusForAUserInTheCallersOrganization() {
        User user = new User();
        user.setId(3L);
        user.setOrganization(organization);
        user.setRole(Role.VIEWER);
        user.setStatus("ACTIVE");
        when(userRepository.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(user));

        UserAdminDto result = userService.updateUser(3L, new UpdateUserRequest(Role.SITE_MANAGER, "SUSPENDED"));

        assertThat(result.role()).isEqualTo(Role.SITE_MANAGER);
        assertThat(result.status()).isEqualTo("SUSPENDED");
    }
}
