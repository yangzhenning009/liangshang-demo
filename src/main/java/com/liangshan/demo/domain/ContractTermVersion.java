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

/**
 * 条款版本。agreed_valid_from / agreed_valid_to 是约定的合同生效与结束日；
 * valid_from / valid_to 是该版本自身的有效期间（左闭右开）。原始条款的 amendment_id 为空。
 */
@Entity
@Table(name = "contract_term_version", indexes = {
        @Index(name = "idx_contract_term_contract", columnList = "contract_id"),
        @Index(name = "idx_contract_term_amendment", columnList = "amendment_id"),
        @Index(name = "idx_contract_term_supplier", columnList = "supplier_id"),
        @Index(name = "idx_contract_term_buyer_company", columnList = "buyer_company_id"),
        @Index(name = "idx_contract_term_seller_company", columnList = "seller_company_id")
})
@Getter
public class ContractTermVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    @Column(name = "amendment_id")
    public Long amendmentId;

    @Column(name = "buyer_company_id")
    public Long buyerCompanyId;

    @Column(name = "buyer_legal_person_id")
    public Long buyerLegalPersonId;

    @Column(name = "buyer_person_id")
    public Long buyerPersonId;

    @Column(name = "seller_company_id")
    public Long sellerCompanyId;

    @Column(name = "seller_legal_person_id")
    public Long sellerLegalPersonId;

    @Column(name = "seller_person_id")
    public Long sellerPersonId;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "pricing_method", nullable = false, length = 32)
    public String pricingMethod;

    @Column(name = "contract_net_amount", precision = 18, scale = 2)
    public BigDecimal contractNetAmount;

    @Column(name = "contract_tax_amount", precision = 18, scale = 2)
    public BigDecimal contractTaxAmount;

    @Column(name = "contract_gross_amount", precision = 18, scale = 2)
    public BigDecimal contractGrossAmount;

    @Column(name = "buyer_name_snapshot", nullable = false, length = 200)
    public String buyerNameSnapshot;

    @Column(name = "seller_name_snapshot", nullable = false, length = 200)
    public String sellerNameSnapshot;

    @Column(name = "buyer_taxpayer_id_snapshot", length = 64)
    public String buyerTaxpayerIdSnapshot;

    @Column(name = "seller_taxpayer_id_snapshot", length = 64)
    public String sellerTaxpayerIdSnapshot;

    @Column(name = "buyer_signatory_person_id")
    public Long buyerSignatoryPersonId;

    @Column(name = "seller_signatory_person_id")
    public Long sellerSignatoryPersonId;

    @Column(name = "buyer_contact_person_id")
    public Long buyerContactPersonId;

    @Column(name = "seller_contact_person_id")
    public Long sellerContactPersonId;

    @Column(name = "procurement_company_id")
    public Long procurementCompanyId;

    @Column(name = "supplier_id")
    public Long supplierId;

    @Column(name = "signed_at")
    public LocalDate signedAt;

    @Column(name = "termination_date")
    public LocalDate terminationDate;

    @Column(name = "termination_reason", length = 500)
    public String terminationReason;

    @Column(name = "agreed_valid_from", nullable = false)
    public LocalDate agreedValidFrom;

    @Column(name = "agreed_valid_to")
    public LocalDate agreedValidTo;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;
}
