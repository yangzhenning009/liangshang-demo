package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ProjectBudgetVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectBudgetVersionRepository extends JpaRepository<ProjectBudgetVersion, Long> {

    List<ProjectBudgetVersion> findByProjectIdOrderByVersionNumberAsc(Long projectId);

    boolean existsByApprovedByEmployeeId(Long employeeId);

    boolean existsByBasisDocumentId(Long documentId);
}
