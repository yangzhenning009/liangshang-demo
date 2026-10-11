package com.liangshan.demo.service;

import com.liangshan.demo.repository.ContractAdditionalSignatoryRepository;
import com.liangshan.demo.repository.ContractAmendmentRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContractLinkGuard {

    private final ContractRepository contracts;
    private final ContractTermVersionRepository terms;
    private final ContractStageRepository stages;
    private final ContractAmendmentRepository amendments;
    private final ContractAdditionalSignatoryRepository signatories;
    private final ProjectContractRepository projectContracts;

    public boolean companyUsed(Long companyId) {
        return contracts.existsByBuyerCompanyId(companyId)
                || contracts.existsBySellerCompanyId(companyId)
                || contracts.existsByManagingCompanyId(companyId)
                || contracts.existsByProcurementCompanyId(companyId)
                || terms.existsByBuyerCompanyId(companyId)
                || terms.existsBySellerCompanyId(companyId)
                || terms.existsByProcurementCompanyId(companyId)
                || signatories.existsBySignatoryCompanyId(companyId);
    }

    public boolean legalPersonUsed(Long legalPersonId) {
        return contracts.existsByBuyerLegalPersonId(legalPersonId)
                || contracts.existsBySellerLegalPersonId(legalPersonId)
                || terms.existsByBuyerLegalPersonId(legalPersonId)
                || terms.existsBySellerLegalPersonId(legalPersonId)
                || signatories.existsBySignatoryLegalPersonId(legalPersonId);
    }

    public boolean personUsed(Long personId) {
        return contracts.existsByBuyerPersonId(personId)
                || contracts.existsBySellerPersonId(personId)
                || contracts.existsByBuyerSignatoryPersonId(personId)
                || contracts.existsBySellerSignatoryPersonId(personId)
                || contracts.existsByBuyerContactPersonId(personId)
                || contracts.existsBySellerContactPersonId(personId)
                || terms.existsByBuyerPersonId(personId)
                || terms.existsBySellerPersonId(personId)
                || terms.existsByBuyerSignatoryPersonId(personId)
                || terms.existsBySellerSignatoryPersonId(personId)
                || terms.existsByBuyerContactPersonId(personId)
                || terms.existsBySellerContactPersonId(personId)
                || signatories.existsBySignatoryPersonId(personId);
    }

    public boolean employeeUsed(Long employeeId) {
        return contracts.existsByContractOwnerEmployeeId(employeeId)
                || contracts.existsByContractApprovedByEmployeeId(employeeId)
                || stages.existsByResponsibleEmployeeId(employeeId);
    }

    public boolean documentUsed(Long documentId) {
        return contracts.existsBySourceDocumentId(documentId)
                || amendments.existsBySourceDocumentId(documentId)
                || signatories.existsByBasisDocumentId(documentId);
    }

    public boolean projectUsed(Long projectId) {
        return projectContracts.existsByProjectId(projectId);
    }
}
