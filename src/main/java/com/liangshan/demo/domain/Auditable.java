package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 通用审计字段。业务关联不放在这里，也不产生数据库外键。
 */
@MappedSuperclass
@Getter
public abstract class Auditable {

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    public String createdBy;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (createdBy != null && createdBy.isBlank()) {
            createdBy = null;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
