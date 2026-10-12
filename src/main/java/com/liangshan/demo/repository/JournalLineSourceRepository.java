package com.liangshan.demo.repository;

import com.liangshan.demo.domain.JournalLineSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalLineSourceRepository extends JpaRepository<JournalLineSource, Long> {

    List<JournalLineSource> findByJournalLineIdOrderByIdAsc(Long journalLineId);

    List<JournalLineSource> findByCostOccurrenceId(Long costOccurrenceId);

    List<JournalLineSource> findByRevenueRecognitionId(Long revenueRecognitionId);
}
