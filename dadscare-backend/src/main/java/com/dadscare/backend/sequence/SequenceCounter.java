package com.dadscare.backend.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Backing row for {@link SequenceCounterService}. Not managed through normal JPA
 * save/update — {@link SequenceCounterService} increments this via a raw atomic SQL
 * statement, so treat this entity as read-only outside that service.
 */
@Getter
@Setter
@Entity
@Table(
        name = "sequence_counters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "counter_type"}))
public class SequenceCounter {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "counter_type", nullable = false, length = 50)
    private String counterType;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;
}
