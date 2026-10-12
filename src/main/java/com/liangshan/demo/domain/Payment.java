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
 * 付款的最小实现，只用于报销付到员工本人。银行快照和完整收付页面留在后续包。
 * 付款不生成新的成本。
 */
@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_number", columnList = "payment_number"),
        @Index(name = "idx_payment_payer_company", columnList = "payer_company_id"),
        @Index(name = "idx_payment_payee_person", columnList = "payee_person_id"),
        @Index(name = "idx_payment_employee", columnList = "related_employee_id"),
        @Index(name = "idx_payment_refund", columnList = "refund_of_payment_id")
})
@Getter
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "payer_company_id")
    public Long payerCompanyId;

    @Column(name = "payer_legal_person_id")
    public Long payerLegalPersonId;

    @Column(name = "payer_person_id")
    public Long payerPersonId;

    @Column(name = "payee_company_id")
    public Long payeeCompanyId;

    @Column(name = "payee_legal_person_id")
    public Long payeeLegalPersonId;

    @Column(name = "payee_person_id")
    public Long payeePersonId;

    @Column(name = "payment_number", nullable = false, length = 64)
    public String paymentNumber;

    @Column(name = "payment_kind", nullable = false, length = 32)
    public String paymentKind;

    @Column(name = "payment_purpose", nullable = false, length = 32)
    public String paymentPurpose;

    @Column(name = "related_employee_id")
    public Long relatedEmployeeId;

    @Column(name = "refund_of_payment_id")
    public Long refundOfPaymentId;

    @Column(name = "paid_at", nullable = false)
    public LocalDateTime paidAt;

    @Column(name = "payment_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal paymentAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "payment_channel", nullable = false, length = 32)
    public String paymentChannel;

    @Column(name = "confirmed_by_employee_id")
    public Long confirmedByEmployeeId;

    @Column(name = "confirmed_at")
    public LocalDateTime confirmedAt;

    @Column(name = "bank_value_date")
    public LocalDate bankValueDate;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
