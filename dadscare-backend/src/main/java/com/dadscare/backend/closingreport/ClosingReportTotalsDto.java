package com.dadscare.backend.closingreport;

import java.math.BigDecimal;
import java.util.List;

/** Column sums across every {@link ClosingReportLine} — the "Total Summary" row/column in the customer's template. */
public record ClosingReportTotalsDto(
        BigDecimal openingStock,
        BigDecimal saleQty,
        BigDecimal receivedQty,
        BigDecimal closingStock,
        BigDecimal advanceBilledQty,
        BigDecimal agingOver30DaysQty,
        int waitingTrucks) {

    public static ClosingReportTotalsDto of(List<ClosingReportLineDto> lines) {
        return new ClosingReportTotalsDto(
                sum(lines, ClosingReportLineDto::openingStock),
                sum(lines, ClosingReportLineDto::saleQty),
                sum(lines, ClosingReportLineDto::receivedQty),
                sum(lines, ClosingReportLineDto::closingStock),
                sum(lines, ClosingReportLineDto::advanceBilledQty),
                sum(lines, ClosingReportLineDto::agingOver30DaysQty),
                lines.stream().mapToInt(l -> l.waitingTrucks() == null ? 0 : l.waitingTrucks()).sum());
    }

    private static BigDecimal sum(List<ClosingReportLineDto> lines, java.util.function.Function<ClosingReportLineDto, BigDecimal> field) {
        return lines.stream().map(field).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
