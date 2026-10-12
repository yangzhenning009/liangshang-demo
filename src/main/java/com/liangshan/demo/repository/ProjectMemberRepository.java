package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectIdOrderByIdAsc(Long projectId);

    List<ProjectMember> findByProjectIdAndEmployeeIdOrderByIdAsc(Long projectId, Long employeeId);

    boolean existsByEmployeeId(Long employeeId);

    boolean existsByEmploymentId(Long employmentId);

    boolean existsByDepartmentAssignmentId(Long assignmentId);
}
