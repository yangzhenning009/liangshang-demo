package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractLineTermVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractLineTermVersionRepository extends JpaRepository<ContractLineTermVersion, Long> {

    List<ContractLineTermVersion> findByContractLineId(Long contractLineId);

    List<ContractLineTermVersion> findByContractTermVersionId(Long contractTermVersionId);

    List<ContractLineTermVersion> findByContractLineIdAndContractTermVersionId(Long contractLineId, Long contractTermVersionId);
}
