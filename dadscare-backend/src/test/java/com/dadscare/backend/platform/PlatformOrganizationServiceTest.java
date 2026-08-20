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
import com.dadscare.backend.user.EmailAlreadyExistsException;
import com.dadscare.backend.user.Role;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
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
                        new CreateOrganizationRequest("Beta Co", "beta-co", "BC", "Admin", "admin@beta.example", null)))
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
                new CreateOrganizationRequest("Beta Co", "beta-co", "BC", "Admin Person", "admin@beta.example", null));

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
                        "Acme Again", "acme-logistics", "AA", "Admin", "admin2@acme.example", null)))
                .isInstanceOf(SlugAlreadyExistsException.class);
    }

    @Test
    void rejectsOnboardingWhenTheAdminEmailAlreadyExists() {
        TenantContext.set(1L, 1L, "ORG_ADMIN", true);
        when(organizationRepository.findBySlug("beta-co")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("dup@beta.example")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.createOrganization(
                        new CreateOrganizationRequest("Beta Co", "beta-co", "BC", "Admin", "dup@beta.example", null)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }
}
