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
 * 费用对应的发票行。发票对象在后续包里扩展，这里只保存普通行 ID。
 */
@Entity
@Table(name = "cost_invoice_line", indexes = {
        @Index(name = "idx_cost_invoice_cost", columnList = "cost_occurrence_id"),
        @Index(name = "idx_cost_invoice_line", columnList = "invoice_line_id")
})
@Getter
public class CostInvoiceLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "cost_occurrence_id", nullable = false)
    public Long costOccurrenceId;

    @Column(name = "invoice_line_id", nullable = false)
    public Long invoiceLineId;

    @Column(name = "matched_expense_gross_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal matchedExpenseGrossAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;
}
