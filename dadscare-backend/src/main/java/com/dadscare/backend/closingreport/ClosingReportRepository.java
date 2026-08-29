package com.dadscare.backend.closingreport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosingReportRepository extends JpaRepository<ClosingReport, Long> {

    Optional<ClosingReport> findBySiteIdAndReportDate(Long siteId, LocalDate reportDate);

    Optional<ClosingReport> findByIdAndOrganizationId(Long id, Long organizationId);

    List<ClosingReport> findAllBySiteIdOrderByReportDateDesc(Long siteId, Pageable pageable);

    /** Most recent report strictly before the given date — the source for a new day's opening-stock prefill. */
    Optional<ClosingReport> findFirstBySiteIdAndReportDateBeforeOrderByReportDateDesc(Long siteId, LocalDate reportDate);
}
