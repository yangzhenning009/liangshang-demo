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
 * 原始费用或人工成本。金额只记一次。工时批准时把成本率主键和单价抄到本行。
 * 关联 ID 是普通 Long，不建外键。
 */
@Entity
@Table(name = "cost_occurrence", indexes = {
        @Index(name = "idx_cost_bearing_company", columnList = "bearing_company_id"),
        @Index(name = "idx_cost_bearing_legal", columnList = "bearing_legal_person_id"),
        @Index(name = "idx_cost_employee", columnList = "incurred_by_employee_id"),
        @Index(name = "idx_cost_supplier", columnList = "supplier_id"),
        @Index(name = "idx_cost_number", columnList = "cost_number"),
        @Index(name = "idx_cost_contract", columnList = "purchase_contract_id"),
        @Index(name = "idx_cost_work_log", columnList = "work_log_id"),
        @Index(name = "idx_cost_rate", columnList = "labor_cost_rate_id"),
        @Index(name = "idx_cost_supersedes", columnList = "supersedes_cost_occurrence_id"),
        @Index(name = "idx_cost_document", columnList = "source_document_id")
})
@Getter
public class CostOccurrence extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "bearing_company_id")
    public Long bearingCompanyId;

    @Column(name = "bearing_legal_person_id")
    public Long bearingLegalPersonId;

    @Column(name = "incurred_by_employee_id")
    public Long incurredByEmployeeId;

    @Column(name = "supplier_company_id")
    public Long supplierCompanyId;

    @Column(name = "supplier_legal_person_id")
    public Long supplierLegalPersonId;

    @Column(name = "supplier_person_id")
    public Long supplierPersonId;

    @Column(name = "supplier_id")
    public Long supplierId;

    @Column(name = "cost_number", nullable = false, length = 64)
    public String costNumber;

    @Column(name = "purchase_contract_id")
    public Long purchaseContractId;

    @Column(name = "expense_start_date")
    public LocalDate expenseStartDate;

    @Column(name = "expense_end_date")
    public LocalDate expenseEndDate;

    @Column(name = "expense_net_amount", precision = 18, scale = 2)
    public BigDecimal expenseNetAmount;

    @Column(name = "expense_tax_amount", precision = 18, scale = 2)
    public BigDecimal expenseTaxAmount;

    @Column(name = "expense_gross_amount", precision = 18, scale = 2)
    public BigDecimal expenseGrossAmount;

    @Column(name = "employee_advanced_amount", precision = 18, scale = 2)
    public BigDecimal employeeAdvancedAmount;

    @Column(name = "cost_policy_code", length = 64)
    public String costPolicyCode;

    @Column(name = "cost_policy_version", length = 32)
    public String costPolicyVersion;

    @Column(name = "cost_type", nullable = false, length = 32)
    public String costType;

    @Column(name = "incurred_at", nullable = false)
    public LocalDateTime incurredAt;

    @Column(name = "cost_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal costAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "cost_basis", nullable = false, length = 32)
    public String costBasis;

    @Column(name = "work_log_id")
    public Long workLogId;

    @Column(name = "labor_cost_rate_id")
    public Long laborCostRateId;

    @Column(name = "costed_hours", precision = 8, scale = 2)
    public BigDecimal costedHours;

    @Column(name = "hourly_cost_amount_snapshot", precision = 18, scale = 4)
    public BigDecimal hourlyCostAmountSnapshot;

    @Column(name = "payroll_employee_id")
    public Long payrollEmployeeId;

    @Column(name = "payroll_employment_id")
    public Long payrollEmploymentId;

    @Column(name = "payroll_period", length = 32)
    public String payrollPeriod;

    @Column(name = "payroll_batch_number", length = 64)
    public String payrollBatchNumber;

    @Column(name = "payroll_line_number")
    public Integer payrollLineNumber;

    @Column(name = "approval_status", nullable = false, length = 32)
    public String approvalStatus;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "supersedes_cost_occurrence_id")
    public Long supersedesCostOccurrenceId;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    @Column(name = "description", length = 1000)
    public String description;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
