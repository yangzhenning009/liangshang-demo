package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByOrderByDepartmentCodeAsc();

    List<Department> findByCompanyIdOrderByDepartmentCodeAsc(Long companyId);

    List<Department> findByCompanyIdAndDepartmentCode(Long companyId, String departmentCode);

    List<Department> findByParentDepartmentId(Long parentDepartmentId);

    List<Department> findByDepartmentHeadEmployeeId(Long departmentHeadEmployeeId);
}
