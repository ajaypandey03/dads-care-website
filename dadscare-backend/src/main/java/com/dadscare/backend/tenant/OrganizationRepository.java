package com.dadscare.backend.tenant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The one repository in the system that is legitimately not tenant-scoped by
 * {@code organizationId} — an Organization IS the tenant.
 */
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findBySlug(String slug);
}
