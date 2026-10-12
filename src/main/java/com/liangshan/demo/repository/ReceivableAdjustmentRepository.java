package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ReceivableAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceivableAdjustmentRepository extends JpaRepository<ReceivableAdjustment, Long> {

    List<ReceivableAdjustment> findByReceivableItemIdOrderByIdAsc(Long receivableItemId);

    boolean existsBySourceDocumentId(Long documentId);

    boolean existsByApprovedByEmployeeId(Long employeeId);
}
