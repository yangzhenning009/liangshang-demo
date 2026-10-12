package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 账簿。归属法人必填。范围公司可空，分公司账簿仍保存分公司自己的 ID。关联 ID 不建外键。
 */
@Entity
@Table(name = "ledger", indexes = {
        @Index(name = "idx_ledger_legal_person", columnList = "legal_person_id"),
        @Index(name = "idx_ledger_scope_company", columnList = "scope_company_id"),
        @Index(name = "idx_ledger_code", columnList = "ledger_code")
})
@Getter
public class Ledger extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "legal_person_id", nullable = false)
    public Long legalPersonId;

    @Column(name = "scope_company_id")
    public Long scopeCompanyId;

    @Column(name = "ledger_code", nullable = false, length = 64)
    public String ledgerCode;

    @Column(name = "name", nullable = false, length = 200)
    public String name;

    @Column(name = "ledger_type", nullable = false, length = 32)
    public String ledgerType;

    @Column(name = "base_currency", nullable = false, length = 8)
    public String baseCurrency;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
