package com.dadscare.backend.alert;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackEntryRepository extends JpaRepository<FeedbackEntry, Long> {

    List<FeedbackEntry> findAllByAlertId(Long alertId);

    /** Used to show "already answered" state on the Alerts page — one alert can have multiple
     *  entries over time (a correction), so this is the current, most-recent answer. */
    Optional<FeedbackEntry> findFirstByAlertIdOrderByCreatedAtDesc(Long alertId);
}
