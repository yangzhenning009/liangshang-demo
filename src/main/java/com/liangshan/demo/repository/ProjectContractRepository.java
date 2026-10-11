package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ProjectContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectContractRepository extends JpaRepository<ProjectContract, Long> {

    List<ProjectContract> findByProjectIdOrderByIdAsc(Long projectId);

    List<ProjectContract> findByContractIdOrderByIdAsc(Long contractId);

    boolean existsByProjectId(Long projectId);

    boolean existsByContractId(Long contractId);
}
