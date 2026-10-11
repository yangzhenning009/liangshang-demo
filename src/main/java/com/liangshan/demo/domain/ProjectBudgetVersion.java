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

@Entity
@Table(name = "project_budget_version", indexes = {
        @Index(name = "idx_project_budget_project_id", columnList = "project_id"),
        @Index(name = "idx_project_budget_approver", columnList = "approved_by_employee_id"),
        @Index(name = "idx_project_budget_document", columnList = "basis_document_id")
})
@Getter
public class ProjectBudgetVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    @Column(name = "budget_currency", length = 8)
    public String budgetCurrency;

    @Column(name = "planned_labor_hours", precision = 12, scale = 2)
    public BigDecimal plannedLaborHours;

    @Column(name = "labor_cost_budget_amount", precision = 18, scale = 2)
    public BigDecimal laborCostBudgetAmount;

    @Column(name = "nonlabor_cost_budget_amount", precision = 18, scale = 2)
    public BigDecimal nonlaborCostBudgetAmount;

    @Column(name = "budget_cost_basis", length = 32)
    public String budgetCostBasis;

    @Column(name = "approval_status", nullable = false, length = 32)
    public String approvalStatus;

    @Column(name = "submitted_at")
    public LocalDateTime submittedAt;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
