package com.liangshan.demo.repository;

import com.liangshan.demo.domain.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {

    List<PaymentAllocation> findByPayableItemId(Long payableItemId);

    List<PaymentAllocation> findByReceivableItemId(Long receivableItemId);

    List<PaymentAllocation> findByPaymentIdOrderByIdAsc(Long paymentId);

    List<PaymentAllocation> findByReversesAllocationId(Long reversesAllocationId);
}
