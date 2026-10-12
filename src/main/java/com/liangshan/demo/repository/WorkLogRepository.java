package com.liangshan.demo.repository;

import com.liangshan.demo.domain.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

    List<WorkLog> findAllByOrderByWorkDateDescIdDesc();

    List<WorkLog> findByApprovalStatusOrderBySubmittedAtAscIdAsc(String approvalStatus);

    boolean existsByEmployeeId(Long employeeId);

    boolean existsByEmploymentId(Long employmentId);

    boolean existsByDepartmentAssignmentId(Long departmentAssignmentId);

    boolean existsByProjectId(Long projectId);

    boolean existsByContractStageId(Long contractStageId);

    boolean existsByApprovedByEmployeeId(Long approvedByEmployeeId);

    boolean existsBySupersedesWorkLogId(Long supersedesWorkLogId);

    boolean existsByLaborCostRateId(Long laborCostRateId);
}
