package com.liangshan.demo.repository;

import com.liangshan.demo.domain.AcceptanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcceptanceEventRepository extends JpaRepository<AcceptanceEvent, Long> {

    List<AcceptanceEvent> findByDeliveryEventIdOrderByAcceptedAtAscIdAsc(Long deliveryEventId);

    boolean existsByDeliveryEventIdAndStatus(Long deliveryEventId, String status);

    boolean existsByAcceptorCompanyId(Long acceptorCompanyId);

    boolean existsByAcceptorLegalPersonId(Long acceptorLegalPersonId);

    boolean existsByAcceptorPersonId(Long acceptorPersonId);

    boolean existsByAcceptedByEmployeeId(Long acceptedByEmployeeId);

    boolean existsByAcceptedByPersonId(Long acceptedByPersonId);

    boolean existsBySourceDocumentId(Long sourceDocumentId);

    boolean existsBySupersedesAcceptanceEventId(Long supersedesAcceptanceEventId);
}
