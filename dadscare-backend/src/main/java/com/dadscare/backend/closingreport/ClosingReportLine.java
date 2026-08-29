package com.dadscare.backend.closingreport;

import com.dadscare.backend.masterdata.ProductMaster;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * One product's row in a {@link ClosingReport}'s table — the seven columns from the
 * customer's template (Op. Stock / Sale / Recd / Clo Stock / Adv. Billed / &gt;30 days
 * Stock / waiting Trucks), one {@link ClosingReportLine} per {@link ProductMaster} the
 * organization has configured. Quantities are in the product's own unit (see
 * {@code ProductMaster#unit}) — not re-validated or converted here, same approach as
 * {@code StockLine#unit}.
 */
@Getter
@Setter
@Entity
@Table(name = "closing_report_lines")
public class ClosingReportLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "closing_report_id", nullable = false)
    private ClosingReport closingReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_master_id", nullable = false)
    private ProductMaster product;

    /** Carried forward from the previous day's {@link #closingStock} for this site+product when a new report is created — see ClosingReportService. */
    @Column(name = "opening_stock", nullable = false, precision = 12, scale = 3)
    private BigDecimal openingStock = BigDecimal.ZERO;

    @Column(name = "sale_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal saleQty = BigDecimal.ZERO;

    @Column(name = "received_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal receivedQty = BigDecimal.ZERO;

    /** Physically counted closing stock — entered directly (per the template), not purely derived. */
    @Column(name = "closing_stock", nullable = false, precision = 12, scale = 3)
    private BigDecimal closingStock = BigDecimal.ZERO;

    /** Billed to a customer but not yet physically dispatched. */
    @Column(name = "advance_billed_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal advanceBilledQty = BigDecimal.ZERO;

    /** Portion of {@link #closingStock} that's aged beyond 30 days. */
    @Column(name = "aging_over_30_days_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal agingOver30DaysQty = BigDecimal.ZERO;

    /** Trucks currently waiting specifically for this product. */
    @Column(name = "waiting_trucks", nullable = false)
    private Integer waitingTrucks = 0;
}
