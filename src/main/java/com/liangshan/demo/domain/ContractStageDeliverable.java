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
 * 交付项挂在阶段条款版本上。同一阶段跨版本保持 deliverable_code，新版本使用新的主键。
 */
@Entity
@Table(name = "contract_stage_deliverable", indexes = {
        @Index(name = "idx_stage_deliverable_term", columnList = "stage_term_version_id"),
        @Index(name = "idx_stage_deliverable_line_term", columnList = "contract_line_term_version_id")
})
@Getter
public class ContractStageDeliverable extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "stage_term_version_id", nullable = false)
    public Long stageTermVersionId;

    @Column(name = "contract_line_term_version_id")
    public Long contractLineTermVersionId;

    @Column(name = "deliverable_code", nullable = false, length = 64)
    public String deliverableCode;

    @Column(name = "deliverable_name", nullable = false, length = 200)
    public String deliverableName;

    @Column(name = "deliverable_specification", length = 500)
    public String deliverableSpecification;

    @Column(name = "unit", nullable = false, length = 32)
    public String unit;

    @Column(name = "planned_quantity", nullable = false, precision = 18, scale = 4)
    public BigDecimal plannedQuantity;

    @Column(name = "acceptance_criteria", nullable = false, length = 1000)
    public String acceptanceCriteria;

    @Column(name = "planned_delivery_date", nullable = false)
    public LocalDate plannedDeliveryDate;

    @Column(name = "planned_acceptance_date")
    public LocalDate plannedAcceptanceDate;
}
