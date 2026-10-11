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
@Table(name = "supplier_supply_category", indexes = {
        @Index(name = "idx_supplier_supply_category_supplier_id", columnList = "supplier_id")
})
@Getter
public class SupplierSupplyCategory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "supplier_id", nullable = false)
    public Long supplierId;

    @Column(name = "supply_category_code", nullable = false, length = 64)
    public String supplyCategoryCode;

    @Column(name = "supply_category_name", nullable = false, length = 200)
    public String supplyCategoryName;

    @Column(name = "supply_scope_description", nullable = false, length = 500)
    public String supplyScopeDescription;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
