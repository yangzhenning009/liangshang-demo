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
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_qualification", indexes = {
        @Index(name = "idx_supplier_qualification_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_supplier_qualification_document", columnList = "qualification_document_id")
})
@Getter
public class SupplierQualification extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "supplier_id", nullable = false)
    public Long supplierId;

    @Column(name = "qualification_name", nullable = false, length = 200)
    public String qualificationName;

    @Column(name = "certificate_number", length = 64)
    public String certificateNumber;

    @Column(name = "issuing_authority", length = 200)
    public String issuingAuthority;

    @Column(name = "issued_on")
    public LocalDate issuedOn;

    @Column(name = "expires_on")
    public LocalDate expiresOn;

    @Column(name = "has_no_expiry", nullable = false)
    public Boolean noExpiry;

    @Column(name = "qualification_document_id")
    public Long qualificationDocumentId;

    @Column(name = "review_status", nullable = false, length = 32)
    public String reviewStatus;

    @Column(name = "reviewed_by_employee_id")
    public Long reviewedByEmployeeId;

    @Column(name = "reviewed_at")
    public LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    public String rejectionReason;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
