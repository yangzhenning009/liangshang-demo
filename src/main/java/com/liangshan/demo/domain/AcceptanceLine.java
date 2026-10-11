package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "acceptance_line", indexes = {
        @Index(name = "idx_acceptance_line_event", columnList = "acceptance_event_id"),
        @Index(name = "idx_acceptance_line_delivery", columnList = "delivery_line_id")
})
@Getter
public class AcceptanceLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "acceptance_event_id", nullable = false)
    public Long acceptanceEventId;

    @Column(name = "delivery_line_id", nullable = false)
    public Long deliveryLineId;

    @Column(name = "reviewed_quantity", nullable = false, precision = 18, scale = 4)
    public BigDecimal reviewedQuantity;

    @Column(name = "accepted_quantity", nullable = false, precision = 18, scale = 4)
    public BigDecimal acceptedQuantity;

    @Column(name = "rejected_quantity", nullable = false, precision = 18, scale = 4)
    public BigDecimal rejectedQuantity;

    @Column(name = "unit", nullable = false, length = 32)
    public String unit;

    @Column(name = "rejection_reason", length = 500)
    public String rejectionReason;
}
