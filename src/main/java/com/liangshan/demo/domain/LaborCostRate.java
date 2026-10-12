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
 * 人工成本率版本。同一雇佣和成本口径的有效期间不重叠，由应用层校验。
 */
@Entity
@Table(name = "labor_cost_rate", indexes = {
        @Index(name = "idx_labor_rate_employment", columnList = "employment_id"),
        @Index(name = "idx_labor_rate_approver", columnList = "approved_by_employee_id"),
        @Index(name = "idx_labor_rate_document", columnList = "basis_document_id")
})
@Getter
public class LaborCostRate extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "employment_id", nullable = false)
    public Long employmentId;

    @Column(name = "cost_basis", nullable = false, length = 32)
    public String costBasis;

    @Column(name = "hourly_cost_amount", nullable = false, precision = 18, scale = 4)
    public BigDecimal hourlyCostAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "rate_version_number", nullable = false)
    public Integer rateVersionNumber;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "approval_status", nullable = false, length = 32)
    public String approvalStatus;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
