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

/**
 * 一个采购公司对一个实际供货对象的档案。关联 ID 是普通 Long，不建外键。
 * 确认时 company_id、legal_person_id、person_id 恰好一个，由应用层校验。
 */
@Entity
@Table(name = "supplier", indexes = {
        @Index(name = "idx_supplier_code", columnList = "supplier_code"),
        @Index(name = "idx_supplier_purchasing_company_id", columnList = "purchasing_company_id"),
        @Index(name = "idx_supplier_company_id", columnList = "company_id"),
        @Index(name = "idx_supplier_legal_person_id", columnList = "legal_person_id"),
        @Index(name = "idx_supplier_person_id", columnList = "person_id"),
        @Index(name = "idx_supplier_owner", columnList = "procurement_owner_employee_id"),
        @Index(name = "idx_supplier_admission_document", columnList = "admission_document_id")
})
@Getter
public class Supplier extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "supplier_code", nullable = false, length = 64)
    public String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 200)
    public String supplierName;

    @Column(name = "purchasing_company_id", nullable = false)
    public Long purchasingCompanyId;

    @Column(name = "company_id")
    public Long companyId;

    @Column(name = "legal_person_id")
    public Long legalPersonId;

    @Column(name = "person_id")
    public Long personId;

    @Column(name = "procurement_owner_employee_id")
    public Long procurementOwnerEmployeeId;

    @Column(name = "admission_status", nullable = false, length = 32)
    public String admissionStatus;

    @Column(name = "admission_submitted_at")
    public LocalDateTime admissionSubmittedAt;

    @Column(name = "admission_reviewed_by_employee_id")
    public Long admissionReviewedByEmployeeId;

    @Column(name = "admission_reviewed_at")
    public LocalDateTime admissionReviewedAt;

    @Column(name = "admission_rejection_reason", length = 500)
    public String admissionRejectionReason;

    @Column(name = "admission_document_id")
    public Long admissionDocumentId;

    @Column(name = "cooperation_status", nullable = false, length = 32)
    public String cooperationStatus;

    @Column(name = "cooperation_started_on")
    public LocalDate cooperationStartedOn;

    @Column(name = "cooperation_ended_on")
    public LocalDate cooperationEndedOn;

    @Column(name = "cooperation_status_effective_at", nullable = false)
    public LocalDateTime cooperationStatusEffectiveAt;

    @Column(name = "cooperation_status_reason", length = 500)
    public String cooperationStatusReason;

    @Column(name = "default_settlement_currency", length = 8)
    public String defaultSettlementCurrency;

    @Column(name = "default_payment_due_basis", length = 32)
    public String defaultPaymentDueBasis;

    @Column(name = "default_payment_term_days")
    public Integer defaultPaymentTermDays;

    @Column(name = "default_payment_day_of_month")
    public Integer defaultPaymentDayOfMonth;
}
