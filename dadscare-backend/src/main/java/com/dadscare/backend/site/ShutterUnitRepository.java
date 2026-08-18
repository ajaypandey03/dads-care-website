package com.dadscare.backend.site;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShutterUnitRepository extends JpaRepository<ShutterUnit, Long> {

    List<ShutterUnit> findAllBySiteId(Long siteId);

    @Query("select su from ShutterUnit su where su.id = :id and su.site.organization.id = :organizationId")
    java.util.Optional<ShutterUnit> findByIdAndOrganizationId(
            @Param("id") Long id, @Param("organizationId") Long organizationId);
}
