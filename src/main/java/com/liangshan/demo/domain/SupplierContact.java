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
@Table(name = "supplier_contact", indexes = {
        @Index(name = "idx_supplier_contact_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_supplier_contact_person_id", columnList = "person_id")
})
@Getter
public class SupplierContact extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "supplier_id", nullable = false)
    public Long supplierId;

    @Column(name = "person_id", nullable = false)
    public Long personId;

    @Column(name = "contact_role", nullable = false, length = 32)
    public String contactRole;

    @Column(name = "business_phone", length = 40)
    public String businessPhone;

    @Column(name = "business_email", length = 200)
    public String businessEmail;

    @Column(name = "is_primary", nullable = false)
    public Boolean primary;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
