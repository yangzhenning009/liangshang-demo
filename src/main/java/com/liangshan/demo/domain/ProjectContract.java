package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 项目与合同的关联。同一对 ID 不重复，由应用层校验，数据库不建唯一约束。
 */
@Entity
@Table(name = "project_contract", indexes = {
        @Index(name = "idx_project_contract_project", columnList = "project_id"),
        @Index(name = "idx_project_contract_contract", columnList = "contract_id")
})
@Getter
public class ProjectContract extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "relationship_note", length = 500)
    public String relationshipNote;
}
