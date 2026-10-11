package com.liangshan.demo.service;

import com.liangshan.demo.repository.SupplierContactRepository;
import com.liangshan.demo.repository.SupplierQualificationRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.repository.SupplierVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplierLinkGuard {

    private final SupplierRepository suppliers;
    private final SupplierContactRepository contacts;
    private final SupplierQualificationRepository qualifications;
    private final SupplierVersionRepository versions;

    public boolean companyUsed(Long companyId) {
        return suppliers.existsByPurchasingCompanyId(companyId) || suppliers.existsByCompanyId(companyId);
    }

    public boolean legalPersonUsed(Long legalPersonId) {
        return suppliers.existsByLegalPersonId(legalPersonId);
    }

    public boolean personUsed(Long personId) {
        return suppliers.existsByPersonId(personId) || contacts.existsByPersonId(personId);
    }

    public boolean employeeUsed(Long employeeId) {
        return suppliers.existsByProcurementOwnerEmployeeId(employeeId)
                || suppliers.existsByAdmissionReviewedByEmployeeId(employeeId)
                || qualifications.existsByReviewedByEmployeeId(employeeId)
                || versions.existsByChangedByEmployeeId(employeeId);
    }

    public boolean documentUsed(Long documentId) {
        return suppliers.existsByAdmissionDocumentId(documentId)
                || qualifications.existsByQualificationDocumentId(documentId)
                || versions.existsByBasisDocumentId(documentId);
    }
}
