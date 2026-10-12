package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPaymentNumber(String paymentNumber);
}
