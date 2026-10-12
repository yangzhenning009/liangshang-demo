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
 * 报销明细。指向已有费用，不复制成本金额。
 */
@Entity
@Table(name = "expense_claim_line", indexes = {
        @Index(name = "idx_claim_line_claim", columnList = "expense_claim_id"),
        @Index(name = "idx_claim_line_cost", columnList = "cost_occurrence_id")
})
@Getter
public class ExpenseClaimLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "expense_claim_id", nullable = false)
    public Long expenseClaimId;

    @Column(name = "line_number", nullable = false)
    public Integer lineNumber;

    @Column(name = "cost_occurrence_id", nullable = false)
    public Long costOccurrenceId;

    @Column(name = "claimed_reimbursement_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal claimedReimbursementAmount;

    @Column(name = "approved_reimbursement_amount", precision = 18, scale = 2)
    public BigDecimal approvedReimbursementAmount;

    @Column(name = "rejection_reason", length = 500)
    public String rejectionReason;
}
