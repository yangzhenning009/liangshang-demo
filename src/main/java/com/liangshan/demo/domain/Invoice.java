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
 * 发票。开票方和受票方直接记在票上。关联应收不增加应收本金。关联 ID 不建外键。
 */
@Entity
@Table(name = "invoice", indexes = {
        @Index(name = "idx_invoice_number", columnList = "invoice_number"),
        @Index(name = "idx_invoice_issuer_company", columnList = "issuer_company_id"),
        @Index(name = "idx_invoice_issuer_legal", columnList = "issuer_legal_person_id"),
        @Index(name = "idx_invoice_recipient_company", columnList = "recipient_company_id"),
        @Index(name = "idx_invoice_recipient_legal", columnList = "recipient_legal_person_id"),
        @Index(name = "idx_invoice_recipient_person", columnList = "recipient_person_id"),
        @Index(name = "idx_invoice_original", columnList = "original_invoice_id"),
        @Index(name = "idx_invoice_document", columnList = "source_document_id")
})
@Getter
public class Invoice extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "invoice_number", nullable = false, length = 64)
    public String invoiceNumber;

    @Column(name = "invoice_code", length = 64)
    public String invoiceCode;

    @Column(name = "invoice_type", nullable = false, length = 32)
    public String invoiceType;

    @Column(name = "invoice_medium", nullable = false, length = 16)
    public String invoiceMedium;

    @Column(name = "invoice_color", nullable = false, length = 16)
    public String invoiceColor;

    @Column(name = "invoice_net_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal invoiceNetAmount;

    @Column(name = "invoice_tax_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal invoiceTaxAmount;

    @Column(name = "invoice_gross_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal invoiceGrossAmount;

    @Column(name = "original_invoice_id")
    public Long originalInvoiceId;

    @Column(name = "invoice_verified_at")
    public LocalDateTime invoiceVerifiedAt;

    @Column(name = "verified_by_employee_id")
    public Long verifiedByEmployeeId;

    @Column(name = "verification_result", length = 32)
    public String verificationResult;

    @Column(name = "verification_comment", length = 1000)
    public String verificationComment;

    @Column(name = "voided_at")
    public LocalDateTime voidedAt;

    @Column(name = "void_reason", length = 500)
    public String voidReason;

    @Column(name = "issuer_company_id")
    public Long issuerCompanyId;

    @Column(name = "issuer_legal_person_id")
    public Long issuerLegalPersonId;

    @Column(name = "recipient_company_id")
    public Long recipientCompanyId;

    @Column(name = "recipient_legal_person_id")
    public Long recipientLegalPersonId;

    @Column(name = "recipient_person_id")
    public Long recipientPersonId;

    @Column(name = "issuer_name_snapshot", length = 200)
    public String issuerNameSnapshot;

    @Column(name = "issuer_taxpayer_id_snapshot", length = 64)
    public String issuerTaxpayerIdSnapshot;

    @Column(name = "recipient_name_snapshot", length = 200)
    public String recipientNameSnapshot;

    @Column(name = "recipient_taxpayer_id_snapshot", length = 64)
    public String recipientTaxpayerIdSnapshot;

    @Column(name = "issued_at")
    public LocalDate issuedAt;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
