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
 * 品项在某一合同条款版本下的计价。币种沿用合同条款版本，不另存。
 * 单价是不含税单价。折扣后的约定金额直接写在品项金额上。
 */
@Entity
@Table(name = "contract_line_term_version", indexes = {
        @Index(name = "idx_line_term_line", columnList = "contract_line_id"),
        @Index(name = "idx_line_term_contract_term", columnList = "contract_term_version_id")
})
@Getter
public class ContractLineTermVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_line_id", nullable = false)
    public Long contractLineId;

    @Column(name = "contract_term_version_id", nullable = false)
    public Long contractTermVersionId;

    @Column(name = "item_name", nullable = false, length = 200)
    public String itemName;

    @Column(name = "item_specification", length = 500)
    public String itemSpecification;

    @Column(name = "unit", nullable = false, length = 32)
    public String unit;

    @Column(name = "contracted_quantity", precision = 18, scale = 4)
    public BigDecimal contractedQuantity;

    @Column(name = "unit_net_price", precision = 18, scale = 4)
    public BigDecimal unitNetPrice;

    @Column(name = "tax_rate", precision = 8, scale = 6)
    public BigDecimal taxRate;

    @Column(name = "line_net_amount", precision = 18, scale = 2)
    public BigDecimal lineNetAmount;

    @Column(name = "line_tax_amount", precision = 18, scale = 2)
    public BigDecimal lineTaxAmount;

    @Column(name = "line_gross_amount", precision = 18, scale = 2)
    public BigDecimal lineGrossAmount;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;
}
