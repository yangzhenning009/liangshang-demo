package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findAllByOrderByCompanyCodeAsc();

    List<Company> findByCompanyCode(String companyCode);

    List<Company> findByLegalPersonId(Long legalPersonId);

    List<Company> findByHeadCompanyId(Long headCompanyId);

    List<Company> findByBranchManagerPersonId(Long branchManagerPersonId);

    List<Company> findByUnifiedSocialCreditCode(String unifiedSocialCreditCode);

    List<Company> findByBusinessLicenseRegistrationNumber(String businessLicenseRegistrationNumber);

    List<Company> findByTaxpayerIdentificationNumber(String taxpayerIdentificationNumber);

    List<Company> findByCompanyTypeOrderByCompanyCodeAsc(String companyType);
}
