package com.liangshan.demo.repository;

import com.liangshan.demo.domain.DeliveryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryLineRepository extends JpaRepository<DeliveryLine, Long> {

    List<DeliveryLine> findByDeliveryEventIdOrderByIdAsc(Long deliveryEventId);

    boolean existsByStageDeliverableId(Long stageDeliverableId);

    boolean existsByDeliverableDocumentId(Long deliverableDocumentId);
}
