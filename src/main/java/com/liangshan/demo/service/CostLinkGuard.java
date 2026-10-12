package com.liangshan.demo.service;

import com.liangshan.demo.repository.CostAllocationRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.ExpenseClaimRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CostLinkGuard {

    private final CostOccurrenceRepository costs;
    private final CostAllocationRepository allocations;
    private final ExpenseClaimRepository claims;
    private final PayableItemRepository payables;

    public boolean employeeUsed(Long employeeId) {
        return costs.existsByIncurredByEmployeeId(employeeId)
                || costs.existsByApprovedByEmployeeId(employeeId)
                || claims.existsByClaimantEmployeeId(employeeId)
                || claims.existsByApprovedByEmployeeId(employeeId)
                || payables.existsByConfirmedByEmployeeId(employeeId)
                || allocations.existsByAllocatedByEmployeeId(employeeId);
    }

    public boolean projectUsed(Long projectId) {
        return allocations.existsByProjectId(projectId);
    }

    public boolean documentUsed(Long documentId) {
        return costs.existsBySourceDocumentId(documentId)
                || claims.existsBySourceDocumentId(documentId)
                || allocations.existsByBasisDocumentId(documentId)
                || payables.existsBySourceDocumentId(documentId);
    }
}
