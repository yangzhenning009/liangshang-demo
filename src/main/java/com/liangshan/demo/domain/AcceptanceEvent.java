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
 * 一次验收结论。复验另记一条，不覆盖原先已经通过的数量。更正通过 supersedes 指向原结论。
 */
@Entity
@Table(name = "acceptance_event", indexes = {
        @Index(name = "idx_acceptance_event_delivery", columnList = "delivery_event_id"),
        @Index(name = "idx_acceptance_event_supersedes", columnList = "supersedes_acceptance_event_id"),
        @Index(name = "idx_acceptance_event_document", columnList = "source_document_id")
})
@Getter
public class AcceptanceEvent extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "delivery_event_id", nullable = false)
    public Long deliveryEventId;

    @Column(name = "acceptor_company_id")
    public Long acceptorCompanyId;

    @Column(name = "acceptor_legal_person_id")
    public Long acceptorLegalPersonId;

    @Column(name = "acceptor_person_id")
    public Long acceptorPersonId;

    @Column(name = "accepted_by_employee_id")
    public Long acceptedByEmployeeId;

    @Column(name = "accepted_by_person_id")
    public Long acceptedByPersonId;

    @Column(name = "acceptance_number", nullable = false, length = 64)
    public String acceptanceNumber;

    @Column(name = "supersedes_acceptance_event_id")
    public Long supersedesAcceptanceEventId;

    @Column(name = "status", nullable = false, length = 32)
    public String status;

    @Column(name = "accepted_at", nullable = false)
    public LocalDateTime acceptedAt;

    @Column(name = "acceptance_result", nullable = false, length = 32)
    public String acceptanceResult;

    @Column(name = "acceptance_comments", length = 1000)
    public String acceptanceComments;

    @Column(name = "source_document_id")
    public Long sourceDocumentId;
}
