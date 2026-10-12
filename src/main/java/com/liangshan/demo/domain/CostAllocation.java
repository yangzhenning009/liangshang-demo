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
 * 费用分配。有效分配合计须等于成本金额，由应用层校验。关联 ID 不建外键。
 */
@Entity
@Table(name = "cost_allocation", indexes = {
        @Index(name = "idx_allocation_cost", columnList = "cost_occurrence_id"),
        @Index(name = "idx_allocation_project", columnList = "project_id"),
        @Index(name = "idx_allocation_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_allocation_employee", columnList = "allocated_by_employee_id"),
        @Index(name = "idx_allocation_supersedes", columnList = "supersedes_cost_allocation_id"),
        @Index(name = "idx_allocation_document", columnList = "basis_document_id")
})
@Getter
public class CostAllocation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "cost_occurrence_id", nullable = false)
    public Long costOccurrenceId;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "allocated_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal allocatedAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "allocation_period", nullable = false, length = 32)
    public String allocationPeriod;

    @Column(name = "allocation_method", nullable = false, length = 32)
    public String allocationMethod;

    @Column(name = "allocation_basis_hours", precision = 8, scale = 2)
    public BigDecimal allocationBasisHours;

    @Column(name = "allocation_ratio", precision = 18, scale = 6)
    public BigDecimal allocationRatio;

    @Column(name = "allocated_by_employee_id")
    public Long allocatedByEmployeeId;

    @Column(name = "allocated_at", nullable = false)
    public LocalDateTime allocatedAt;

    @Column(name = "supersedes_cost_allocation_id")
    public Long supersedesCostAllocationId;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
