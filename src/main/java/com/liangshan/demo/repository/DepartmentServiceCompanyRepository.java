package com.liangshan.demo.repository;

import com.liangshan.demo.domain.DepartmentServiceCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentServiceCompanyRepository extends JpaRepository<DepartmentServiceCompany, Long> {

    List<DepartmentServiceCompany> findByDepartmentIdOrderByIdAsc(Long departmentId);

    List<DepartmentServiceCompany> findByServiceCompanyId(Long serviceCompanyId);
}
