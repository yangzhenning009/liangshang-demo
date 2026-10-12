package com.liangshan.demo.repository;

import com.liangshan.demo.domain.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {

    List<AccountingPeriod> findByLedgerIdOrderByPeriodStartDateAsc(Long ledgerId);

    List<AccountingPeriod> findAllByOrderByPeriodStartDateDesc();
}
