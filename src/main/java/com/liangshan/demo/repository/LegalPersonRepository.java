package com.liangshan.demo.repository;

import com.liangshan.demo.domain.LegalPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalPersonRepository extends JpaRepository<LegalPerson, Long> {

    List<LegalPerson> findAllByOrderByLegalNameAsc();
}
