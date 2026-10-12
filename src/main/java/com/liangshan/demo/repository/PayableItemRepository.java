package com.liangshan.demo.repository;

import com.liangshan.demo.domain.PayableItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayableItemRepository extends JpaRepository<PayableItem, Long> {

    List<PayableItem> findByExpenseClaimId(Long expenseClaimId);

    List<PayableItem> findByPayableNumber(String payableNumber);

    boolean existsByCostOccurrenceId(Long costOccurrenceId);

    boolean existsByConfirmedByEmployeeId(Long employeeId);

    boolean existsBySourceDocumentId(Long documentId);
}
