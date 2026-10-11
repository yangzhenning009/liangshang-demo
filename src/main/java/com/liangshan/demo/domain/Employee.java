package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 员工通过 person_id 关联自然人。一对一由应用层维护，数据库不建唯一约束或外键。
 * 雇佣、部门任职、汇报明细属于后续工作包。
 */
@Entity
@Table(name = "employee", indexes = {
        @Index(name = "idx_employee_person_id", columnList = "person_id"),
        @Index(name = "idx_employee_employee_code", columnList = "employee_code")
})
@Getter
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "person_id", nullable = false)
    public Long personId;

    @Column(name = "employee_code", nullable = false, length = 64)
    public String employeeCode;

    @Column(name = "work_email", length = 200)
    public String workEmail;

    @Column(name = "work_phone", length = 40)
    public String workPhone;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
