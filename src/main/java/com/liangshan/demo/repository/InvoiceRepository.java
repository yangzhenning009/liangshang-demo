package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrderByIssuedAtDescIdDesc();

    List<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsBySourceDocumentId(Long sourceDocumentId);

    boolean existsByVerifiedByEmployeeId(Long employeeId);
}
