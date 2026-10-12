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
 * 应收项。本金只在确认时写入。发票分配和收款核销都不另记一笔应收。关联 ID 不建外键。
 */
@Entity
@Table(name = "receivable_item", indexes = {
        @Index(name = "idx_receivable_number", columnList = "receivable_number"),
        @Index(name = "idx_receivable_creditor_company", columnList = "creditor_company_id"),
        @Index(name = "idx_receivable_creditor_legal", columnList = "creditor_legal_person_id"),
        @Index(name = "idx_receivable_debtor_company", columnList = "debtor_company_id"),
        @Index(name = "idx_receivable_debtor_legal", columnList = "debtor_legal_person_id"),
        @Index(name = "idx_receivable_debtor_person", columnList = "debtor_person_id"),
        @Index(name = "idx_receivable_contract", columnList = "contract_id"),
        @Index(name = "idx_receivable_schedule", columnList = "contract_payment_schedule_id"),
        @Index(name = "idx_receivable_project", columnList = "project_id"),
        @Index(name = "idx_receivable_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_receivable_acceptance", columnList = "trigger_acceptance_event_id"),
        @Index(name = "idx_receivable_invoice", columnList = "trigger_invoice_id"),
        @Index(name = "idx_receivable_document", columnList = "source_document_id")
})
@Getter
public class ReceivableItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "receivable_number", nullable = false, length = 64)
    public String receivableNumber;

    @Column(name = "creditor_company_id")
    public Long creditorCompanyId;

    @Column(name = "creditor_legal_person_id")
    public Long creditorLegalPersonId;

    @Column(name = "debtor_company_id")
    public Long debtorCompanyId;

    @Column(name = "debtor_legal_person_id")
    public Long debtorLegalPersonId;

    @Column(name = "debtor_person_id")
    public Long debtorPersonId;

    @Column(name = "contract_id")
    public Long contractId;

    @Column(name = "contract_payment_schedule_id")
    public Long contractPaymentScheduleId;

    @Column(name = "project_id")
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "receivable_purpose", nullable = false, length = 32)
    public String receivablePurpose;

    @Column(name = "receivable_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal receivableAmount;

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
