package com.dadscare.backend.velosyss;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationCursorRepository extends JpaRepository<IntegrationCursor, Long> {
    Optional<IntegrationCursor> findByCursorName(String cursorName);
}
