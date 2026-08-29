package com.dadscare.backend.closingreport;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Upserts one site's Closing Report for one date — every field is optional except each
 * line's {@code productMasterId}, since a report can be filled in progressively over the
 * day (e.g. labour count first, stock numbers at close) via repeated PUTs. Each call is
 * authoritative only for the fields/lines it actually sends: an omitted top-level field
 * leaves the existing value alone, and each line in {@code lines} fully replaces that
 * product's existing line (or creates it) — products not mentioned are left untouched.
 */
public record UpsertClosingReportRequest(
        Integer labourCount,
        @PositiveOrZero Integer completedTrucks,
        @PositiveOrZero Integer standingTrucks,
        @Size(max = 500) String remarks,
        @Valid List<LineRequest> lines) {

    public record LineRequest(
            @NotNull Long productMasterId,
            BigDecimal openingStock,
            BigDecimal saleQty,
            BigDecimal receivedQty,
            BigDecimal closingStock,
            BigDecimal advanceBilledQty,
            BigDecimal agingOver30DaysQty,
            @PositiveOrZero Integer waitingTrucks) {}
}
