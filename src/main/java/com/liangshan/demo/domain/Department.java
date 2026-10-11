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

@Entity
@Table(name = "department", indexes = {
        @Index(name = "idx_department_company_id", columnList = "company_id"),
        @Index(name = "idx_department_parent_id", columnList = "parent_department_id"),
        @Index(name = "idx_department_code", columnList = "department_code"),
        @Index(name = "idx_department_head", columnList = "department_head_employee_id")
})
@Getter
public class Department extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "company_id", nullable = false)
    public Long companyId;

    @Column(name = "parent_department_id")
    public Long parentDepartmentId;

    @Column(name = "department_code", nullable = false, length = 64)
    public String departmentCode;

    @Column(name = "name", nullable = false, length = 200)
    public String name;

    @Column(name = "department_type", nullable = false, length = 50)
    public String departmentType;

    @Column(name = "department_head_employee_id")
    public Long departmentHeadEmployeeId;

    @Column(name = "department_contact_phone", length = 40)
    public String departmentContactPhone;

    @Column(name = "department_contact_email", length = 200)
    public String departmentContactEmail;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
