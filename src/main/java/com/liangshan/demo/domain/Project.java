package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * 项目是管理和成本归集对象。客户在公司、法人、自然人中最多一个，由应用层校验。
 * 关联 ID 是普通 Long，不建外键。
 */
@Entity
@Table(name = "project", indexes = {
        @Index(name = "idx_project_code", columnList = "project_code"),
        @Index(name = "idx_project_owner_company", columnList = "owner_company_id"),
        @Index(name = "idx_project_customer_company", columnList = "customer_company_id"),
        @Index(name = "idx_project_customer_legal", columnList = "customer_legal_person_id"),
        @Index(name = "idx_project_customer_person", columnList = "customer_person_id"),
        @Index(name = "idx_project_lead_department", columnList = "lead_department_id"),
        @Index(name = "idx_project_manager", columnList = "project_manager_employee_id"),
        @Index(name = "idx_project_approved_budget", columnList = "approved_budget_version_id")
})
@Getter
public class Project extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_code", nullable = false, length = 64)
    public String projectCode;

    @Column(name = "name", nullable = false, length = 200)
    public String name;

    @Column(name = "project_type", nullable = false, length = 32)
    public String projectType;

    @Column(name = "project_scope_description", length = 1000)
    public String projectScopeDescription;

    @Column(name = "actual_start_at")
    public LocalDate actualStartAt;

    @Column(name = "actual_completed_at")
    public LocalDate actualCompletedAt;

    @Column(name = "closed_at")
    public LocalDate closedAt;

    @Column(name = "budget_currency", length = 8)
    public String budgetCurrency;

    @Column(name = "approved_budget_version_id")
    public Long approvedBudgetVersionId;

    @Column(name = "customer_company_id")
    public Long customerCompanyId;

    @Column(name = "customer_legal_person_id")
    public Long customerLegalPersonId;

    @Column(name = "customer_person_id")
    public Long customerPersonId;

    @Column(name = "owner_company_id", nullable = false)
    public Long ownerCompanyId;

    @Column(name = "lead_department_id")
    public Long leadDepartmentId;

    @Column(name = "project_manager_employee_id")
    public Long projectManagerEmployeeId;

    @Column(name = "planned_start_at", nullable = false)
    public LocalDate plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    public LocalDate plannedEndAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
