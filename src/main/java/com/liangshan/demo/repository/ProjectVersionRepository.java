package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Long> {

    List<ProjectVersion> findByProjectIdOrderByVersionNumberAsc(Long projectId);

    boolean existsByOwnerCompanyId(Long companyId);

    boolean existsByCustomerCompanyId(Long companyId);

    boolean existsByCustomerLegalPersonId(Long legalPersonId);

    boolean existsByCustomerPersonId(Long personId);

    boolean existsByLeadDepartmentId(Long departmentId);

    boolean existsByProjectManagerEmployeeId(Long employeeId);

    boolean existsByBasisDocumentId(Long documentId);
}
