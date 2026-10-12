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
 * 应付项。员工报销、采购和合同计划确认都写在这张表。关联 ID 不建外键。
 */
@Entity
@Table(name = "payable_item", indexes = {
        @Index(name = "idx_payable_number", columnList = "payable_number"),
        @Index(name = "idx_payable_debtor_company", columnList = "debtor_company_id"),
        @Index(name = "idx_payable_debtor_legal", columnList = "debtor_legal_person_id"),
        @Index(name = "idx_payable_creditor_company", columnList = "creditor_company_id"),
        @Index(name = "idx_payable_creditor_legal", columnList = "creditor_legal_person_id"),
        @Index(name = "idx_payable_creditor_person", columnList = "creditor_person_id"),
        @Index(name = "idx_payable_supplier", columnList = "supplier_id"),
        @Index(name = "idx_payable_cost", columnList = "cost_occurrence_id"),
        @Index(name = "idx_payable_claim", columnList = "expense_claim_id"),
        @Index(name = "idx_payable_document", columnList = "source_document_id")
})
@Getter
public class PayableItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "payable_number", nullable = false, length = 64)
    public String payableNumber;

    @Column(name = "debtor_company_id")
    public Long debtorCompanyId;

    @Column(name = "debtor_legal_person_id")
    public Long debtorLegalPersonId;

    @Column(name = "creditor_company_id")
    public Long creditorCompanyId;

    @Column(name = "creditor_legal_person_id")
    public Long creditorLegalPersonId;

    @Column(name = "creditor_person_id")
    public Long creditorPersonId;

    @Column(name = "supplier_id")
    public Long supplierId;

    @Column(name = "contract_id")
    public Long contractId;

    @Column(name = "contract_payment_schedule_id")
    public Long contractPaymentScheduleId;

    @Column(name = "project_id")
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "cost_occurrence_id")
    public Long costOccurrenceId;

    @Column(name = "expense_claim_id")
    public Long expenseClaimId;

    @Column(name = "payable_purpose", nullable = false, length = 32)
    public String payablePurpose;

    @Column(name = "payable_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal payableAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "confirmed_at")
    public LocalDateTime confirmedAt;

    @Column(name = "confirmed_by_employee_id")
    public Long confirmedByEmployeeId;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "trigger_acceptance_event_id")
    public Long triggerAcceptanceEventId;

    @Column(name = "trigger_invoice_id")
    public Long triggerInvoiceId;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
