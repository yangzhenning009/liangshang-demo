package com.liangshan.demo.service;

import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractPaymentScheduleRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.DeliveryEventRepository;
import com.liangshan.demo.repository.DeliveryLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 交付、验收和收付款计划对主数据的引用。只查仓库，避免和合同服务互相依赖。
 */
@Component
@RequiredArgsConstructor
public class FulfillmentLinkGuard {

    private final ContractBillingScheduleRepository billings;
    private final ContractPaymentScheduleRepository payments;
    private final DeliveryEventRepository deliveries;
    private final DeliveryLineRepository deliveryLines;
    private final AcceptanceEventRepository acceptances;
    private final ContractStageRepository stages;

    public boolean companyUsed(Long companyId) {
        return deliveries.existsBySubmitterCompanyId(companyId)
                || acceptances.existsByAcceptorCompanyId(companyId);
    }

    public boolean legalPersonUsed(Long legalPersonId) {
        return deliveries.existsBySubmitterLegalPersonId(legalPersonId)
                || acceptances.existsByAcceptorLegalPersonId(legalPersonId);
    }

    public boolean personUsed(Long personId) {
        return deliveries.existsBySubmitterPersonId(personId)
                || acceptances.existsByAcceptorPersonId(personId)
                || acceptances.existsByAcceptedByPersonId(personId);
    }

    public boolean employeeUsed(Long employeeId) {
        return deliveries.existsBySubmittedByEmployeeId(employeeId)
                || acceptances.existsByAcceptedByEmployeeId(employeeId);
    }

    public boolean documentUsed(Long documentId) {
        return deliveries.existsBySourceDocumentId(documentId)
                || deliveryLines.existsByDeliverableDocumentId(documentId)
                || acceptances.existsBySourceDocumentId(documentId);
    }

    public boolean projectUsed(Long projectId) {
        return deliveries.existsByProjectId(projectId);
    }

    public boolean contractUsed(Long contractId) {
        if (billings.existsByContractId(contractId) || payments.existsByContractId(contractId)) {
            return true;
        }
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            if (deliveries.existsByContractStageId(stage.id)) {
                return true;
            }
        }
        return false;
    }

    public boolean stageUsed(Long stageId) {
        return billings.existsByContractStageId(stageId)
                || payments.existsByContractStageId(stageId)
                || deliveries.existsByContractStageId(stageId);
    }

    public boolean deliverableUsed(Long deliverableId) {
        return deliveryLines.existsByStageDeliverableId(deliverableId);
    }
}
