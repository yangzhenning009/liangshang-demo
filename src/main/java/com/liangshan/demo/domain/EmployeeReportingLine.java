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
 * 汇报线。任职双方期间与行政链无环由应用层校验。职能汇报不并入行政链。
 */
@Entity
@Table(name = "employee_reporting_line", indexes = {
        @Index(name = "idx_reporting_employee_id", columnList = "employee_id"),
        @Index(name = "idx_reporting_assignment_id", columnList = "assignment_id"),
        @Index(name = "idx_reporting_manager_assignment_id", columnList = "manager_assignment_id")
})
@Getter
public class EmployeeReportingLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    @Column(name = "assignment_id", nullable = false)
    public Long assignmentId;

    @Column(name = "manager_assignment_id", nullable = false)
    public Long managerAssignmentId;

    @Column(name = "reporting_type", nullable = false, length = 32)
    public String reportingType;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;
}
