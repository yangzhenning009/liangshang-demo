package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ProjectBudgetVersion;
import com.liangshan.demo.domain.ProjectDeliveryCompany;
import com.liangshan.demo.domain.ProjectMember;
import com.liangshan.demo.domain.ProjectSupportDepartment;
import com.liangshan.demo.domain.ProjectVersion;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectBudgetVersionRepository;
import com.liangshan.demo.repository.ProjectDeliveryCompanyRepository;
import com.liangshan.demo.repository.ProjectMemberRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ProjectSupportDepartmentRepository;
import com.liangshan.demo.repository.ProjectVersionRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.ProjectDetailForm;
import com.liangshan.demo.web.form.ProjectDetailForm.BudgetForm;
import com.liangshan.demo.web.form.ProjectDetailForm.DeliveryForm;
import com.liangshan.demo.web.form.ProjectDetailForm.MemberForm;
import com.liangshan.demo.web.form.ProjectDetailForm.SupportForm;
import com.liangshan.demo.web.form.ProjectForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final ProjectBudgetVersionRepository budgets;
    private final ProjectVersionRepository versions;
    private final ProjectDeliveryCompanyRepository deliveries;
    private final ProjectSupportDepartmentRepository supports;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final DepartmentRepository departments;
    private final EmployeeRepository employees;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final SourceDocumentService documents;
    private final ContractLinkGuard contractLinks;
    private final FulfillmentLinkGuard fulfillmentLinks;
    private final WorkLogLinkGuard workLogLinks;
    private final CostLinkGuard costLinks;

    public record Row(
            Long id,
            String projectCode,
            String name,
            String projectType,
            String ownerCompany,
            String customer,
            String status) {
    }

    public record Header(
            Long id,
            String projectCode,
            String name,
            String projectType,
            String ownerCompany,
            String customer,
            String status,
            String approvedBudget) {
    }

    public record EmploymentChoice(Long id, Long employeeId, String label) {
    }

    public record AssignmentChoice(Long id, Long employeeId, Long employmentId, String label) {
    }

    /**
     * 两项都已知时总额为两者之和。任一项未知则总额未知，不用零代替未编制金额。
     */
    public static BigDecimal budgetTotal(BigDecimal labor, BigDecimal nonlabor) {
        if (labor == null || nonlabor == null) {
            return null;
        }
        return labor.add(nonlabor);
    }

    public List<Row> list() {
        Map<Long, Company> companyMap = companyMap();
        Map<Long, LegalPerson> legalMap = legalMap();
        Map<Long, Person> personMap = personMap();
        return projects.findAllByOrderByProjectCodeAsc().stream()
                .map(project -> new Row(
                        project.id,
                        project.projectCode,
                        project.name,
                        project.projectType,
                        companyLabel(companyMap.get(project.ownerCompanyId)),
                        customerLabel(project, companyMap, legalMap, personMap),
                        project.status))
                .toList();
    }

    public Header header(Long id) {
        Project project = get(id);
        String approved = "尚未批准";
        if (project.approvedBudgetVersionId != null) {
            approved = budgets.findById(project.approvedBudgetVersionId)
                    .map(version -> "版本 " + version.versionNumber)
                    .orElse("尚未批准");
        }
        return new Header(
                project.id,
                project.projectCode,
                project.name,
                project.projectType,
                companyLabel(companies.findById(project.ownerCompanyId).orElse(null)),
                customerLabel(project, companyMap(), legalMap(), personMap()),
                project.status,
                approved);
    }

    public ProjectForm form(Long id) {
        Project project = get(id);
        ProjectForm form = new ProjectForm();
        form.setId(project.id);
        form.setProjectCode(project.projectCode);
        form.setName(project.name);
        form.setProjectType(project.projectType);
        form.setProjectScopeDescription(project.projectScopeDescription);
        form.setActualStartAt(project.actualStartAt);
        form.setActualCompletedAt(project.actualCompletedAt);
        form.setClosedAt(project.closedAt);
        form.setBudgetCurrency(project.budgetCurrency);
        form.setCustomerCompanyId(project.customerCompanyId);
        form.setCustomerLegalPersonId(project.customerLegalPersonId);
        form.setCustomerPersonId(project.customerPersonId);
        form.setOwnerCompanyId(project.ownerCompanyId);
        form.setLeadDepartmentId(project.leadDepartmentId);
        form.setProjectManagerEmployeeId(project.projectManagerEmployeeId);
        form.setPlannedStartAt(project.plannedStartAt);
        form.setPlannedEndAt(project.plannedEndAt);
        form.setStatus(project.status);
        versions.findByProjectIdOrderByVersionNumberAsc(id).stream()
                .filter(version -> version.validTo == null)
                .findFirst()
                .ifPresent(version -> form.setBasisDocumentId(version.basisDocumentId));
        return form;
    }

    public ProjectDetailForm detailForm(Long id) {
        get(id);
        ProjectDetailForm form = new ProjectDetailForm();
        form.setMembers(new ArrayList<>(members.findByProjectIdOrderByIdAsc(id).stream().map(this::toMember).toList()));
        form.setBudgets(new ArrayList<>(budgets.findByProjectIdOrderByVersionNumberAsc(id).stream().map(this::toBudget).toList()));
        form.setDeliveries(new ArrayList<>(deliveries.findByProjectIdOrderByIdAsc(id).stream().map(this::toDelivery).toList()));
        form.setSupports(new ArrayList<>(supports.findByProjectIdOrderByIdAsc(id).stream().map(this::toSupport).toList()));
        return form;
    }

    public List<ProjectVersion> versionHistory(Long id) {
        get(id);
        return versions.findByProjectIdOrderByVersionNumberAsc(id);
    }

    public List<EmploymentChoice> employmentChoices() {
        Map<Long, Employee> employeeMap = employees.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Person> people = personMap();
        return employments.findAll().stream()
                .map(row -> new EmploymentChoice(row.id, row.employeeId, employmentLabel(row, employeeMap, people)))
                .toList();
    }

    public List<AssignmentChoice> assignmentChoices() {
        Map<Long, Department> departmentMap = departments.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        return assignments.findAll().stream()
                .map(row -> new AssignmentChoice(row.id, row.employeeId, row.employmentId, assignmentLabel(row, departmentMap)))
                .toList();
    }

    @Transactional
    public long save(ProjectForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validateHeader(form, errors);
        errors.throwIfAny();

        Project project = form.getId() == null ? new Project() : get(form.getId());
        applyHeader(project, form);
        project = projects.save(project);
        recordVersion(project, form);
        return project.id;
    }

    @Transactional
    public void saveDetails(Long projectId, ProjectDetailForm form) {
        get(projectId);
        normalizeDetails(form);
        FieldErrors errors = new FieldErrors();
        validateDetails(form, errors);
        errors.throwIfAny();
        syncMembers(projectId, form.getMembers());
        syncBudgets(projectId, form.getBudgets());
        syncDeliveries(projectId, form.getDeliveries());
        syncSupports(projectId, form.getSupports());
        refreshApprovedBudget(projectId);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (workLogLinks.projectUsed(id)) {
            throw new FieldErrorsException(Map.of("*", "该项目仍有工时记录，不能删除"));
        }
        if (costLinks.projectUsed(id)) {
            throw new FieldErrorsException(Map.of("*", "该项目仍有费用分配，不能删除"));
        }
        if (fulfillmentLinks.projectUsed(id)) {
            throw new FieldErrorsException(Map.of("*", "该项目仍有交付记录，不能删除"));
        }
        if (contractLinks.projectUsed(id)) {
            throw new FieldErrorsException(Map.of("*", "该项目仍关联合同，不能删除"));
        }
        members.findByProjectIdOrderByIdAsc(id).forEach(members::delete);
        budgets.findByProjectIdOrderByVersionNumberAsc(id).forEach(budgets::delete);
        deliveries.findByProjectIdOrderByIdAsc(id).forEach(deliveries::delete);
        supports.findByProjectIdOrderByIdAsc(id).forEach(supports::delete);
        versions.findByProjectIdOrderByVersionNumberAsc(id).forEach(versions::delete);
        projects.deleteById(id);
    }

    private void validateHeader(ProjectForm form, FieldErrors errors) {
        if (form.getProjectCode() == null) {
            errors.add("projectCode", "请填写项目编号");
        } else {
            for (Project other : projects.findByProjectCode(form.getProjectCode())) {
                if (!other.id.equals(form.getId())) {
                    errors.add("projectCode", "项目编号已存在");
                }
            }
        }
        if (form.getName() == null) {
            errors.add("name", "请填写项目名称");
        }
        if (form.getProjectType() == null || !Codes.PROJECT_TYPES.contains(form.getProjectType())) {
            errors.add("projectType", "请选择项目类型");
        }
        if (form.getStatus() == null || !Codes.PROJECT_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择项目状态");
        }
        if (form.getOwnerCompanyId() == null) {
            errors.add("ownerCompanyId", "请选择项目管理公司");
        } else if (companies.findById(form.getOwnerCompanyId()).isEmpty()) {
            errors.add("ownerCompanyId", "项目管理公司不存在");
        }
        int customers = 0;
        if (form.getCustomerCompanyId() != null) {
            customers++;
            if (companies.findById(form.getCustomerCompanyId()).isEmpty()) {
                errors.add("customerCompanyId", "客户公司不存在");
            }
        }
        if (form.getCustomerLegalPersonId() != null) {
            customers++;
            if (legalPersons.findById(form.getCustomerLegalPersonId()).isEmpty()) {
                errors.add("customerLegalPersonId", "客户法人不存在");
            }
        }
        if (form.getCustomerPersonId() != null) {
            customers++;
            if (persons.findById(form.getCustomerPersonId()).isEmpty()) {
                errors.add("customerPersonId", "客户自然人不存在");
            }
        }
        if (customers > 1) {
            errors.add("customerCompanyId", "客户在公司、法人、自然人中最多选择一个");
        }
        if (form.getLeadDepartmentId() != null) {
            Department department = departments.findById(form.getLeadDepartmentId()).orElse(null);
            if (department == null) {
                errors.add("leadDepartmentId", "牵头部门不存在");
            } else if (form.getOwnerCompanyId() != null && !form.getOwnerCompanyId().equals(department.companyId)) {
                errors.add("leadDepartmentId", "牵头部门须属于项目管理公司");
            }
        }
        if (form.getProjectManagerEmployeeId() != null && employees.findById(form.getProjectManagerEmployeeId()).isEmpty()) {
            errors.add("projectManagerEmployeeId", "项目经理不存在");
        }
        if (form.getPlannedStartAt() == null || form.getPlannedEndAt() == null) {
            errors.add("plannedStartAt", "请填写计划开始和计划结束");
        } else if (form.getPlannedEndAt().isBefore(form.getPlannedStartAt())) {
            errors.add("plannedEndAt", "计划结束不能早于计划开始");
        }
        if (form.getActualStartAt() != null && form.getActualCompletedAt() != null
                && form.getActualCompletedAt().isBefore(form.getActualStartAt())) {
            errors.add("actualCompletedAt", "实际完成不能早于实际启动");
        }
        if ("结项".equals(form.getStatus()) && form.getClosedAt() == null) {
            errors.add("closedAt", "结项时须填写结项日");
        }
        if ("筹备".equals(form.getStatus())
                && (form.getActualStartAt() != null || form.getActualCompletedAt() != null || form.getClosedAt() != null)) {
            errors.add("status", "筹备阶段实际启动、完成和结项日留空");
        }
        if (!"结项".equals(form.getStatus()) && form.getClosedAt() != null) {
            errors.add("closedAt", "尚未结项时结项日留空");
        }
        documents.require(form.getBasisDocumentId(), errors, "basisDocumentId");
    }

    private void validateDetails(ProjectDetailForm form, FieldErrors errors) {
        List<MemberForm> memberRows = form.getMembers().stream().filter(row -> !row.isRemoved()).toList();
        for (int i = 0; i < form.getMembers().size(); i++) {
            MemberForm row = form.getMembers().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "members[" + i + "]";
            if (row.getEmployeeId() == null || employees.findById(row.getEmployeeId()).isEmpty()) {
                errors.add(prefix + ".employeeId", "请选择项目成员");
            }
            EmployeeEmployment employment = null;
            if (row.getEmploymentId() == null) {
                errors.add(prefix + ".employmentId", "请选择成员的雇佣记录");
            } else {
                employment = employments.findById(row.getEmploymentId()).orElse(null);
                if (employment == null) {
                    errors.add(prefix + ".employmentId", "雇佣记录不存在");
                } else if (row.getEmployeeId() != null && !row.getEmployeeId().equals(employment.employeeId)) {
                    errors.add(prefix + ".employmentId", "雇佣记录不属于该员工");
                }
            }
            EmployeeDepartmentAssignment assignment = null;
            if (row.getDepartmentAssignmentId() != null) {
                assignment = assignments.findById(row.getDepartmentAssignmentId()).orElse(null);
                if (assignment == null) {
                    errors.add(prefix + ".departmentAssignmentId", "任职记录不存在");
                } else if (row.getEmployeeId() != null && !row.getEmployeeId().equals(assignment.employeeId)) {
                    errors.add(prefix + ".departmentAssignmentId", "任职不属于该员工");
                } else if (employment != null && !employment.id.equals(assignment.employmentId)) {
                    errors.add(prefix + ".departmentAssignmentId", "任职不属于该员工的这条雇佣");
                }
            }
            if (row.getProjectRole() == null || !Codes.PROJECT_ROLES.contains(row.getProjectRole())) {
                errors.add(prefix + ".projectRole", "请选择项目角色");
            }
            if (row.getPlannedHours() != null && row.getPlannedHours().signum() < 0) {
                errors.add(prefix + ".plannedHours", "计划工时不能为负；留空表示尚未编制");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写成员生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            if (employment != null && row.getValidFrom() != null
                    && !Periods.contains(employment.employmentStartDate, employment.employmentEndDate, row.getValidFrom(), row.getValidTo())) {
                errors.add(prefix + ".validFrom", "成员期间必须落在雇佣有效期内");
            }
            if (assignment != null && row.getValidFrom() != null
                    && !Periods.contains(assignment.validFrom, assignment.validTo, row.getValidFrom(), row.getValidTo())) {
                errors.add(prefix + ".validFrom", "成员期间必须落在任职有效期内");
            }
        }
        for (int i = 0; i < memberRows.size(); i++) {
            for (int j = i + 1; j < memberRows.size(); j++) {
                MemberForm left = memberRows.get(i);
                MemberForm right = memberRows.get(j);
                if (Cmp.eq(left.getEmployeeId(), right.getEmployeeId())
                        && Periods.overlaps(left.getValidFrom(), left.getValidTo(), right.getValidFrom(), right.getValidTo())) {
                    errors.add("members", "同一员工的成员期间不能重叠");
                }
            }
        }

        List<BudgetForm> budgetRows = form.getBudgets().stream().filter(row -> !row.isRemoved()).toList();
        Set<Integer> numbers = new HashSet<>();
        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        int openBudgets = 0;
        for (int i = 0; i < form.getBudgets().size(); i++) {
            BudgetForm row = form.getBudgets().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "budgets[" + i + "]";
            if (row.getVersionNumber() == null || row.getVersionNumber() < 1) {
                errors.add(prefix + ".versionNumber", "请填写大于 0 的预算版本号");
            } else if (!numbers.add(row.getVersionNumber())) {
                errors.add(prefix + ".versionNumber", "预算版本号在本项目内不能重复");
            }
            if (row.getPlannedLaborHours() != null && row.getPlannedLaborHours().signum() < 0) {
                errors.add(prefix + ".plannedLaborHours", "计划工时不能为负；留空表示尚未编制");
            }
            if (row.getLaborCostBudgetAmount() != null && row.getLaborCostBudgetAmount().signum() < 0) {
                errors.add(prefix + ".laborCostBudgetAmount", "人工预算不能为负；留空表示尚未编制");
            }
            if (row.getNonlaborCostBudgetAmount() != null && row.getNonlaborCostBudgetAmount().signum() < 0) {
                errors.add(prefix + ".nonlaborCostBudgetAmount", "非人工预算不能为负；留空表示尚未编制");
            }
            boolean amountKnown = row.getLaborCostBudgetAmount() != null || row.getNonlaborCostBudgetAmount() != null;
            if (amountKnown && row.getBudgetCurrency() == null) {
                errors.add(prefix + ".budgetCurrency", "填写金额时须填写币种");
            }
            boolean laborKnown = row.getLaborCostBudgetAmount() != null || row.getPlannedLaborHours() != null;
            if (laborKnown && (row.getBudgetCostBasis() == null || !Codes.BUDGET_COST_BASIS.contains(row.getBudgetCostBasis()))) {
                errors.add(prefix + ".budgetCostBasis", "填写人工金额或计划工时时须选择成本口径");
            } else if (row.getBudgetCostBasis() != null && !Codes.BUDGET_COST_BASIS.contains(row.getBudgetCostBasis())) {
                errors.add(prefix + ".budgetCostBasis", "请选择成本口径");
            }
            String approval = row.getApprovalStatus();
            if (approval == null || !Codes.BUDGET_APPROVAL_STATUS.contains(approval)) {
                errors.add(prefix + ".approvalStatus", "请选择预算审批状态");
            } else if ("草稿".equals(approval)) {
                if (row.getSubmittedAt() != null || row.getApprovedByEmployeeId() != null || row.getApprovedAt() != null) {
                    errors.add(prefix + ".approvalStatus", "草稿不填写提交和审批");
                }
            } else if (row.getSubmittedAt() == null) {
                errors.add(prefix + ".submittedAt", "待审批、已批准或驳回须填写提交时间");
            }
            if ("已批准".equals(approval) && (row.getApprovedByEmployeeId() == null || row.getApprovedAt() == null)) {
                errors.add(prefix + ".approvedByEmployeeId", "已批准的预算须填写审批人和审批时间");
            }
            if (row.getApprovedByEmployeeId() != null && employees.findById(row.getApprovedByEmployeeId()).isEmpty()) {
                errors.add(prefix + ".approvedByEmployeeId", "预算审批人不存在");
            }
            if (!"已批准".equals(approval) && (row.getApprovedByEmployeeId() != null || row.getApprovedAt() != null)) {
                errors.add(prefix + ".approvedByEmployeeId", "尚未批准时审批人和审批时间留空");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写预算生效日");
            } else {
                errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
                froms.add(row.getValidFrom());
                tos.add(row.getValidTo());
                if (row.getValidTo() == null) {
                    openBudgets++;
                }
            }
            documents.require(row.getBasisDocumentId(), errors, prefix + ".basisDocumentId");
        }
        if (openBudgets > 1) {
            errors.add("budgets", "同一时间只能有一条未结束的预算版本");
        }
        errors.add("budgets", Periods.overlapError(froms, tos, "预算版本"));

        List<DeliveryForm> deliveryRows = form.getDeliveries().stream().filter(row -> !row.isRemoved()).toList();
        for (int i = 0; i < form.getDeliveries().size(); i++) {
            DeliveryForm row = form.getDeliveries().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "deliveries[" + i + "]";
            if (row.getDeliveryCompanyId() == null || companies.findById(row.getDeliveryCompanyId()).isEmpty()) {
                errors.add(prefix + ".deliveryCompanyId", "请选择交付公司");
            }
            if (row.getLeadDepartmentId() != null) {
                Department department = departments.findById(row.getLeadDepartmentId()).orElse(null);
                if (department == null) {
                    errors.add(prefix + ".leadDepartmentId", "交付牵头部门不存在");
                } else if (row.getDeliveryCompanyId() != null && !row.getDeliveryCompanyId().equals(department.companyId)) {
                    errors.add(prefix + ".leadDepartmentId", "交付牵头部门须属于该交付公司");
                }
            }
            if (row.getResponsibilityDescription() == null) {
                errors.add(prefix + ".responsibilityDescription", "请填写交付责任");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写交付生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            documents.require(row.getBasisDocumentId(), errors, prefix + ".basisDocumentId");
        }
        for (int i = 0; i < deliveryRows.size(); i++) {
            for (int j = i + 1; j < deliveryRows.size(); j++) {
                DeliveryForm left = deliveryRows.get(i);
                DeliveryForm right = deliveryRows.get(j);
                if (Cmp.eq(left.getDeliveryCompanyId(), right.getDeliveryCompanyId())
                        && Periods.overlaps(left.getValidFrom(), left.getValidTo(), right.getValidFrom(), right.getValidTo())) {
                    errors.add("deliveries", "同一交付公司的责任期间不能重叠");
                }
            }
        }

        List<SupportForm> supportRows = form.getSupports().stream().filter(row -> !row.isRemoved()).toList();
        for (int i = 0; i < form.getSupports().size(); i++) {
            SupportForm row = form.getSupports().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "supports[" + i + "]";
            if (row.getDepartmentId() == null || departments.findById(row.getDepartmentId()).isEmpty()) {
                errors.add(prefix + ".departmentId", "请选择支持部门");
            }
            if (row.getResponsibilityDescription() == null) {
                errors.add(prefix + ".responsibilityDescription", "请填写支持责任");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写支持生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
        }
        for (int i = 0; i < supportRows.size(); i++) {
            for (int j = i + 1; j < supportRows.size(); j++) {
                SupportForm left = supportRows.get(i);
                SupportForm right = supportRows.get(j);
                if (Cmp.eq(left.getDepartmentId(), right.getDepartmentId())
                        && Periods.overlaps(left.getValidFrom(), left.getValidTo(), right.getValidFrom(), right.getValidTo())) {
                    errors.add("supports", "同一支持部门的责任期间不能重叠");
                }
            }
        }
    }

    private void applyHeader(Project project, ProjectForm form) {
        project.projectCode = form.getProjectCode();
        project.name = form.getName();
        project.projectType = form.getProjectType();
        project.projectScopeDescription = form.getProjectScopeDescription();
        project.actualStartAt = form.getActualStartAt();
        project.actualCompletedAt = form.getActualCompletedAt();
        project.closedAt = form.getClosedAt();
        project.budgetCurrency = form.getBudgetCurrency();
        project.customerCompanyId = form.getCustomerCompanyId();
        project.customerLegalPersonId = form.getCustomerLegalPersonId();
        project.customerPersonId = form.getCustomerPersonId();
        project.ownerCompanyId = form.getOwnerCompanyId();
        project.leadDepartmentId = form.getLeadDepartmentId();
        project.projectManagerEmployeeId = form.getProjectManagerEmployeeId();
        project.plannedStartAt = form.getPlannedStartAt();
        project.plannedEndAt = form.getPlannedEndAt();
        project.status = form.getStatus();
    }

    private void recordVersion(Project project, ProjectForm form) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDate today = LocalDate.now();
        List<ProjectVersion> existing = versions.findByProjectIdOrderByVersionNumberAsc(project.id);
        ProjectVersion open = existing.stream().filter(version -> version.validTo == null).findFirst().orElse(null);
        if (open == null) {
            ProjectVersion created = new ProjectVersion();
            created.projectId = project.id;
            created.versionNumber = nextNumber(existing);
            copySnapshot(project, created);
            created.validFrom = project.plannedStartAt;
            created.recordedAt = now;
            created.basisDocumentId = form.getBasisDocumentId();
            versions.save(created);
            return;
        }
        if (!snapshotChanged(open, project)) {
            open.basisDocumentId = form.getBasisDocumentId();
            versions.save(open);
            return;
        }
        if (open.validFrom != null && open.validFrom.isBefore(today)) {
            open.validTo = today;
            versions.save(open);
            ProjectVersion next = new ProjectVersion();
            next.projectId = project.id;
            next.versionNumber = nextNumber(existing);
            copySnapshot(project, next);
            next.validFrom = today;
            next.recordedAt = now;
            next.basisDocumentId = form.getBasisDocumentId();
            versions.save(next);
            return;
        }
        copySnapshot(project, open);
        open.recordedAt = now;
        open.basisDocumentId = form.getBasisDocumentId();
        versions.save(open);
    }

    private int nextNumber(List<ProjectVersion> existing) {
        int max = 0;
        for (ProjectVersion version : existing) {
            if (version.versionNumber != null) {
                max = Math.max(max, version.versionNumber);
            }
        }
        return max + 1;
    }

    private boolean snapshotChanged(ProjectVersion version, Project project) {
        return !Cmp.eq(version.customerCompanyId, project.customerCompanyId)
                || !Cmp.eq(version.customerLegalPersonId, project.customerLegalPersonId)
                || !Cmp.eq(version.customerPersonId, project.customerPersonId)
                || !Cmp.eq(version.ownerCompanyId, project.ownerCompanyId)
                || !Cmp.eq(version.leadDepartmentId, project.leadDepartmentId)
                || !Cmp.eq(version.projectManagerEmployeeId, project.projectManagerEmployeeId)
                || !Cmp.eq(version.projectType, project.projectType)
                || !Cmp.eq(version.projectScopeDescription, project.projectScopeDescription)
                || !Cmp.eq(version.name, project.name)
                || !Cmp.eq(version.plannedStartAt, project.plannedStartAt)
                || !Cmp.eq(version.plannedEndAt, project.plannedEndAt);
    }

    private void copySnapshot(Project project, ProjectVersion version) {
        version.customerCompanyId = project.customerCompanyId;
        version.customerLegalPersonId = project.customerLegalPersonId;
        version.customerPersonId = project.customerPersonId;
        version.ownerCompanyId = project.ownerCompanyId;
        version.leadDepartmentId = project.leadDepartmentId;
        version.projectManagerEmployeeId = project.projectManagerEmployeeId;
        version.projectType = project.projectType;
        version.projectScopeDescription = project.projectScopeDescription;
        version.name = project.name;
        version.plannedStartAt = project.plannedStartAt;
        version.plannedEndAt = project.plannedEndAt;
    }

    private void refreshApprovedBudget(Long projectId) {
        Project project = get(projectId);
        LocalDate today = LocalDate.now();
        Long current = null;
        for (ProjectBudgetVersion version : budgets.findByProjectIdOrderByVersionNumberAsc(projectId)) {
            if (!"已批准".equals(version.approvalStatus) || version.validFrom == null) {
                continue;
            }
            boolean started = !version.validFrom.isAfter(today);
            boolean stillOpen = version.validTo == null || today.isBefore(version.validTo);
            if (started && stillOpen) {
                current = version.id;
                break;
            }
        }
        project.approvedBudgetVersionId = current;
        projects.save(project);
    }

    private void syncMembers(Long projectId, List<MemberForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (MemberForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            ProjectMember entity = row.getId() == null ? new ProjectMember() : members.findById(row.getId()).orElseThrow(() ->
                    new FieldErrorsException(Map.of("*", "项目成员不存在")));
            if (entity.projectId != null && !projectId.equals(entity.projectId)) {
                throw new FieldErrorsException(Map.of("*", "项目成员不属于当前项目"));
            }
            entity.projectId = projectId;
            entity.employeeId = row.getEmployeeId();
            entity.employmentId = row.getEmploymentId();
            entity.departmentAssignmentId = row.getDepartmentAssignmentId();
            entity.projectRole = row.getProjectRole();
            entity.plannedHours = row.getPlannedHours();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            keep.add(members.save(entity).id);
        }
        for (ProjectMember existing : members.findByProjectIdOrderByIdAsc(projectId)) {
            if (!keep.contains(existing.id)) {
                members.delete(existing);
            }
        }
    }

    private void syncBudgets(Long projectId, List<BudgetForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (BudgetForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            ProjectBudgetVersion entity = row.getId() == null ? new ProjectBudgetVersion() : budgets.findById(row.getId()).orElseThrow(() ->
                    new FieldErrorsException(Map.of("*", "预算版本不存在")));
            if (entity.projectId != null && !projectId.equals(entity.projectId)) {
                throw new FieldErrorsException(Map.of("*", "预算版本不属于当前项目"));
            }
            entity.projectId = projectId;
            entity.versionNumber = row.getVersionNumber();
            entity.budgetCurrency = row.getBudgetCurrency();
            entity.plannedLaborHours = row.getPlannedLaborHours();
            entity.laborCostBudgetAmount = row.getLaborCostBudgetAmount();
            entity.nonlaborCostBudgetAmount = row.getNonlaborCostBudgetAmount();
            entity.budgetCostBasis = row.getBudgetCostBasis();
            entity.approvalStatus = row.getApprovalStatus();
            entity.submittedAt = minute(row.getSubmittedAt());
            entity.approvedByEmployeeId = row.getApprovedByEmployeeId();
            entity.approvedAt = minute(row.getApprovedAt());
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.basisDocumentId = row.getBasisDocumentId();
            keep.add(budgets.save(entity).id);
        }
        for (ProjectBudgetVersion existing : budgets.findByProjectIdOrderByVersionNumberAsc(projectId)) {
            if (!keep.contains(existing.id)) {
                budgets.delete(existing);
            }
        }
    }

    private void syncDeliveries(Long projectId, List<DeliveryForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (DeliveryForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            ProjectDeliveryCompany entity = row.getId() == null ? new ProjectDeliveryCompany() : deliveries.findById(row.getId()).orElseThrow(() ->
                    new FieldErrorsException(Map.of("*", "交付公司不存在")));
            if (entity.projectId != null && !projectId.equals(entity.projectId)) {
                throw new FieldErrorsException(Map.of("*", "交付公司不属于当前项目"));
            }
            entity.projectId = projectId;
            entity.deliveryCompanyId = row.getDeliveryCompanyId();
            entity.leadDepartmentId = row.getLeadDepartmentId();
            entity.responsibilityDescription = row.getResponsibilityDescription();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.basisDocumentId = row.getBasisDocumentId();
            keep.add(deliveries.save(entity).id);
        }
        for (ProjectDeliveryCompany existing : deliveries.findByProjectIdOrderByIdAsc(projectId)) {
            if (!keep.contains(existing.id)) {
                deliveries.delete(existing);
            }
        }
    }

    private void syncSupports(Long projectId, List<SupportForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (SupportForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            ProjectSupportDepartment entity = row.getId() == null ? new ProjectSupportDepartment() : supports.findById(row.getId()).orElseThrow(() ->
                    new FieldErrorsException(Map.of("*", "支持部门不存在")));
            if (entity.projectId != null && !projectId.equals(entity.projectId)) {
                throw new FieldErrorsException(Map.of("*", "支持部门不属于当前项目"));
            }
            entity.projectId = projectId;
            entity.departmentId = row.getDepartmentId();
            entity.responsibilityDescription = row.getResponsibilityDescription();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            keep.add(supports.save(entity).id);
        }
        for (ProjectSupportDepartment existing : supports.findByProjectIdOrderByIdAsc(projectId)) {
            if (!keep.contains(existing.id)) {
                supports.delete(existing);
            }
        }
    }

    private MemberForm toMember(ProjectMember row) {
        MemberForm form = new MemberForm();
        form.setId(row.id);
        form.setEmployeeId(row.employeeId);
        form.setEmploymentId(row.employmentId);
        form.setDepartmentAssignmentId(row.departmentAssignmentId);
        form.setProjectRole(row.projectRole);
        form.setPlannedHours(row.plannedHours);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        return form;
    }

    private BudgetForm toBudget(ProjectBudgetVersion row) {
        BudgetForm form = new BudgetForm();
        form.setId(row.id);
        form.setVersionNumber(row.versionNumber);
        form.setBudgetCurrency(row.budgetCurrency);
        form.setPlannedLaborHours(row.plannedLaborHours);
        form.setLaborCostBudgetAmount(row.laborCostBudgetAmount);
        form.setNonlaborCostBudgetAmount(row.nonlaborCostBudgetAmount);
        form.setBudgetCostBasis(row.budgetCostBasis);
        form.setApprovalStatus(row.approvalStatus);
        form.setSubmittedAt(row.submittedAt);
        form.setApprovedByEmployeeId(row.approvedByEmployeeId);
        form.setApprovedAt(row.approvedAt);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setBasisDocumentId(row.basisDocumentId);
        return form;
    }

    private DeliveryForm toDelivery(ProjectDeliveryCompany row) {
        DeliveryForm form = new DeliveryForm();
        form.setId(row.id);
        form.setDeliveryCompanyId(row.deliveryCompanyId);
        form.setLeadDepartmentId(row.leadDepartmentId);
        form.setResponsibilityDescription(row.responsibilityDescription);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setBasisDocumentId(row.basisDocumentId);
        return form;
    }

    private SupportForm toSupport(ProjectSupportDepartment row) {
        SupportForm form = new SupportForm();
        form.setId(row.id);
        form.setDepartmentId(row.departmentId);
        form.setResponsibilityDescription(row.responsibilityDescription);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        return form;
    }

    private void normalize(ProjectForm form) {
        form.setProjectCode(Text.blankToNull(form.getProjectCode()));
        form.setName(Text.blankToNull(form.getName()));
        form.setProjectType(Text.blankToNull(form.getProjectType()));
        form.setProjectScopeDescription(Text.blankToNull(form.getProjectScopeDescription()));
        form.setBudgetCurrency(Text.upperOrNull(form.getBudgetCurrency()));
        form.setStatus(Text.blankToNull(form.getStatus()));
    }

    private void normalizeDetails(ProjectDetailForm form) {
        if (form.getMembers() == null) {
            form.setMembers(List.of());
        }
        if (form.getBudgets() == null) {
            form.setBudgets(List.of());
        }
        if (form.getDeliveries() == null) {
            form.setDeliveries(List.of());
        }
        if (form.getSupports() == null) {
            form.setSupports(List.of());
        }
        for (MemberForm row : form.getMembers()) {
            row.setProjectRole(Text.blankToNull(row.getProjectRole()));
        }
        for (BudgetForm row : form.getBudgets()) {
            row.setBudgetCurrency(Text.upperOrNull(row.getBudgetCurrency()));
            row.setBudgetCostBasis(Text.blankToNull(row.getBudgetCostBasis()));
            row.setApprovalStatus(Text.blankToNull(row.getApprovalStatus()));
        }
        for (DeliveryForm row : form.getDeliveries()) {
            row.setResponsibilityDescription(Text.blankToNull(row.getResponsibilityDescription()));
        }
        for (SupportForm row : form.getSupports()) {
            row.setResponsibilityDescription(Text.blankToNull(row.getResponsibilityDescription()));
        }
    }

    private Project get(Long id) {
        return projects.findById(id).orElseThrow(NotFoundException::new);
    }

    private Map<Long, Company> companyMap() {
        return companies.findAll().stream().collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
    }

    private Map<Long, LegalPerson> legalMap() {
        return legalPersons.findAll().stream().collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
    }

    private Map<Long, Person> personMap() {
        return persons.findAll().stream().collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
    }

    private String companyLabel(Company company) {
        return company == null ? "未找到公司" : company.companyCode + " " + company.name;
    }

    private String customerLabel(Project project, Map<Long, Company> companyMap, Map<Long, LegalPerson> legalMap, Map<Long, Person> personMap) {
        if (project.customerCompanyId != null) {
            return "公司 " + companyLabel(companyMap.get(project.customerCompanyId));
        }
        if (project.customerLegalPersonId != null) {
            LegalPerson legalPerson = legalMap.get(project.customerLegalPersonId);
            return legalPerson == null ? "法人 #" + project.customerLegalPersonId : "法人 " + legalPerson.legalName;
        }
        if (project.customerPersonId != null) {
            Person person = personMap.get(project.customerPersonId);
            return person == null ? "自然人 #" + project.customerPersonId : "自然人 " + person.name;
        }
        return "未指定客户";
    }

    private String employmentLabel(EmployeeEmployment row, Map<Long, Employee> employeeMap, Map<Long, Person> people) {
        Employee employee = employeeMap.get(row.employeeId);
        String who = employee == null ? "员工 #" + row.employeeId : employee.employeeCode;
        if (employee != null && people.get(employee.personId) != null) {
            who = who + " " + people.get(employee.personId).name;
        }
        return who + " · " + row.employeeNumber + " " + row.employmentStartDate
                + (row.employmentEndDate == null ? " 起" : " 至 " + row.employmentEndDate);
    }

    private String assignmentLabel(EmployeeDepartmentAssignment row, Map<Long, Department> departmentMap) {
        Department department = departmentMap.get(row.departmentId);
        String name = department == null ? "部门 #" + row.departmentId : department.departmentCode + " " + department.name;
        return name + " · " + row.assignmentType + " " + row.validFrom
                + (row.validTo == null ? " 起" : " 至 " + row.validTo);
    }

    private LocalDateTime minute(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }
}
