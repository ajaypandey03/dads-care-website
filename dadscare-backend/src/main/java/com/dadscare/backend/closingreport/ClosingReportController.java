package com.dadscare.backend.closingreport;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sites/{siteId}/closing-reports")
@RequiredArgsConstructor
public class ClosingReportController {

    private final ClosingReportService closingReportService;

    /** VIEWER excluded — same write policy as unlock requests (see UnlockRequestController). */
    @PreAuthorize("hasAnyRole('ORG_ADMIN','SITE_MANAGER','OPERATOR')")
    @PutMapping("/{reportDate}")
    public ClosingReportDto upsert(
            @PathVariable Long siteId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
            @Valid @RequestBody UpsertClosingReportRequest request) {
        return closingReportService.upsert(siteId, reportDate, request);
    }

    @GetMapping("/{reportDate}")
    public ClosingReportDto get(
            @PathVariable Long siteId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        return closingReportService.get(siteId, reportDate);
    }

    @GetMapping
    public List<ClosingReportDto> listRecent(@PathVariable Long siteId, @RequestParam(defaultValue = "30") int limit) {
        return closingReportService.listRecent(siteId, limit);
    }

    /** Suggested opening-stock figures for a new report — see ClosingReportService#prefill. */
    @GetMapping("/{reportDate}/prefill")
    public List<ClosingStockPrefillDto> prefill(
            @PathVariable Long siteId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        return closingReportService.prefill(siteId, reportDate);
    }
}
