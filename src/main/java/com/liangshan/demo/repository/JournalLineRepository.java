package com.liangshan.demo.repository;

import com.liangshan.demo.domain.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalLineRepository extends JpaRepository<JournalLine, Long> {

    List<JournalLine> findByJournalEntryIdOrderByLineNumberAscIdAsc(Long journalEntryId);
}
