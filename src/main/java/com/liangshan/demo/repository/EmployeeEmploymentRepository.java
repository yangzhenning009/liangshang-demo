package com.liangshan.demo.repository;

import com.liangshan.demo.domain.EmployeeEmployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeEmploymentRepository extends JpaRepository<EmployeeEmployment, Long> {

    List<EmployeeEmployment> findByEmployeeIdOrderByIdAsc(Long employeeId);

    List<EmployeeEmployment> findByEmployerCompanyIdAndEmployeeNumber(Long employerCompanyId, String employeeNumber);

    List<EmployeeEmployment> findByEmployerLegalPersonIdAndEmployeeNumber(Long employerLegalPersonId, String employeeNumber);

    boolean existsByEmploymentDocumentId(Long employmentDocumentId);
}
