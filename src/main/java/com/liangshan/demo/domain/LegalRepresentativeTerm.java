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
@Table(name = "legal_representative_term", indexes = {
        @Index(name = "idx_rep_term_legal_person_id", columnList = "legal_person_id"),
        @Index(name = "idx_rep_term_person_id", columnList = "person_id")
})
@Getter
public class LegalRepresentativeTerm extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "legal_person_id", nullable = false)
    public Long legalPersonId;

    @Column(name = "person_id", nullable = false)
    public Long personId;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
