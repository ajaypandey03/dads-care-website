package com.dadscare.backend.forms;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GodownFormRepository extends JpaRepository<GodownForm, Long> {

    Optional<GodownForm> findByUnlockRequestId(Long unlockRequestId);
}
