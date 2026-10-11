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

@Entity
@Table(name = "company_shareholding", indexes = {
        @Index(name = "idx_share_company_id", columnList = "company_id"),
        @Index(name = "idx_share_shareholder_company_id", columnList = "shareholder_company_id"),
        @Index(name = "idx_share_shareholder_legal_person_id", columnList = "shareholder_legal_person_id"),
        @Index(name = "idx_share_shareholder_person_id", columnList = "shareholder_person_id")
})
@Getter
public class CompanyShareholding extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "company_id", nullable = false)
    public Long companyId;

    @Column(name = "shareholder_company_id")
    public Long shareholderCompanyId;

    @Column(name = "shareholder_legal_person_id")
    public Long shareholderLegalPersonId;

    @Column(name = "shareholder_person_id")
    public Long shareholderPersonId;

    @Column(name = "share_class", nullable = false, length = 50)
    public String shareClass;

    @Column(name = "ownership_ratio", nullable = false, precision = 10, scale = 6)
    public BigDecimal ownershipRatio;

    @Column(name = "subscribed_capital_amount", precision = 18, scale = 2)
    public BigDecimal subscribedCapitalAmount;

    @Column(name = "currency", length = 8)
    public String currency;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
