package com.dadscare.backend.masterdata;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMasterRepository extends JpaRepository<ProductMaster, Long> {

    List<ProductMaster> findAllByOrganizationIdAndActiveTrue(Long organizationId);

    Optional<ProductMaster> findByIdAndOrganizationId(Long id, Long organizationId);
}
