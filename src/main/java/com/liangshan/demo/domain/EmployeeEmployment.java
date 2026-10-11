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
import java.time.LocalDateTime;

/**
 * 雇佣记录。用人对象在公司与法人中恰好选一个，工号在该对象内唯一，均由应用层校验。
 */
@Entity
@Table(name = "employee_employment", indexes = {
        @Index(name = "idx_employment_employee_id", columnList = "employee_id"),
        @Index(name = "idx_employment_employer_company", columnList = "employer_company_id"),
        @Index(name = "idx_employment_employer_legal", columnList = "employer_legal_person_id"),
        @Index(name = "idx_employment_employee_number", columnList = "employee_number"),
        @Index(name = "idx_employment_document", columnList = "employment_document_id")
})
@Getter
public class EmployeeEmployment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "employee_id", nullable = false)
    public Long employeeId;

    @Column(name = "employer_company_id")
    public Long employerCompanyId;

    @Column(name = "employer_legal_person_id")
    public Long employerLegalPersonId;

    @Column(name = "employee_number", nullable = false, length = 64)
    public String employeeNumber;

    @Column(name = "employment_type", nullable = false, length = 32)
    public String employmentType;

    @Column(name = "employment_contract_number", length = 64)
    public String employmentContractNumber;

    @Column(name = "employment_start_date", nullable = false)
    public LocalDate employmentStartDate;

    @Column(name = "employment_end_date")
    public LocalDate employmentEndDate;

    @Column(name = "termination_reason", length = 500)
    public String terminationReason;

    @Column(name = "employment_contract_start_date")
    public LocalDate employmentContractStartDate;

    @Column(name = "employment_contract_end_date")
    public LocalDate employmentContractEndDate;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "employment_document_id")
    public Long employmentDocumentId;
}
