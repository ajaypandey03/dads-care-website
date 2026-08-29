package com.dadscare.backend.closingreport;

import com.dadscare.backend.masterdata.ProductMaster;
import com.dadscare.backend.masterdata.ProductMasterRepository;
import com.dadscare.backend.site.Site;
import com.dadscare.backend.site.SiteRepository;
import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Storage layer for the customer's existing WhatsApp "Closing Report" — see the pasted
 * template (per-godown, per-product Op./Sale/Recd/Clo Stock/Adv. Billed/&gt;30 days
 * Stock/waiting Trucks table) in the implementation chat. Deliberately just data-in,
 * data-out for now: <strong>how this gets turned into the actual WhatsApp notification
 * (on a schedule? on manual "send" from the dashboard? replacing or alongside the
 * existing lock-event alert?) is not decided yet</strong> — that's a follow-up once the
 * customer confirms the message format. This service only guarantees the numbers have
 * somewhere durable to live and can be queried back out.
 */
@Service
@RequiredArgsConstructor
public class ClosingReportService {

    private final ClosingReportRepository closingReportRepository;
    private final ClosingReportLineRepository closingReportLineRepository;
    private final SiteRepository siteRepository;
    private final ProductMasterRepository productMasterRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClosingReportDto upsert(Long siteId, LocalDate reportDate, UpsertClosingReportRequest request) {
        Long organizationId = TenantContext.organizationId();
        Site site = siteRepository
                .findByIdAndOrganizationId(siteId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Site " + siteId + " not found"));

        ClosingReport report = closingReportRepository
                .findBySiteIdAndReportDate(siteId, reportDate)
                .orElseGet(() -> {
                    ClosingReport created = new ClosingReport();
                    created.setOrganization(site.getOrganization());
                    created.setSite(site);
                    created.setReportDate(reportDate);
                    return created;
                });

        if (request.labourCount() != null) report.setLabourCount(request.labourCount());
        if (request.completedTrucks() != null) report.setCompletedTrucks(request.completedTrucks());
        if (request.standingTrucks() != null) report.setStandingTrucks(request.standingTrucks());
        if (request.remarks() != null) report.setRemarks(request.remarks());

        User submitter = userRepository.findByIdAndOrganizationId(TenantContext.userId(), organizationId).orElse(null);
        if (submitter != null) {
            report.setSubmittedBy(submitter);
        }

        closingReportRepository.save(report);

        if (request.lines() != null) {
            for (var lineRequest : request.lines()) {
                applyLine(report, organizationId, lineRequest);
            }
        }

        return get(siteId, reportDate);
    }

    private void applyLine(ClosingReport report, Long organizationId, UpsertClosingReportRequest.LineRequest lineRequest) {
        ProductMaster product = productMasterRepository
                .findByIdAndOrganizationId(lineRequest.productMasterId(), organizationId)
                .orElseThrow(() -> new EntityNotFoundException("ProductMaster " + lineRequest.productMasterId() + " not found"));

        ClosingReportLine line = closingReportLineRepository
                .findByClosingReportIdAndProductId(report.getId(), product.getId())
                .orElseGet(() -> {
                    ClosingReportLine created = new ClosingReportLine();
                    created.setClosingReport(report);
                    created.setProduct(product);
                    return created;
                });

        if (lineRequest.openingStock() != null) line.setOpeningStock(lineRequest.openingStock());
        if (lineRequest.saleQty() != null) line.setSaleQty(lineRequest.saleQty());
        if (lineRequest.receivedQty() != null) line.setReceivedQty(lineRequest.receivedQty());
        if (lineRequest.closingStock() != null) line.setClosingStock(lineRequest.closingStock());
        if (lineRequest.advanceBilledQty() != null) line.setAdvanceBilledQty(lineRequest.advanceBilledQty());
        if (lineRequest.agingOver30DaysQty() != null) line.setAgingOver30DaysQty(lineRequest.agingOver30DaysQty());
        if (lineRequest.waitingTrucks() != null) line.setWaitingTrucks(lineRequest.waitingTrucks());

        closingReportLineRepository.save(line);
    }

    @Transactional(readOnly = true)
    public ClosingReportDto get(Long siteId, LocalDate reportDate) {
        Long organizationId = TenantContext.organizationId();
        siteRepository
                .findByIdAndOrganizationId(siteId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Site " + siteId + " not found"));

        ClosingReport report = closingReportRepository
                .findBySiteIdAndReportDate(siteId, reportDate)
                .orElseThrow(() -> new EntityNotFoundException("No closing report for site " + siteId + " on " + reportDate));

        return toDto(report);
    }

    @Transactional(readOnly = true)
    public List<ClosingReportDto> listRecent(Long siteId, int limit) {
        return closingReportRepository.findAllBySiteIdOrderByReportDateDesc(siteId, PageRequest.of(0, limit)).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Suggested opening-stock figures for a new report on {@code reportDate}: every
     * active {@link ProductMaster} the org has, each defaulted to the previous report's
     * {@code closingStock} for that product (0 if there is none, or if the product had
     * no line on it). Purely a convenience for the submitting form to pre-fill — the
     * actual opening stock saved is whatever the {@code upsert} call sends, physical
     * count included, same as the customer's own template treats it.
     */
    @Transactional(readOnly = true)
    public List<ClosingStockPrefillDto> prefill(Long siteId, LocalDate reportDate) {
        Long organizationId = TenantContext.organizationId();
        siteRepository
                .findByIdAndOrganizationId(siteId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Site " + siteId + " not found"));

        var previousLines = closingReportRepository
                .findFirstBySiteIdAndReportDateBeforeOrderByReportDateDesc(siteId, reportDate)
                .map(previous -> closingReportLineRepository.findAllByClosingReportId(previous.getId()))
                .orElse(List.of());

        return productMasterRepository.findAllByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(product -> {
                    BigDecimal opening = previousLines.stream()
                            .filter(l -> l.getProduct().getId().equals(product.getId()))
                            .map(ClosingReportLine::getClosingStock)
                            .findFirst()
                            .orElse(BigDecimal.ZERO);
                    return new ClosingStockPrefillDto(product.getId(), product.getName(), product.getUnit(), opening);
                })
                .toList();
    }

    private ClosingReportDto toDto(ClosingReport report) {
        List<ClosingReportLineDto> lines = closingReportLineRepository.findAllByClosingReportId(report.getId()).stream()
                .map(ClosingReportLineDto::from)
                .toList();
        return ClosingReportDto.from(report, lines);
    }
}
