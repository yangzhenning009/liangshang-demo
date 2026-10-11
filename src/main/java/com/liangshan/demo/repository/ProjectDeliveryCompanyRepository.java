package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ProjectDeliveryCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectDeliveryCompanyRepository extends JpaRepository<ProjectDeliveryCompany, Long> {

    List<ProjectDeliveryCompany> findByProjectIdOrderByIdAsc(Long projectId);

    boolean existsByDeliveryCompanyId(Long companyId);

    boolean existsByLeadDepartmentId(Long departmentId);

    boolean existsByBasisDocumentId(Long documentId);
}
