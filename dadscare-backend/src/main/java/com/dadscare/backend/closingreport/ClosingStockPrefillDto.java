package com.dadscare.backend.closingreport;

import java.math.BigDecimal;

/** One product's suggested opening-stock figure for a new report — see {@code ClosingReportService#prefill}. */
public record ClosingStockPrefillDto(Long productMasterId, String productName, String unit, BigDecimal openingStock) {}
