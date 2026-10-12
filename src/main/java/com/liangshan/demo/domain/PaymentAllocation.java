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
import java.time.LocalDateTime;

/**
 * 收付款核销。apply 核销，reverse 撤销。不新建成本，也不改应收应付本金。
 */
@Entity
@Table(name = "payment_allocation", indexes = {
        @Index(name = "idx_pay_alloc_payment", columnList = "payment_id"),
        @Index(name = "idx_pay_alloc_payable", columnList = "payable_item_id"),
        @Index(name = "idx_pay_alloc_receivable", columnList = "receivable_item_id")
})
@Getter
public class PaymentAllocation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "payment_id", nullable = false)
    public Long paymentId;

    @Column(name = "receivable_item_id")
    public Long receivableItemId;

    @Column(name = "payable_item_id")
    public Long payableItemId;

    @Column(name = "settlement_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal settlementAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "settlement_action", nullable = false, length = 32)
    public String settlementAction;

    @Column(name = "reverses_allocation_id")
    public Long reversesAllocationId;

    @Column(name = "allocated_at", nullable = false)
    public LocalDateTime allocatedAt;

    @Column(name = "allocated_by_employee_id", nullable = false)
    public Long allocatedByEmployeeId;

    @Column(name = "settlement_basis_document_id")
    public Long settlementBasisDocumentId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
