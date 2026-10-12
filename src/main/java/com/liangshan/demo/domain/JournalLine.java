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

/**
 * 凭证明细。借贷金额非负，每行只有一边为正。往来对象是普通可空 Long，不建外键。
 */
@Entity
@Table(name = "journal_line", indexes = {
        @Index(name = "idx_journal_line_entry", columnList = "journal_entry_id"),
        @Index(name = "idx_journal_line_account", columnList = "accounting_account_id"),
        @Index(name = "idx_journal_line_project", columnList = "project_id")
})
@Getter
public class JournalLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "journal_entry_id", nullable = false)
    public Long journalEntryId;

    @Column(name = "line_number", nullable = false)
    public Integer lineNumber;

    @Column(name = "accounting_account_id", nullable = false)
    public Long accountingAccountId;

    @Column(name = "account_code_snapshot", nullable = false, length = 64)
    public String accountCodeSnapshot;

    @Column(name = "account_name_snapshot", nullable = false, length = 200)
    public String accountNameSnapshot;

    @Column(name = "line_description", length = 500)
    public String lineDescription;

    @Column(name = "debit_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal debitAmount;

    @Column(name = "credit_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal creditAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "counterparty_company_id")
    public Long counterpartyCompanyId;

    @Column(name = "counterparty_legal_person_id")
    public Long counterpartyLegalPersonId;

    @Column(name = "counterparty_person_id")
    public Long counterpartyPersonId;

    @Column(name = "employee_id")
    public Long employeeId;

    @Column(name = "department_id")
    public Long departmentId;

    @Column(name = "project_id")
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
