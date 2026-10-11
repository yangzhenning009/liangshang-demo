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

@Entity
@Table(name = "company_bank_account", indexes = {
        @Index(name = "idx_bank_company_id", columnList = "company_id")
})
@Getter
public class CompanyBankAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "company_id", nullable = false)
    public Long companyId;

    @Column(name = "bank_account_name", nullable = false, length = 200)
    public String bankAccountName;

    @Column(name = "bank_account_number", nullable = false, length = 64)
    public String bankAccountNumber;

    @Column(name = "bank_name", nullable = false, length = 200)
    public String bankName;

    @Column(name = "bank_branch_name", length = 200)
    public String bankBranchName;

    @Column(name = "bank_routing_number", length = 32)
    public String bankRoutingNumber;

    @Column(name = "account_currency", nullable = false, length = 8)
    public String accountCurrency;

    @Column(name = "is_receipt_account", nullable = false)
    public boolean receiptAccount;

    @Column(name = "is_payment_account", nullable = false)
    public boolean paymentAccount;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
