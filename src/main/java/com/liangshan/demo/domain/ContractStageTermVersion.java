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
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_stage_term_version", indexes = {
        @Index(name = "idx_stage_term_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_stage_term_amendment", columnList = "amendment_id")
})
@Getter
public class ContractStageTermVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_stage_id", nullable = false)
    public Long contractStageId;

    @Column(name = "version_number", nullable = false)
    public Integer versionNumber;

    @Column(name = "amendment_id")
    public Long amendmentId;

    @Column(name = "stage_name", nullable = false, length = 200)
    public String stageName;

    @Column(name = "deliverable_description", nullable = false, length = 1000)
    public String deliverableDescription;

    @Column(name = "acceptance_criteria", nullable = false, length = 1000)
    public String acceptanceCriteria;

    @Column(name = "planned_start_at", nullable = false)
    public LocalDate plannedStartAt;

    @Column(name = "planned_due_at", nullable = false)
    public LocalDate plannedDueAt;

    @Column(name = "stage_net_amount", precision = 18, scale = 2)
    public BigDecimal stageNetAmount;

    @Column(name = "stage_tax_amount", precision = 18, scale = 2)
    public BigDecimal stageTaxAmount;

    @Column(name = "stage_gross_amount", precision = 18, scale = 2)
    public BigDecimal stageGrossAmount;

    @Column(name = "currency", nullable = false, length = 8)
    public String currency;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "recorded_at", nullable = false)
    public LocalDateTime recordedAt;
}
