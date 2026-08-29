package com.dadscare.backend.closingreport;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ClosingReportDto(
        Long id,
        Long siteId,
        LocalDate reportDate,
        Integer labourCount,
        Integer completedTrucks,
        Integer standingTrucks,
        String remarks,
        String submittedByName,
        List<ClosingReportLineDto> lines,
        ClosingReportTotalsDto totals,
        Instant createdAt,
        Instant updatedAt) {

    public static ClosingReportDto from(ClosingReport report, List<ClosingReportLineDto> lines) {
        return new ClosingReportDto(
                report.getId(),
                report.getSite().getId(),
                report.getReportDate(),
                report.getLabourCount(),
                report.getCompletedTrucks(),
                report.getStandingTrucks(),
                report.getRemarks(),
                report.getSubmittedBy() != null ? report.getSubmittedBy().getName() : null,
                lines,
                ClosingReportTotalsDto.of(lines),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }
}
