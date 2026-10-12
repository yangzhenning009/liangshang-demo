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
 * 发票行的开票覆盖。关联应收或应付不改变结算本金，也不计入收付款余额。关联 ID 不建外键。
 */
@Entity
@Table(name = "invoice_line_allocation", indexes = {
        @Index(name = "idx_invoice_alloc_line", columnList = "invoice_line_id"),
        @Index(name = "idx_invoice_alloc_contract", columnList = "contract_id"),
        @Index(name = "idx_invoice_alloc_project", columnList = "project_id"),
        @Index(name = "idx_invoice_alloc_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_invoice_alloc_billing", columnList = "billing_schedule_id"),
        @Index(name = "idx_invoice_alloc_receivable", columnList = "receivable_item_id"),
        @Index(name = "idx_invoice_alloc_payable", columnList = "payable_item_id")
})
@Getter
public class InvoiceLineAllocation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "invoice_line_id", nullable = false)
    public Long invoiceLineId;

    @Column(name = "contract_id")
    public Long contractId;

    @Column(name = "project_id")
    public Long projectId;

    @Column(name = "contract_stage_id")
    public Long contractStageId;

    @Column(name = "billing_schedule_id")
    public Long billingScheduleId;

    @Column(name = "receivable_item_id")
    public Long receivableItemId;

    @Column(name = "payable_item_id")
    public Long payableItemId;

    @Column(name = "allocated_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal allocatedAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "allocation_basis", nullable = false, length = 16)
    public String allocationBasis;
}
