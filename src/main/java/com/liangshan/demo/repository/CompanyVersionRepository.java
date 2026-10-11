package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CompanyVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyVersionRepository extends JpaRepository<CompanyVersion, Long> {

    List<CompanyVersion> findByCompanyIdOrderByVersionNumberAsc(Long companyId);

    List<CompanyVersion> findByLegalPersonId(Long legalPersonId);

    List<CompanyVersion> findByHeadCompanyId(Long headCompanyId);

    boolean existsByBasisDocumentId(Long basisDocumentId);
}
