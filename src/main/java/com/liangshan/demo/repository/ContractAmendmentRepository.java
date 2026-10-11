package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractAmendment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractAmendmentRepository extends JpaRepository<ContractAmendment, Long> {

    List<ContractAmendment> findByContractIdOrderByIdAsc(Long contractId);

    boolean existsBySourceDocumentId(Long documentId);
}
