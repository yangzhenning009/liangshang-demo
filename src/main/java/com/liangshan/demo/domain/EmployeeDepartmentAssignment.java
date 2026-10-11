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
 * 部门任职。期间必须落在同一员工的雇佣有效期内，由应用层校验。
 */
@Entity
@Table(name = "employee_department_assignment", indexes = {
        @Index(name = "idx_assignment_employee_id", columnList = "employee_id"),
        @Index(name = "idx_assignment_employment_id", columnList = "employment_id"),
        @Index(name = "idx_assignment_department_id", columnList = "department_id")
})
@Getter
public class EmployeeDepartmentAssignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    @Column(name = "employment_id", nullable = false)
    public Long employmentId;

    @Column(name = "department_id", nullable = false)
    public Long departmentId;

    @Column(name = "assignment_type", nullable = false, length = 32)
    public String assignmentType;

    @Column(name = "position_title", length = 100)
    public String positionTitle;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
