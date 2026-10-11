package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractPaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractPaymentScheduleRepository extends JpaRepository<ContractPaymentSchedule, Long> {

    List<ContractPaymentSchedule> findByContractIdOrderByScheduleNumberAsc(Long contractId);

    boolean existsByContractId(Long contractId);

    boolean existsByContractStageId(Long contractStageId);

    boolean existsBySupersedesPaymentScheduleId(Long supersedesPaymentScheduleId);
}
