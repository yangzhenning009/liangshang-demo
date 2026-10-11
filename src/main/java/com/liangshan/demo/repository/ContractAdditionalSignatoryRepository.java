package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractAdditionalSignatory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractAdditionalSignatoryRepository extends JpaRepository<ContractAdditionalSignatory, Long> {

    List<ContractAdditionalSignatory> findByContractIdOrderByIdAsc(Long contractId);

    boolean existsBySignatoryCompanyId(Long companyId);

    boolean existsBySignatoryLegalPersonId(Long legalPersonId);

    boolean existsBySignatoryPersonId(Long personId);

    boolean existsByBasisDocumentId(Long documentId);
}
