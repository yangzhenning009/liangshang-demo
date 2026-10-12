package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByPaidAtDescIdDesc();

    List<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByRefundOfPaymentId(Long refundOfPaymentId);

    boolean existsByRelatedEmployeeId(Long employeeId);

    boolean existsByConfirmedByEmployeeId(Long employeeId);

    boolean existsBySourceDocumentId(Long documentId);

    boolean existsByAgencyBasisDocumentId(Long documentId);
}
