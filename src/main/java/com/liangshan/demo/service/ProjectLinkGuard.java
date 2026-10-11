package com.liangshan.demo.service;

import com.liangshan.demo.repository.ProjectBudgetVersionRepository;
import com.liangshan.demo.repository.ProjectDeliveryCompanyRepository;
import com.liangshan.demo.repository.ProjectMemberRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ProjectSupportDepartmentRepository;
import com.liangshan.demo.repository.ProjectVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectLinkGuard {

    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final ProjectBudgetVersionRepository budgets;
    private final ProjectVersionRepository versions;
    private final ProjectDeliveryCompanyRepository deliveries;
    private final ProjectSupportDepartmentRepository supports;

    public boolean companyUsed(Long companyId) {
        return projects.existsByOwnerCompanyId(companyId)
                || projects.existsByCustomerCompanyId(companyId)
                || versions.existsByOwnerCompanyId(companyId)
                || versions.existsByCustomerCompanyId(companyId)
                || deliveries.existsByDeliveryCompanyId(companyId);
    }

    public boolean legalPersonUsed(Long legalPersonId) {
        return projects.existsByCustomerLegalPersonId(legalPersonId)
                || versions.existsByCustomerLegalPersonId(legalPersonId);
    }

    public boolean personUsed(Long personId) {
        return projects.existsByCustomerPersonId(personId)
                || versions.existsByCustomerPersonId(personId);
    }

    public boolean departmentUsed(Long departmentId) {
        return projects.existsByLeadDepartmentId(departmentId)
                || versions.existsByLeadDepartmentId(departmentId)
                || deliveries.existsByLeadDepartmentId(departmentId)
                || supports.existsByDepartmentId(departmentId);
    }

    public boolean employeeUsed(Long employeeId) {
        return projects.existsByProjectManagerEmployeeId(employeeId)
                || versions.existsByProjectManagerEmployeeId(employeeId)
                || members.existsByEmployeeId(employeeId)
                || budgets.existsByApprovedByEmployeeId(employeeId);
    }

    public boolean employmentUsed(Long employmentId) {
        return members.existsByEmploymentId(employmentId);
    }

    public boolean assignmentUsed(Long assignmentId) {
        return members.existsByDepartmentAssignmentId(assignmentId);
    }

    public boolean documentUsed(Long documentId) {
        return budgets.existsByBasisDocumentId(documentId)
                || versions.existsByBasisDocumentId(documentId)
                || deliveries.existsByBasisDocumentId(documentId);
    }
}
