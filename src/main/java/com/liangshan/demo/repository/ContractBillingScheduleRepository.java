package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractBillingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractBillingScheduleRepository extends JpaRepository<ContractBillingSchedule, Long> {

    List<ContractBillingSchedule> findByContractIdOrderByScheduleNumberAsc(Long contractId);

    boolean existsByContractId(Long contractId);

    boolean existsByContractStageId(Long contractStageId);

    boolean existsBySupersedesBillingScheduleId(Long supersedesBillingScheduleId);
}
