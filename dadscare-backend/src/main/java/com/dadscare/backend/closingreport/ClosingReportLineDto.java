package com.dadscare.backend.closingreport;

import java.math.BigDecimal;

public record ClosingReportLineDto(
        Long productMasterId,
        String productName,
        String unit,
        BigDecimal openingStock,
        BigDecimal saleQty,
        BigDecimal receivedQty,
        BigDecimal closingStock,
        BigDecimal advanceBilledQty,
        BigDecimal agingOver30DaysQty,
        Integer waitingTrucks) {

    public static ClosingReportLineDto from(ClosingReportLine line) {
        return new ClosingReportLineDto(
                line.getProduct().getId(),
                line.getProduct().getName(),
                line.getProduct().getUnit(),
                line.getOpeningStock(),
                line.getSaleQty(),
                line.getReceivedQty(),
                line.getClosingStock(),
                line.getAdvanceBilledQty(),
                line.getAgingOver30DaysQty(),
                line.getWaitingTrucks());
    }
}
