package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CostAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostAllocationRepository extends JpaRepository<CostAllocation, Long> {

    List<CostAllocation> findByCostOccurrenceIdOrderByIdAsc(Long costOccurrenceId);

    boolean existsByProjectId(Long projectId);

    boolean existsByContractStageId(Long contractStageId);

    boolean existsByBasisDocumentId(Long documentId);

    boolean existsByAllocatedByEmployeeId(Long employeeId);
}
