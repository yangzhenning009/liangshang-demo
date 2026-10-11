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
@Table(name = "project_delivery_company", indexes = {
        @Index(name = "idx_project_delivery_project_id", columnList = "project_id"),
        @Index(name = "idx_project_delivery_company_id", columnList = "delivery_company_id"),
        @Index(name = "idx_project_delivery_department", columnList = "lead_department_id"),
        @Index(name = "idx_project_delivery_document", columnList = "basis_document_id")
})
@Getter
public class ProjectDeliveryCompany extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "delivery_company_id", nullable = false)
    public Long deliveryCompanyId;

    @Column(name = "lead_department_id")
    public Long leadDepartmentId;

    @Column(name = "responsibility_description", nullable = false, length = 500)
    public String responsibilityDescription;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
