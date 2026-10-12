package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findAllByOrderByLedgerCodeAsc();

    List<Ledger> findByLedgerCode(String ledgerCode);
}
