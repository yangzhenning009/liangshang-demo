package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "person", indexes = {
        @Index(name = "idx_person_name", columnList = "name"),
        @Index(name = "idx_person_status", columnList = "status")
})
@Getter
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "mobile_phone", length = 40)
    public String mobilePhone;

    @Column(name = "email", length = 200)
    public String email;

    @Column(name = "status", nullable = false, length = 32)
    public String status;
}
