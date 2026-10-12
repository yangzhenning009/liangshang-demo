package com.liangshan.demo.service;

import com.liangshan.demo.repository.LaborCostRateRepository;
import com.liangshan.demo.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkLogLinkGuard {

    private final WorkLogRepository logs;
    private final LaborCostRateRepository rates;

    public boolean employeeUsed(Long employeeId) {
        return logs.existsByEmployeeId(employeeId) || logs.existsByApprovedByEmployeeId(employeeId)
                || rates.existsByApprovedByEmployeeId(employeeId);
    }

    public boolean employmentUsed(Long employmentId) {
        return logs.existsByEmploymentId(employmentId) || rates.existsByEmploymentId(employmentId);
    }

    public boolean assignmentUsed(Long assignmentId) {
        return logs.existsByDepartmentAssignmentId(assignmentId);
    }

    public boolean projectUsed(Long projectId) {
        return logs.existsByProjectId(projectId);
    }

    public boolean stageUsed(Long stageId) {
        return logs.existsByContractStageId(stageId);
    }

    public boolean documentUsed(Long documentId) {
        return rates.existsByBasisDocumentId(documentId);
    }
}
