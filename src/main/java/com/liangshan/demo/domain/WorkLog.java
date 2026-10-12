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
 * 工时记录。批准时把当时的成本率主键和单价抄到本行，之后改成本率不回写这笔金额。
 * 关联 ID 是普通 Long，不建外键。成本归集对象在后续费用包里再记。
 */
@Entity
@Table(name = "work_log", indexes = {
        @Index(name = "idx_work_log_employee", columnList = "employee_id"),
        @Index(name = "idx_work_log_employment", columnList = "employment_id"),
        @Index(name = "idx_work_log_assignment", columnList = "department_assignment_id"),
        @Index(name = "idx_work_log_project", columnList = "project_id"),
        @Index(name = "idx_work_log_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_work_log_approver", columnList = "approved_by_employee_id"),
        @Index(name = "idx_work_log_supersedes", columnList = "supersedes_work_log_id"),
        @Index(name = "idx_work_log_rate", columnList = "labor_cost_rate_id")
})
@Getter
public class WorkLog extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    @Column(name = "employment_id", nullable = false)
    public Long employmentId;

    @Column(name = "department_assignment_id")
    public Long departmentAssignmentId;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "work_date", nullable = false)
    public LocalDate workDate;

    @Column(name = "hours", nullable = false, precision = 8, scale = 2)
    public BigDecimal hours;

    @Column(name = "work_description", nullable = false, length = 1000)
    public String workDescription;

    @Column(name = "is_billable", nullable = false)
    public boolean billable;

    @Column(name = "submitted_at")
    public LocalDateTime submittedAt;

    @Column(name = "approval_status", nullable = false, length = 32)
    public String approvalStatus;

    @Column(name = "record_status", nullable = false, length = 32)
    public String recordStatus;

    @Column(name = "approved_by_employee_id")
    public Long approvedByEmployeeId;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(name = "approval_comment", length = 1000)
    public String approvalComment;

    @Column(name = "supersedes_work_log_id")
    public Long supersedesWorkLogId;

    @Column(name = "labor_cost_rate_id")
    public Long laborCostRateId;

    @Column(name = "hourly_cost_amount_snapshot", precision = 18, scale = 4)
    public BigDecimal hourlyCostAmountSnapshot;

    @Column(name = "labor_cost_amount", precision = 18, scale = 2)
    public BigDecimal laborCostAmount;

    @Column(name = "labor_cost_currency", length = 8)
    public String laborCostCurrency;
}
