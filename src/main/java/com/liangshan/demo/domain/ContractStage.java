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
 * 阶段身份。阶段编号和序号在合同内唯一，由应用层校验。
 */
@Entity
@Table(name = "contract_stage", indexes = {
        @Index(name = "idx_contract_stage_contract", columnList = "contract_id"),
        @Index(name = "idx_contract_stage_employee", columnList = "responsible_employee_id")
})
@Getter
public class ContractStage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "stage_code", nullable = false, length = 64)
    public String stageCode;

    @Column(name = "sequence_no", nullable = false)
    public Integer sequenceNo;

    @Column(name = "stage_name", nullable = false, length = 200)
    public String stageName;

    @Column(name = "responsible_employee_id")
    public Long responsibleEmployeeId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
