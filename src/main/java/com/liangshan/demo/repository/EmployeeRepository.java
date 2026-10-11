package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByOrderByEmployeeCodeAsc();

    List<Employee> findByPersonId(Long personId);

    List<Employee> findByEmployeeCode(String employeeCode);
}
