package com.dadscare.backend.forms;

import com.dadscare.backend.masterdata.ProductMaster;
import com.dadscare.backend.masterdata.TransporterMaster;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** One truck/logistics line item on a {@link GodownForm} per the original spec's table. */
@Getter
@Setter
@Entity
@Table(name = "truck_entries")
public class TruckEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "godown_form_id", nullable = false)
    private GodownForm godownForm;

    @Column(nullable = false)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_master_id", nullable = false)
    private ProductMaster product;

    @Column(name = "vehicle_no", nullable = false, length = 30)
    private String vehicleNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transporter_master_id", nullable = false)
    private TransporterMaster transporter;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "waiting_since")
    private Instant waitingSince;
}
