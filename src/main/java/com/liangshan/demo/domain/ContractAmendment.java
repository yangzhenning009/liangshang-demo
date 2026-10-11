package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "contract_amendment", indexes = {
        @Index(name = "idx_contract_amendment_contract", columnList = "contract_id"),
        @Index(name = "idx_contract_amendment_document", columnList = "source_document_id")
})
@Getter
public class ContractAmendment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "amendment_number", nullable = false, length = 64)
    public String amendmentNumber;

    @Column(name = "signed_at", nullable = false)
    public LocalDate signedAt;

    @Column(name = "effective_at", nullable = false)
    public LocalDate effectiveAt;

    @Column(name = "source_document_id", nullable = false)
    public Long sourceDocumentId;

    @Column(name = "summary", nullable = false, length = 1000)
    public String summary;
}
