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
 * 发票明细。金额按票面符号保存。红字行保留原行。关联 ID 不建外键。
 */
@Entity
@Table(name = "invoice_line", indexes = {
        @Index(name = "idx_invoice_line_invoice", columnList = "invoice_id"),
        @Index(name = "idx_invoice_line_original", columnList = "original_invoice_line_id")
})
@Getter
public class InvoiceLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "invoice_id", nullable = false)
    public Long invoiceId;

    @Column(name = "line_number", nullable = false)
    public Integer lineNumber;

    @Column(name = "item_name", nullable = false, length = 200)
    public String itemName;

    @Column(name = "item_specification", length = 200)
    public String itemSpecification;

    @Column(name = "tax_classification_code", length = 64)
    public String taxClassificationCode;

    @Column(name = "quantity", precision = 18, scale = 4)
    public BigDecimal quantity;

    @Column(name = "unit", length = 32)
    public String unit;

    @Column(name = "unit_net_price", precision = 18, scale = 4)
    public BigDecimal unitNetPrice;

    @Column(name = "tax_rate", precision = 18, scale = 6)
    public BigDecimal taxRate;

    @Column(name = "tax_treatment", length = 32)
    public String taxTreatment;

    @Column(name = "net_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal netAmount;

    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal taxAmount;

    @Column(name = "gross_amount", nullable = false, precision = 18, scale = 2)
    public BigDecimal grossAmount;

    @Column(name = "original_invoice_line_id")
    public Long originalInvoiceLineId;
}
