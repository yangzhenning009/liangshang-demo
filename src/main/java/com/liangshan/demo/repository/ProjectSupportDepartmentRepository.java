package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ProjectSupportDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectSupportDepartmentRepository extends JpaRepository<ProjectSupportDepartment, Long> {

    List<ProjectSupportDepartment> findByProjectIdOrderByIdAsc(Long projectId);

    boolean existsByDepartmentId(Long departmentId);
}
