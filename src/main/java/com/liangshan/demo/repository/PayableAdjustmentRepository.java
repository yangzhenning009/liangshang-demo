package com.liangshan.demo.repository;

import com.liangshan.demo.domain.PayableAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayableAdjustmentRepository extends JpaRepository<PayableAdjustment, Long> {

    List<PayableAdjustment> findByPayableItemIdOrderByIdAsc(Long payableItemId);

    boolean existsBySourceDocumentId(Long documentId);

    boolean existsByApprovedByEmployeeId(Long employeeId);
}
