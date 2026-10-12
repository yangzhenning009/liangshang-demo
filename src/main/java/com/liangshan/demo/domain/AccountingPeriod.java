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
 * 会计期间。启闭由应用层执行。关闭人是普通可空 Long，不建外键。
 */
@Entity
@Table(name = "accounting_period", indexes = {
        @Index(name = "idx_period_ledger", columnList = "ledger_id"),
        @Index(name = "idx_period_year", columnList = "fiscal_year")
})
@Getter
public class AccountingPeriod extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "ledger_id", nullable = false)
    public Long ledgerId;

    @Column(name = "fiscal_year", nullable = false)
    public Integer fiscalYear;

    @Column(name = "period_number", nullable = false)
    public Integer periodNumber;

    @Column(name = "period_start_date", nullable = false)
    public LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    public LocalDate periodEndDate;

    @Column(name = "period_status", nullable = false, length = 16)
    public String periodStatus;

    @Column(name = "closed_at")
    public LocalDateTime closedAt;

    @Column(name = "closed_by_employee_id")
    public Long closedByEmployeeId;
}
