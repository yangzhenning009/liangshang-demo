package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractTermVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractTermVersionRepository extends JpaRepository<ContractTermVersion, Long> {

    List<ContractTermVersion> findByContractIdOrderByVersionNumberAsc(Long contractId);

    boolean existsBySupplierId(Long supplierId);

    boolean existsByBuyerCompanyId(Long companyId);

    boolean existsByBuyerLegalPersonId(Long legalPersonId);

    boolean existsByBuyerPersonId(Long personId);

    boolean existsBySellerCompanyId(Long companyId);

    boolean existsBySellerLegalPersonId(Long legalPersonId);

    boolean existsBySellerPersonId(Long personId);

    boolean existsByProcurementCompanyId(Long companyId);

    boolean existsByBuyerSignatoryPersonId(Long personId);

    boolean existsBySellerSignatoryPersonId(Long personId);

    boolean existsByBuyerContactPersonId(Long personId);

    boolean existsBySellerContactPersonId(Long personId);
}
