package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByProjectCodeAsc();

    List<Project> findByProjectCode(String projectCode);

    boolean existsByOwnerCompanyId(Long companyId);

    boolean existsByCustomerCompanyId(Long companyId);

    boolean existsByCustomerLegalPersonId(Long legalPersonId);

    boolean existsByCustomerPersonId(Long personId);

    boolean existsByLeadDepartmentId(Long departmentId);

    boolean existsByProjectManagerEmployeeId(Long employeeId);
}
