package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CompanyShareholding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyShareholdingRepository extends JpaRepository<CompanyShareholding, Long> {

    List<CompanyShareholding> findByCompanyIdOrderByIdAsc(Long companyId);

    List<CompanyShareholding> findByShareholderCompanyId(Long shareholderCompanyId);

    List<CompanyShareholding> findByShareholderLegalPersonId(Long shareholderLegalPersonId);

    List<CompanyShareholding> findByShareholderPersonId(Long shareholderPersonId);
}
