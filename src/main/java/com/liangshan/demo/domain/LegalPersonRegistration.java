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
 * 非公司法人的登记资料。公司与分公司的证照只放在 company_registration。
 */
@Entity
@Table(name = "legal_person_registration", indexes = {
        @Index(name = "idx_lp_reg_legal_person_id", columnList = "legal_person_id"),
        @Index(name = "idx_lp_reg_uscc", columnList = "unified_social_credit_code"),
        @Index(name = "idx_lp_reg_cert", columnList = "registration_certificate_number"),
        @Index(name = "idx_lp_reg_tin", columnList = "taxpayer_identification_number")
})
@Getter
public class LegalPersonRegistration extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "legal_person_id", nullable = false)
    public Long legalPersonId;

    @Column(name = "registration_type", nullable = false, length = 50)
    public String registrationType;

    @Column(name = "unified_social_credit_code", length = 32)
    public String unifiedSocialCreditCode;

    @Column(name = "registration_certificate_number", length = 64)
    public String registrationCertificateNumber;

    @Column(name = "taxpayer_identification_number", length = 32)
    public String taxpayerIdentificationNumber;

    @Column(name = "registration_authority", nullable = false, length = 200)
    public String registrationAuthority;

    @Column(name = "jurisdiction", nullable = false, length = 100)
    public String jurisdiction;

    @Column(name = "registered_name", nullable = false, length = 200)
    public String registeredName;

    @Column(name = "registered_address", nullable = false, length = 500)
    public String registeredAddress;

    @Column(name = "registration_status", nullable = false, length = 32)
    public String registrationStatus;

    @Column(name = "registered_at", nullable = false)
    public LocalDate registeredAt;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
