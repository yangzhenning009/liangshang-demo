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
@Table(name = "delivery_line", indexes = {
        @Index(name = "idx_delivery_line_event", columnList = "delivery_event_id"),
        @Index(name = "idx_delivery_line_deliverable", columnList = "stage_deliverable_id"),
        @Index(name = "idx_delivery_line_document", columnList = "deliverable_document_id")
})
@Getter
public class DeliveryLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "delivery_event_id", nullable = false)
    public Long deliveryEventId;

    @Column(name = "stage_deliverable_id", nullable = false)
    public Long stageDeliverableId;

    @Column(name = "submitted_quantity", nullable = false, precision = 18, scale = 4)
    public BigDecimal submittedQuantity;

    @Column(name = "unit", nullable = false, length = 32)
    public String unit;

    @Column(name = "delivered_artifact_version", length = 64)
    public String deliveredArtifactVersion;

    @Column(name = "deliverable_document_id")
    public Long deliverableDocumentId;
}
