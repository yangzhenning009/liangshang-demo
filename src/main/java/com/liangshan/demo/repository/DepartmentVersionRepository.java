package com.liangshan.demo.repository;

import com.liangshan.demo.domain.DepartmentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentVersionRepository extends JpaRepository<DepartmentVersion, Long> {

    List<DepartmentVersion> findByDepartmentIdOrderByVersionNumberAsc(Long departmentId);

    List<DepartmentVersion> findByCompanyId(Long companyId);

    List<DepartmentVersion> findByDepartmentHeadEmployeeId(Long departmentHeadEmployeeId);

    boolean existsByBasisDocumentId(Long basisDocumentId);
}
