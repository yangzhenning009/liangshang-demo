package com.liangshan.demo.repository;

import com.liangshan.demo.domain.InvoiceLineAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineAllocationRepository extends JpaRepository<InvoiceLineAllocation, Long> {

    List<InvoiceLineAllocation> findByInvoiceLineIdOrderByIdAsc(Long invoiceLineId);

    boolean existsByContractId(Long contractId);

    boolean existsByProjectId(Long projectId);

    boolean existsByReceivableItemId(Long receivableItemId);

    boolean existsByPayableItemId(Long payableItemId);

    boolean existsByBillingScheduleId(Long billingScheduleId);
}
