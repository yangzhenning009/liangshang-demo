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
@Table(name = "supplier_version", indexes = {
        @Index(name = "idx_supplier_version_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_supplier_version_basis_document", columnList = "basis_document_id"),
        @Index(name = "idx_supplier_version_changed_by", columnList = "changed_by_employee_id")
})
@Getter
public class SupplierVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "supplier_id", nullable = false)
    public Long supplierId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    @Column(name = "supplier_name", nullable = false, length = 200)
    public String supplierName;

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

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;

    @Column(name = "changed_by_employee_id")
    public Long changedByEmployeeId;

    @Column(name = "change_reason", length = 500)
    public String changeReason;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
