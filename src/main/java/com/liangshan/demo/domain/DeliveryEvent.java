package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 一次交付提交。提交方恰好一个，阶段、条款版本和项目合同须一致，由应用层校验。
 */
@Entity
@Table(name = "delivery_event", indexes = {
        @Index(name = "idx_delivery_event_project", columnList = "project_id"),
        @Index(name = "idx_delivery_event_stage", columnList = "contract_stage_id"),
        @Index(name = "idx_delivery_event_stage_term", columnList = "stage_term_version_id"),
        @Index(name = "idx_delivery_event_resubmission", columnList = "resubmission_of_delivery_event_id"),
        @Index(name = "idx_delivery_event_document", columnList = "source_document_id")
})
@Getter
public class DeliveryEvent extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "contract_stage_id", nullable = false)
    public Long contractStageId;

    @Column(name = "stage_term_version_id", nullable = false)
    public Long stageTermVersionId;

    @Column(name = "submitter_company_id")
    public Long submitterCompanyId;

    @Column(name = "submitter_legal_person_id")
    public Long submitterLegalPersonId;

    @Column(name = "submitter_person_id")
    public Long submitterPersonId;

    @Column(name = "submitted_by_employee_id")
    public Long submittedByEmployeeId;

    @Column(name = "delivery_number", nullable = false, length = 64)
    public String deliveryNumber;

    @Column(name = "submitted_at", nullable = false)
    public LocalDateTime submittedAt;

    @Column(name = "deliverable_description", nullable = false, length = 1000)
    public String deliverableDescription;

    @Column(name = "resubmission_of_delivery_event_id")
    public Long resubmissionOfDeliveryEventId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
