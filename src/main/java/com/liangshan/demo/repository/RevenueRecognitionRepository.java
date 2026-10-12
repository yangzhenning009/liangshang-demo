package com.liangshan.demo.repository;

import com.liangshan.demo.domain.RevenueRecognition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevenueRecognitionRepository extends JpaRepository<RevenueRecognition, Long> {

    List<RevenueRecognition> findAllByOrderByRecognitionDateDescIdDesc();

    List<RevenueRecognition> findByReversalOfRecognitionId(Long reversalOfRecognitionId);
}
