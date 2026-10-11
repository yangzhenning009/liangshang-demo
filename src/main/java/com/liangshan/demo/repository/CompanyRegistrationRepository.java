package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CompanyRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRegistrationRepository extends JpaRepository<CompanyRegistration, Long> {

    List<CompanyRegistration> findByCompanyIdOrderByVersionNumberAsc(Long companyId);

    List<CompanyRegistration> findByUnifiedSocialCreditCode(String unifiedSocialCreditCode);

    List<CompanyRegistration> findByBusinessLicenseRegistrationNumber(String businessLicenseRegistrationNumber);

    List<CompanyRegistration> findByTaxpayerIdentificationNumber(String taxpayerIdentificationNumber);

    List<CompanyRegistration> findByBranchManagerPersonId(Long branchManagerPersonId);
}
