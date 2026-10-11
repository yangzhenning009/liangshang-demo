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
import java.time.LocalDateTime;

@Entity
@Table(name = "company_registration", indexes = {
        @Index(name = "idx_company_reg_company_id", columnList = "company_id"),
        @Index(name = "idx_company_reg_uscc", columnList = "unified_social_credit_code"),
        @Index(name = "idx_company_reg_blrn", columnList = "business_license_registration_number"),
        @Index(name = "idx_company_reg_tin", columnList = "taxpayer_identification_number"),
        @Index(name = "idx_company_reg_manager", columnList = "branch_manager_person_id")
})
@Getter
public class CompanyRegistration extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "company_id", nullable = false)
    public Long companyId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    @Column(name = "registration_type", nullable = false, length = 50)
    public String registrationType;

    @Column(name = "unified_social_credit_code", length = 32)
    public String unifiedSocialCreditCode;

    @Column(name = "business_license_registration_number", length = 64)
    public String businessLicenseRegistrationNumber;

    @Column(name = "taxpayer_identification_number", length = 32)
    public String taxpayerIdentificationNumber;

    @Column(name = "registration_authority", nullable = false, length = 200)
    public String registrationAuthority;

    @Column(name = "registration_jurisdiction", nullable = false, length = 100)
    public String registrationJurisdiction;

    @Column(name = "registered_name", nullable = false, length = 200)
    public String registeredName;

    @Column(name = "registered_address", nullable = false, length = 500)
    public String registeredAddress;

    @Column(name = "registration_status", nullable = false, length = 32)
    public String registrationStatus;

    @Column(name = "registered_at", nullable = false)
    public LocalDate registeredAt;

    @Column(name = "business_scope", length = 4000)
    public String businessScope;

    @Column(name = "registered_capital_amount", precision = 18, scale = 2)
    public BigDecimal registeredCapitalAmount;

    @Column(name = "registered_capital_currency", length = 8)
    public String registeredCapitalCurrency;

    @Column(name = "business_term_start_date")
    public LocalDate businessTermStartDate;

    @Column(name = "business_term_end_date")
    public LocalDate businessTermEndDate;

    @Column(name = "is_business_term_indefinite")
    public Boolean businessTermIndefinite;

    @Column(name = "branch_manager_person_id")
    public Long branchManagerPersonId;

    @Column(name = "business_license_document_id")
    public Long businessLicenseDocumentId;

    @Column(name = "tax_information_source_document_id")
    public Long taxInformationSourceDocumentId;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
