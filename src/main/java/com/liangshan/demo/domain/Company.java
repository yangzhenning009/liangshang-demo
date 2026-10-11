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

/**
 * 公司与分公司共用本表。legal_person_id、head_company_id 是普通可空 Long，不建外键。
 * 证照与税号的当前值来自未结束的 company_registration，由应用层在同一事务写入。
 */
@Entity
@Table(name = "company", indexes = {
        @Index(name = "idx_company_company_code", columnList = "company_code"),
        @Index(name = "idx_company_legal_person_id", columnList = "legal_person_id"),
        @Index(name = "idx_company_head_company_id", columnList = "head_company_id"),
        @Index(name = "idx_company_uscc", columnList = "unified_social_credit_code"),
        @Index(name = "idx_company_blrn", columnList = "business_license_registration_number"),
        @Index(name = "idx_company_tin", columnList = "taxpayer_identification_number"),
        @Index(name = "idx_company_branch_manager", columnList = "branch_manager_person_id")
})
@Getter
public class Company extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "company_code", nullable = false, length = 64)
    public String companyCode;

    @Column(name = "name", nullable = false, length = 200)
    public String name;

    @Column(name = "company_type", nullable = false, length = 32)
    public String companyType;

    @Column(name = "unified_social_credit_code", length = 32)
    public String unifiedSocialCreditCode;

    @Column(name = "business_license_registration_number", length = 64)
    public String businessLicenseRegistrationNumber;

    @Column(name = "taxpayer_identification_number", length = 32)
    public String taxpayerIdentificationNumber;

    @Column(name = "registered_name", length = 200)
    public String registeredName;

    @Column(name = "registered_address", length = 500)
    public String registeredAddress;

    @Column(name = "registration_jurisdiction", length = 100)
    public String registrationJurisdiction;

    @Column(name = "business_license_document_id")
    public Long businessLicenseDocumentId;

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

    @Column(name = "branch_manager_person_id")
    public Long branchManagerPersonId;

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

    @Column(name = "established_at")
    public LocalDate establishedAt;

    @Column(name = "terminated_at")
    public LocalDate terminatedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
