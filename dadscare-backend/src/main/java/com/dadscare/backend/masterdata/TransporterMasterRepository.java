package com.dadscare.backend.masterdata;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransporterMasterRepository extends JpaRepository<TransporterMaster, Long> {

    List<TransporterMaster> findAllByOrganizationIdAndActiveTrue(Long organizationId);

    Optional<TransporterMaster> findByIdAndOrganizationId(Long id, Long organizationId);
}
