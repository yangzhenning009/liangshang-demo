package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ExpenseClaimLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseClaimLineRepository extends JpaRepository<ExpenseClaimLine, Long> {

    List<ExpenseClaimLine> findByExpenseClaimIdOrderByLineNumberAscIdAsc(Long expenseClaimId);

    List<ExpenseClaimLine> findByCostOccurrenceId(Long costOccurrenceId);
}
