package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CostInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostInvoiceLineRepository extends JpaRepository<CostInvoiceLine, Long> {

    List<CostInvoiceLine> findByCostOccurrenceIdOrderByIdAsc(Long costOccurrenceId);

    List<CostInvoiceLine> findByInvoiceLineId(Long invoiceLineId);
}
