package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractStageRepository extends JpaRepository<ContractStage, Long> {

    List<ContractStage> findByContractIdOrderBySequenceNoAsc(Long contractId);

    boolean existsByResponsibleEmployeeId(Long employeeId);
}
