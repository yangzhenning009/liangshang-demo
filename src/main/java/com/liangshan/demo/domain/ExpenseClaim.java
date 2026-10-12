package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 员工报销单。类型至少包括项目费用和体检费用。批准后形成应付，不再另记一笔成本。
 */
@Entity
@Table(name = "expense_claim", indexes = {
        @Index(name = "idx_claim_number", columnList = "claim_number"),
        @Index(name = "idx_claim_employee", columnList = "claimant_employee_id"),
        @Index(name = "idx_claim_company", columnList = "reimbursement_company_id"),
        @Index(name = "idx_claim_approver", columnList = "approved_by_employee_id"),
        @Index(name = "idx_claim_document", columnList = "source_document_id")
})
@Getter
public class ExpenseClaim extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "claim_number", nullable = false, length = 64)
    public String claimNumber;

    @Column(name = "claim_type", nullable = false, length = 32)
    public String claimType;

    @Column(name = "claimant_employee_id", nullable = false)
    public Long claimantEmployeeId;

    @Column(name = "reimbursement_company_id", nullable = false)
    public Long reimbursementCompanyId;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "submitted_at")
    public LocalDateTime submittedAt;

    @Column(name = "approval_status", nullable = false, length = 32)
    public String approvalStatus;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "approval_comment", length = 1000)
    public String approvalComment;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
