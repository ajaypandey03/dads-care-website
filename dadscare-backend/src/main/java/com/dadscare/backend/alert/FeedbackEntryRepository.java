package com.dadscare.backend.alert;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackEntryRepository extends JpaRepository<FeedbackEntry, Long> {

    List<FeedbackEntry> findAllByAlertId(Long alertId);
}
