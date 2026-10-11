package com.liangshan.demo.repository;

import com.liangshan.demo.domain.EmployeeReportingLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeReportingLineRepository extends JpaRepository<EmployeeReportingLine, Long> {

    List<EmployeeReportingLine> findByEmployeeIdOrderByIdAsc(Long employeeId);

    List<EmployeeReportingLine> findByAssignmentIdOrManagerAssignmentId(Long assignmentId, Long managerAssignmentId);

    boolean existsByEmployeeId(Long employeeId);
}
