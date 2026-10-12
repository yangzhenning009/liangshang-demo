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
import java.time.LocalDateTime;

/**
 * 应收调整。只增加或只减少，不覆盖原始本金。关联 ID 不建外键。
 */
@Entity
@Table(name = "receivable_adjustment", indexes = {
        @Index(name = "idx_receivable_adj_item", columnList = "receivable_item_id"),
        @Index(name = "idx_receivable_adj_document", columnList = "source_document_id")
})
@Getter
public class ReceivableAdjustment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "receivable_item_id", nullable = false)
    public Long receivableItemId;

    @Column(name = "increase_receivable_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal increaseReceivableAmount;

    @Column(name = "decrease_receivable_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal decreaseReceivableAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "adjusted_at", nullable = false)
    public LocalDateTime adjustedAt;

    @Column(name = "adjustment_reason", nullable = false, length = 500)
    public String adjustmentReason;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
