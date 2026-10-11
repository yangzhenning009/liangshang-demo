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

@Entity
@Table(name = "project_member", indexes = {
        @Index(name = "idx_project_member_project_id", columnList = "project_id"),
        @Index(name = "idx_project_member_employee_id", columnList = "employee_id"),
        @Index(name = "idx_project_member_employment_id", columnList = "employment_id"),
        @Index(name = "idx_project_member_assignment_id", columnList = "department_assignment_id")
})
@Getter
public class ProjectMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    @Column(name = "employment_id", nullable = false)
    public Long employmentId;

    @Column(name = "department_assignment_id")
    public Long departmentAssignmentId;

    @Column(name = "project_role", nullable = false, length = 32)
    public String projectRole;

    @Column(name = "planned_hours", precision = 12, scale = 2)
    public BigDecimal plannedHours;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;
}
