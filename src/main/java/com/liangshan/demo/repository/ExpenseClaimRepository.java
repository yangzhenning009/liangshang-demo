package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {

    List<ExpenseClaim> findAllByOrderByIdDesc();

    List<ExpenseClaim> findByClaimNumber(String claimNumber);

    boolean existsByClaimantEmployeeId(Long employeeId);

    boolean existsByApprovedByEmployeeId(Long employeeId);

    boolean existsByReimbursementCompanyId(Long companyId);

    boolean existsBySourceDocumentId(Long documentId);
}
