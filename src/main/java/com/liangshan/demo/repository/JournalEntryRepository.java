package com.liangshan.demo.repository;

import com.liangshan.demo.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findAllByOrderByVoucherDateDescIdDesc();

    List<JournalEntry> findByLedgerIdAndAccountingPeriodIdAndVoucherNumber(
            Long ledgerId, Long accountingPeriodId, String voucherNumber);

    List<JournalEntry> findByReversalOfEntryId(Long reversalOfEntryId);
}
