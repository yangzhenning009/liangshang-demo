package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractLineRepository extends JpaRepository<ContractLine, Long> {

    List<ContractLine> findByContractIdOrderByLineNumberAsc(Long contractId);
}
