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

/**
 * 开票计划。被替代的原计划保留。关联 ID 是普通 Long，不建外键。
 */
@Entity
@Table(name = "contract_billing_schedule", indexes = {
        @Index(name = "idx_billing_schedule_contract", columnList = "contract_id"),
        @Index(name = "idx_billing_schedule_term", columnList = "contract_term_version_id"),
        @Index(name = "idx_billing_schedule_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_billing_schedule_supersedes", columnList = "supersedes_billing_schedule_id")
})
@Getter
public class ContractBillingSchedule extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "contract_term_version_id", nullable = false)
    public Long contractTermVersionId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "schedule_number", nullable = false)
    public Integer scheduleNumber;

    @Column(name = "billing_trigger", nullable = false, length = 32)
    public String billingTrigger;

    @Column(name = "planned_invoice_gross_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal plannedInvoiceGrossAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "planned_invoice_date")
    public LocalDate plannedInvoiceDate;

    @Column(name = "days_after_trigger")
    public Integer daysAfterTrigger;

    @Column(name = "trigger_description", length = 500)
    public String triggerDescription;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "supersedes_billing_schedule_id")
    public Long supersedesBillingScheduleId;
}
