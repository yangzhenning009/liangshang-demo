package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractStageTermVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractStageTermVersionRepository extends JpaRepository<ContractStageTermVersion, Long> {

    List<ContractStageTermVersion> findByContractStageIdOrderByVersionNumberAsc(Long contractStageId);
}
