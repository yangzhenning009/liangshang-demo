package com.liangshan.demo.repository;

import com.liangshan.demo.domain.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, Long> {

    List<DeliveryEvent> findByContractStageIdOrderBySubmittedAtAscIdAsc(Long contractStageId);

    boolean existsByContractStageId(Long contractStageId);

    boolean existsByProjectId(Long projectId);

    boolean existsBySubmitterCompanyId(Long submitterCompanyId);

    boolean existsBySubmitterLegalPersonId(Long submitterLegalPersonId);

    boolean existsBySubmitterPersonId(Long submitterPersonId);

    boolean existsBySubmittedByEmployeeId(Long submittedByEmployeeId);

    boolean existsBySourceDocumentId(Long sourceDocumentId);

    boolean existsByResubmissionOfDeliveryEventId(Long resubmissionOfDeliveryEventId);
}
