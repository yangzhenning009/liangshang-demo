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
@Table(name = "project_support_department", indexes = {
        @Index(name = "idx_project_support_project_id", columnList = "project_id"),
        @Index(name = "idx_project_support_department_id", columnList = "department_id")
})
@Getter
public class ProjectSupportDepartment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "department_id", nullable = false)
    public Long departmentId;

    @Column(name = "responsibility_description", nullable = false, length = 500)
    public String responsibilityDescription;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;
}
