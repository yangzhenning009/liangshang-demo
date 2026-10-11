package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findAllByOrderByContractNumberAsc();

    List<Contract> findByContractNumber(String contractNumber);

    boolean existsBySupplierId(Long supplierId);

    boolean existsByBuyerCompanyId(Long companyId);

    boolean existsByBuyerLegalPersonId(Long legalPersonId);

    boolean existsByBuyerPersonId(Long personId);

    boolean existsBySellerCompanyId(Long companyId);

    boolean existsBySellerLegalPersonId(Long legalPersonId);

    boolean existsBySellerPersonId(Long personId);

    boolean existsByManagingCompanyId(Long companyId);

    boolean existsByProcurementCompanyId(Long companyId);

    boolean existsByContractOwnerEmployeeId(Long employeeId);

    boolean existsByContractApprovedByEmployeeId(Long employeeId);

    boolean existsByBuyerSignatoryPersonId(Long personId);

    boolean existsBySellerSignatoryPersonId(Long personId);

    boolean existsByBuyerContactPersonId(Long personId);

    boolean existsBySellerContactPersonId(Long personId);

    boolean existsBySourceDocumentId(Long documentId);
}
