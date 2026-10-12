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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收入确认事实。审批不等于过账。冲回用负金额另记一条，不写成负的借贷。关联 ID 不建外键。
 */
@Entity
@Table(name = "revenue_recognition", indexes = {
        @Index(name = "idx_revenue_ledger", columnList = "ledger_id"),
        @Index(name = "idx_revenue_contract", columnList = "contract_id"),
        @Index(name = "idx_revenue_project", columnList = "project_id"),
        @Index(name = "idx_revenue_period", columnList = "accounting_period_id"),
        @Index(name = "idx_revenue_reversal", columnList = "reversal_of_recognition_id")
})
@Getter
public class RevenueRecognition extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "ledger_id", nullable = false)
    public Long ledgerId;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "contract_term_version_id", nullable = false)
    public Long contractTermVersionId;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "recognition_date", nullable = false)
    public LocalDate recognitionDate;

    @Column(name = "accounting_period_id", nullable = false)
    public Long accountingPeriodId;

    @Column(name = "recognized_revenue_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal recognizedRevenueAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "revenue_policy_code", nullable = false, length = 64)
    public String revenuePolicyCode;

    @Column(name = "revenue_policy_version", nullable = false, length = 32)
    public String revenuePolicyVersion;

    @Column(name = "acceptance_event_id")
    public Long acceptanceEventId;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "reversal_of_recognition_id")
    public Long reversalOfRecognitionId;
}
