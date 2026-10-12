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

/**
 * 会计科目。科目代码在账簿内由应用层去重。上级科目是普通可空 Long，不建外键。
 */
@Entity
@Table(name = "accounting_account", indexes = {
        @Index(name = "idx_account_ledger", columnList = "ledger_id"),
        @Index(name = "idx_account_parent", columnList = "parent_account_id"),
        @Index(name = "idx_account_code", columnList = "account_code")
})
@Getter
public class AccountingAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "ledger_id", nullable = false)
    public Long ledgerId;

    @Column(name = "account_code", nullable = false, length = 64)
    public String accountCode;

    @Column(name = "account_name", nullable = false, length = 200)
    public String accountName;

    @Column(name = "parent_account_id")
    public Long parentAccountId;

    @Column(name = "account_category", nullable = false, length = 32)
    public String accountCategory;

    @Column(name = "normal_balance_direction", nullable = false, length = 8)
    public String normalBalanceDirection;

    @Column(name = "is_postable", nullable = false)
    public boolean postable;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
