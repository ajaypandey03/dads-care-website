package com.dadscare.backend.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PlatformOrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PlatformOrganizationService service;

    @BeforeEach
    void setUp() {
        service = new PlatformOrganizationService(organizationRepository, userRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void rejectsListingForACallerWhoIsNotAPlatformAdmin() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", false);

        assertThatThrownBy(() -> service.listOrganizations()).isInstanceOf(PlatformAdminRequiredException.class);
    }

    @Test
    void rejectsCreatingAnOrganizationForACallerWhoIsNotAPlatformAdmin() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", false);

        assertThatThrownBy(() -> service.createOrganization(
                        new CreateOrganizationRequest("Beta Co", "beta-co", "BC", "Admin", "admin@beta.example", null, null)))
                .isInstanceOf(PlatformAdminRequiredException.class);
    }

    @Test
    void listsEveryOrganizationForAPlatformAdmin() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Acme Logistics");
        org.setSlug("acme-logistics");
        org.setCodePrefix("AC");
        when(organizationRepository.findAll()).thenReturn(List.of(org));

        List<OrganizationDto> result = service.listOrganizations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).slug()).isEqualTo("acme-logistics");
    }

    @Test
    void createsAnOrganizationAndItsFirstOrgAdminWithATemporaryPassword() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        when(organizationRepository.findBySlug("beta-co")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@beta.example")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-temp-password");

        CreateOrganizationResponse response = service.createOrganization(
                new CreateOrganizationRequest("Beta Co", "beta-co", "BC", "Admin Person", "admin@beta.example", null, null));

        assertThat(response.organization().slug()).isEqualTo("beta-co");
        assertThat(response.adminUser().email()).isEqualTo("admin@beta.example");
        assertThat(response.adminUser().role()).isEqualTo(Role.ORG_ADMIN);
        assertThat(response.temporaryPassword()).isNotBlank();
        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void rejectsOnboardingAnOrganizationWhoseSlugAlreadyExists() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        when(organizationRepository.findBySlug("acme-logistics")).thenReturn(Optional.of(new Organization()));

        assertThatThrownBy(() -> service.createOrganization(new CreateOrganizationRequest(
                        "Acme Again", "acme-logistics", "AA", "Admin", "admin2@acme.example", null, null)))
                .isInstanceOf(SlugAlreadyExistsException.class);
    }

    @Test
    void rejectsOnboardingWhenTheAdminEmailAlreadyExists() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        when(organizationRepository.findBySlug("beta-co")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("dup@beta.example")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.createOrganization(
                        new CreateOrganizationRequest("Beta Co", "beta-co", "BC", "Admin", "dup@beta.example", null, null)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updatesAnOrganizationsNameCodePrefixAndActiveFlag() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(9L);
        org.setName("Old Name");
        org.setSlug("old-slug");
        org.setCodePrefix("OLD");
        org.setActive(true);
        when(organizationRepository.findById(9L)).thenReturn(Optional.of(org));

        OrganizationDto result = service.updateOrganization(9L, new UpdateOrganizationRequest("New Name", "NEW", false));

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.codePrefix()).isEqualTo("NEW");
        assertThat(result.active()).isFalse();
        // slug is untouched by design
        assertThat(org.getSlug()).isEqualTo("old-slug");
    }

    @Test
    void rejectsUpdatingAnOrganizationThatDoesNotExist() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        when(organizationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrganization(404L, new UpdateOrganizationRequest("X", "X", true)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listsUsersForASpecificOrganization() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(9L);
        when(organizationRepository.findById(9L)).thenReturn(Optional.of(org));
        User user = new User();
        user.setId(3L);
        user.setOrganization(org);
        user.setName("Someone");
        user.setEmail("someone@beta.example");
        user.setRole(Role.VIEWER);
        when(userRepository.findAllByOrganizationId(9L)).thenReturn(List.of(user));

        List<UserAdminDto> result = service.listOrganizationUsers(9L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("someone@beta.example");
    }

    @Test
    void createsAUserDirectlyInsideAnyOrganization() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(9L);
        when(organizationRepository.findById(9L)).thenReturn(Optional.of(org));
        when(userRepository.findByEmail("new@beta.example")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        CreateUserResponse response =
                service.createOrganizationUser(9L, new CreateUserRequest("New Person", "new@beta.example", null, Role.OPERATOR, null));

        assertThat(response.user().email()).isEqualTo("new@beta.example");
        assertThat(response.temporaryPassword()).isNotBlank();
    }

    @Test
    void updatesARolesAndStatusForAUserInAGivenOrganization() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(9L);
        when(organizationRepository.findById(9L)).thenReturn(Optional.of(org));
        User user = new User();
        user.setId(3L);
        user.setOrganization(org);
        user.setRole(Role.VIEWER);
        user.setStatus("ACTIVE");
        when(userRepository.findByIdAndOrganizationId(3L, 9L)).thenReturn(Optional.of(user));

        UserAdminDto result = service.updateOrganizationUser(9L, 3L, new UpdateUserRequest(Role.SITE_MANAGER, "SUSPENDED", null));

        assertThat(result.role()).isEqualTo(Role.SITE_MANAGER);
        assertThat(result.status()).isEqualTo("SUSPENDED");
    }

    @Test
    void resetsAUsersPasswordAndReturnsItExactlyOnce() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(9L);
        when(organizationRepository.findById(9L)).thenReturn(Optional.of(org));
        User user = new User();
        user.setId(3L);
        user.setOrganization(org);
        user.setEmail("locked-out@beta.example");
        when(userRepository.findByIdAndOrganizationId(3L, 9L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("new-hashed-password");

        ResetPasswordResponse response = service.resetOrganizationUserPassword(9L, 3L);

        assertThat(response.user().email()).isEqualTo("locked-out@beta.example");
        assertThat(response.temporaryPassword()).isNotBlank();
        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
    }

    @Test
    void rejectsActingOnAUserThatDoesNotBelongToTheGivenOrganization() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        Organization org = new Organization();
        org.setId(9L);
        when(organizationRepository.findById(9L)).thenReturn(Optional.of(org));
        when(userRepository.findByIdAndOrganizationId(3L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrganizationUser(9L, 3L, new UpdateUserRequest(Role.VIEWER, "ACTIVE", null)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
