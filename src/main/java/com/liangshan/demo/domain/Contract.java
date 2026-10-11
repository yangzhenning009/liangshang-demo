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
 * 合同主记录展示当前条款。买方、卖方各自恰好一个对象，由应用层校验。
 * 关联 ID 是普通 Long，不建外键。
 */
@Entity
@Table(name = "contract", indexes = {
        @Index(name = "idx_contract_number", columnList = "contract_number"),
        @Index(name = "idx_contract_buyer_company", columnList = "buyer_company_id"),
        @Index(name = "idx_contract_seller_company", columnList = "seller_company_id"),
        @Index(name = "idx_contract_supplier", columnList = "supplier_id"),
        @Index(name = "idx_contract_procurement_company", columnList = "procurement_company_id"),
        @Index(name = "idx_contract_source_document", columnList = "source_document_id")
})
@Getter
public class Contract extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_number", nullable = false, length = 64)
    public String contractNumber;

    @Column(name = "contract_type", nullable = false, length = 32)
    public String contractType;

    @Column(name = "contract_title", nullable = false, length = 200)
    public String contractTitle;

    @Column(name = "managing_company_id")
    public Long managingCompanyId;

    @Column(name = "procurement_company_id")
    public Long procurementCompanyId;

    @Column(name = "supplier_id")
    public Long supplierId;

    @Column(name = "contract_owner_employee_id")
    public Long contractOwnerEmployeeId;

    @Column(name = "buyer_signatory_person_id")
    public Long buyerSignatoryPersonId;

    @Column(name = "seller_signatory_person_id")
    public Long sellerSignatoryPersonId;

    @Column(name = "buyer_name_snapshot", nullable = false, length = 200)
    public String buyerNameSnapshot;

    @Column(name = "seller_name_snapshot", nullable = false, length = 200)
    public String sellerNameSnapshot;

    @Column(name = "buyer_taxpayer_id_snapshot", length = 64)
    public String buyerTaxpayerIdSnapshot;

    @Column(name = "seller_taxpayer_id_snapshot", length = 64)
    public String sellerTaxpayerIdSnapshot;

    @Column(name = "buyer_contact_person_id")
    public Long buyerContactPersonId;

    @Column(name = "seller_contact_person_id")
    public Long sellerContactPersonId;

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

    @Column(name = "signed_at")
    public LocalDate signedAt;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

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

    @Column(name = "approval_status", nullable = false, length = 32)
    public String approvalStatus;

    @Column(name = "contract_approved_at")
    public LocalDateTime contractApprovedAt;

    @Column(name = "contract_approved_by_employee_id")
    public Long contractApprovedByEmployeeId;

    @Column(name = "termination_date")
    public LocalDate terminationDate;

    @Column(name = "termination_reason", length = 500)
    public String terminationReason;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
