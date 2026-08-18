package com.dadscare.backend.site;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findAllByOrganizationId(Long organizationId);

    Optional<Site> findByIdAndOrganizationId(Long id, Long organizationId);
}
