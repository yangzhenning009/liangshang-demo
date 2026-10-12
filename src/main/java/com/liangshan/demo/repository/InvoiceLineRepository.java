package com.liangshan.demo.repository;

import com.liangshan.demo.domain.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {

    List<InvoiceLine> findByInvoiceIdOrderByLineNumberAscIdAsc(Long invoiceId);

    List<InvoiceLine> findAllByOrderByIdAsc();
}
