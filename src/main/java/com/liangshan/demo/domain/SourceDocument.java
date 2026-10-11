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

/**
 * 来源材料。出具对象是一组可空 ID，最多一个由应用层判断。不建外键。
 */
@Entity
@Table(name = "source_document", indexes = {
        @Index(name = "idx_source_document_issuer_company", columnList = "issuer_company_id"),
        @Index(name = "idx_source_document_issuer_legal", columnList = "issuer_legal_person_id"),
        @Index(name = "idx_source_document_issuer_person", columnList = "issuer_person_id"),
        @Index(name = "idx_source_document_prepared_by", columnList = "prepared_by_employee_id"),
        @Index(name = "idx_source_document_uploaded_by", columnList = "uploaded_by_employee_id")
})
@Getter
public class SourceDocument extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "document_type", length = 50)
    public String documentType;

    @Column(name = "document_number", length = 64)
    public String documentNumber;

    @Column(name = "issuer_company_id")
    public Long issuerCompanyId;

    @Column(name = "issuer_legal_person_id")
    public Long issuerLegalPersonId;

    @Column(name = "issuer_person_id")
    public Long issuerPersonId;

    @Column(name = "prepared_by_employee_id")
    public Long preparedByEmployeeId;

    @Column(name = "issued_at")
    public LocalDate issuedAt;

    @Column(name = "file_storage_uri", length = 500)
    public String fileStorageUri;

    @Column(name = "file_name", length = 255)
    public String fileName;

    @Column(name = "mime_type", length = 100)
    public String mimeType;

    @Column(name = "file_checksum", length = 128)
    public String fileChecksum;

    @Column(name = "uploaded_at")
    public LocalDateTime uploadedAt;

    @Column(name = "uploaded_by_employee_id")
    public Long uploadedByEmployeeId;

    @Column(name = "content_summary", length = 2000)
    public String contentSummary;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
