package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ReceivableItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceivableItemRepository extends JpaRepository<ReceivableItem, Long> {

    List<ReceivableItem> findAllByOrderByIdDesc();

    List<ReceivableItem> findByReceivableNumber(String receivableNumber);

    List<ReceivableItem> findByContractPaymentScheduleId(Long contractPaymentScheduleId);

    boolean existsByProjectId(Long projectId);

    boolean existsByContractId(Long contractId);

    boolean existsByConfirmedByEmployeeId(Long employeeId);

    boolean existsBySourceDocumentId(Long documentId);

    boolean existsByTriggerInvoiceId(Long invoiceId);
}
