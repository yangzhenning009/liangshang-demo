package com.liangshan.demo.repository;

import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeDepartmentAssignmentRepository extends JpaRepository<EmployeeDepartmentAssignment, Long> {

    List<EmployeeDepartmentAssignment> findByEmployeeIdOrderByIdAsc(Long employeeId);

    List<EmployeeDepartmentAssignment> findByEmploymentId(Long employmentId);

    boolean existsByEmployeeId(Long employeeId);
}
