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

/**
 * 分录的业务来源。每行恰好指向一种事实。贡献金额与该行非零一侧核对。关联 ID 不建外键。
 */
@Entity
@Table(name = "journal_line_source", indexes = {
        @Index(name = "idx_journal_source_line", columnList = "journal_line_id"),
        @Index(name = "idx_journal_source_cost", columnList = "cost_occurrence_id"),
        @Index(name = "idx_journal_source_receivable", columnList = "receivable_item_id"),
        @Index(name = "idx_journal_source_payable", columnList = "payable_item_id"),
        @Index(name = "idx_journal_source_payment", columnList = "payment_id"),
        @Index(name = "idx_journal_source_revenue", columnList = "revenue_recognition_id")
})
@Getter
public class JournalLineSource extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "journal_line_id", nullable = false)
    public Long journalLineId;

    @Column(name = "invoice_line_id")
    public Long invoiceLineId;

    @Column(name = "payment_id")
    public Long paymentId;

    @Column(name = "receivable_item_id")
    public Long receivableItemId;

    @Column(name = "payable_item_id")
    public Long payableItemId;

    @Column(name = "receivable_adjustment_id")
    public Long receivableAdjustmentId;

    @Column(name = "payable_adjustment_id")
    public Long payableAdjustmentId;

    @Column(name = "cost_allocation_id")
    public Long costAllocationId;

    @Column(name = "cost_occurrence_id")
    public Long costOccurrenceId;

    @Column(name = "revenue_recognition_id")
    public Long revenueRecognitionId;

    @Column(name = "source_contribution_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal sourceContributionAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
