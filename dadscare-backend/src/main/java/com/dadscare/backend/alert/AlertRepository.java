package com.dadscare.backend.alert;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    Optional<Alert> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByRawEventId(Long rawEventId);
}
