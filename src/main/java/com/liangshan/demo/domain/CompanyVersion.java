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
@Table(name = "company_version", indexes = {
        @Index(name = "idx_company_version_company_id", columnList = "company_id"),
        @Index(name = "idx_company_version_legal_person_id", columnList = "legal_person_id"),
        @Index(name = "idx_company_version_head_company_id", columnList = "head_company_id")
})
@Getter
public class CompanyVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "company_id", nullable = false)
    public Long companyId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    @Column(name = "company_code", nullable = false, length = 64)
    public String companyCode;

    @Column(name = "name", nullable = false, length = 200)
    public String name;

    @Column(name = "company_type", nullable = false, length = 32)
    public String companyType;

    @Column(name = "legal_person_id")
    public Long legalPersonId;

    @Column(name = "head_company_id")
    public Long headCompanyId;

    @Column(name = "company_form", length = 50)
    public String companyForm;

    @Column(name = "contact_phone", length = 40)
    public String contactPhone;

    @Column(name = "contact_email", length = 200)
    public String contactEmail;

    @Column(name = "business_address", length = 500)
    public String businessAddress;

    @Column(name = "established_at")
    public LocalDate establishedAt;

    @Column(name = "terminated_at")
    public LocalDate terminatedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
