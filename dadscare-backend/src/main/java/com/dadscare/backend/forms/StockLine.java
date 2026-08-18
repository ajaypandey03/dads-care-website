package com.dadscare.backend.forms;

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
import lombok.Getter;
import lombok.Setter;

/** One inventory line item on a {@link GodownForm} — "Rice, 500, kg" per the original spec's table. */
@Getter
@Setter
@Entity
@Table(name = "stock_lines")
public class StockLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "godown_form_id", nullable = false)
    private GodownForm godownForm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_master_id", nullable = false)
    private ProductMaster product;

    @Column(nullable = false)
    private Integer quantity;

    /** Snapshot of the product's unit at entry time, so a later master-data edit doesn't rewrite history. */
    @Column(nullable = false, length = 20)
    private String unit;
}
