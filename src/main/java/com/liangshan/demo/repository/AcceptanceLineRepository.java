package com.liangshan.demo.repository;

import com.liangshan.demo.domain.AcceptanceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcceptanceLineRepository extends JpaRepository<AcceptanceLine, Long> {

    List<AcceptanceLine> findByAcceptanceEventIdOrderByIdAsc(Long acceptanceEventId);
}
