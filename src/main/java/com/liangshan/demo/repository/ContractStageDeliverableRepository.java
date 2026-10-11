package com.liangshan.demo.repository;

import com.liangshan.demo.domain.ContractStageDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractStageDeliverableRepository extends JpaRepository<ContractStageDeliverable, Long> {

    List<ContractStageDeliverable> findByStageTermVersionIdOrderByIdAsc(Long stageTermVersionId);
}
