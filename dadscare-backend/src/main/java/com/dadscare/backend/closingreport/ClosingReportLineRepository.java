package com.dadscare.backend.closingreport;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosingReportLineRepository extends JpaRepository<ClosingReportLine, Long> {

    List<ClosingReportLine> findAllByClosingReportId(Long closingReportId);

    Optional<ClosingReportLine> findByClosingReportIdAndProductId(Long closingReportId, Long productId);
}
