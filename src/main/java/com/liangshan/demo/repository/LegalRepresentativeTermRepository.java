package com.liangshan.demo.repository;

import com.liangshan.demo.domain.LegalRepresentativeTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalRepresentativeTermRepository extends JpaRepository<LegalRepresentativeTerm, Long> {

    List<LegalRepresentativeTerm> findByLegalPersonIdOrderByValidFromAsc(Long legalPersonId);

    List<LegalRepresentativeTerm> findByPersonId(Long personId);
}
