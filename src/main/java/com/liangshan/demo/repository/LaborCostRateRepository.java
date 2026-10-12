package com.liangshan.demo.repository;

import com.liangshan.demo.domain.LaborCostRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaborCostRateRepository extends JpaRepository<LaborCostRate, Long> {

    List<LaborCostRate> findAllByOrderByIdDesc();

    List<LaborCostRate> findByEmploymentIdAndCostBasisOrderByRateVersionNumberAsc(Long employmentId, String costBasis);

    boolean existsByEmploymentId(Long employmentId);

    boolean existsByApprovedByEmployeeId(Long approvedByEmployeeId);

    boolean existsByBasisDocumentId(Long basisDocumentId);
}
