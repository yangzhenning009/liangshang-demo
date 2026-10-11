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

@Entity
@Table(name = "project_version", indexes = {
        @Index(name = "idx_project_version_project_id", columnList = "project_id"),
        @Index(name = "idx_project_version_owner", columnList = "owner_company_id"),
        @Index(name = "idx_project_version_customer_company", columnList = "customer_company_id"),
        @Index(name = "idx_project_version_document", columnList = "basis_document_id")
})
@Getter
public class ProjectVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

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

    @Column(name = "project_type", nullable = false, length = 32)
    public String projectType;

    @Column(name = "project_scope_description", length = 1000)
    public String projectScopeDescription;

    @Column(name = "name", nullable = false, length = 200)
    public String name;

    @Column(name = "planned_start_at", nullable = false)
    public LocalDate plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    public LocalDate plannedEndAt;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
