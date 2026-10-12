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
 * 记账凭证。已过账后不能改，只能另做冲销凭证。关联 ID 不建外键。
 */
@Entity
@Table(name = "journal_entry", indexes = {
        @Index(name = "idx_journal_ledger", columnList = "ledger_id"),
        @Index(name = "idx_journal_period", columnList = "accounting_period_id"),
        @Index(name = "idx_journal_voucher", columnList = "voucher_number"),
        @Index(name = "idx_journal_reversal", columnList = "reversal_of_entry_id")
})
@Getter
public class JournalEntry extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "ledger_id", nullable = false)
    public Long ledgerId;

    @Column(name = "accounting_period_id", nullable = false)
    public Long accountingPeriodId;

    @Column(name = "voucher_number", nullable = false, length = 64)
    public String voucherNumber;

    @Column(name = "voucher_date", nullable = false)
    public LocalDate voucherDate;

    @Column(name = "entry_description", nullable = false, length = 500)
    public String entryDescription;

    @Column(name = "prepared_by_employee_id", nullable = false)
    public Long preparedByEmployeeId;

    @Column(name = "reviewed_by_employee_id")
    public Long reviewedByEmployeeId;

    @Column(name = "reviewed_at")
    public LocalDateTime reviewedAt;

    @Column(name = "posted_by_employee_id")
    public Long postedByEmployeeId;

    @Column(name = "posted_at")
    public LocalDateTime postedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "posting_batch_number", length = 64)
    public String postingBatchNumber;

    @Column(name = "posting_request_number", length = 64)
    public String postingRequestNumber;

    @Column(name = "posting_policy_code", length = 64)
    public String postingPolicyCode;

    @Column(name = "posting_policy_version", length = 32)
    public String postingPolicyVersion;

    @Column(name = "adjustment_reason", length = 500)
    public String adjustmentReason;

    @Column(name = "reversal_of_entry_id")
    public Long reversalOfEntryId;
}
