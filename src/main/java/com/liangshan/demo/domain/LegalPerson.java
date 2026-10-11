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
@Table(name = "legal_person", indexes = {
        @Index(name = "idx_legal_person_status", columnList = "status")
})
@Getter
public class LegalPerson extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "legal_name", nullable = false, length = 200)
    public String legalName;

    @Column(name = "legal_person_type", nullable = false, length = 50)
    public String legalPersonType;

    @Column(name = "established_at")
    public LocalDate establishedAt;

    @Column(name = "terminated_at")
    public LocalDate terminatedAt;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
