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
 * 工时记录。批准后按当时的成本率生成 cost_occurrence，单价快照记在成本上。
 * 关联 ID 是普通 Long，不建外键。
 */
@Entity
@Table(name = "work_log", indexes = {
        @Index(name = "idx_work_log_employee", columnList = "employee_id"),
        @Index(name = "idx_work_log_employment", columnList = "employment_id"),
        @Index(name = "idx_work_log_assignment", columnList = "department_assignment_id"),
        @Index(name = "idx_work_log_project", columnList = "project_id"),
        @Index(name = "idx_work_log_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_work_log_approver", columnList = "approved_by_employee_id"),
        @Index(name = "idx_work_log_supersedes", columnList = "supersedes_work_log_id")
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
}
