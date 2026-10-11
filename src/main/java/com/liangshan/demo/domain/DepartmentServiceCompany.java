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
@Table(name = "department_service_company", indexes = {
        @Index(name = "idx_dept_service_department_id", columnList = "department_id"),
        @Index(name = "idx_dept_service_company_id", columnList = "service_company_id")
})
@Getter
public class DepartmentServiceCompany extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "department_id", nullable = false)
    public Long departmentId;

    @Column(name = "service_company_id", nullable = false)
    public Long serviceCompanyId;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
