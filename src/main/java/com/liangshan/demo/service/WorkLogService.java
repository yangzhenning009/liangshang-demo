package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.EmployeeReportingLine;
import com.liangshan.demo.domain.LaborCostRate;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ProjectContract;
import com.liangshan.demo.domain.ProjectMember;
import com.liangshan.demo.domain.WorkLog;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeReportingLineRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LaborCostRateRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import com.liangshan.demo.repository.ProjectMemberRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.WorkLogRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.WorkLogDecisionForm;
import com.liangshan.demo.web.form.WorkLogForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository logs;
    private final LaborCostRateRepository rates;
    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final EmployeeReportingLineRepository reportingLines;
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final ProjectContractRepository projectContracts;
    private final ContractStageRepository stages;
    private final ContractRepository contracts;
    private final CostOccurrenceService costOccurrences;

    public record Row(
            Long id,
            String employeeLabel,
            LocalDate workDate,
            BigDecimal hours,
            String projectLabel,
            String description,
            String approvalStatus,
            String recordStatus,
            BigDecimal hourlyCostAmountSnapshot,
            BigDecimal laborCostAmount,
            String laborCostCurrency,
            Long laborCostRateId,
            boolean billable) {
    }

    public List<Row> list() {
        return logs.findAllByOrderByWorkDateDescIdDesc().stream().map(this::row).toList();
    }

    public List<Row> queue() {
        return logs.findByApprovalStatusOrderBySubmittedAtAscIdAsc(Codes.WORK_PENDING).stream().map(this::row).toList();
    }

    public WorkLog get(Long id) {
        return logs.findById(id).orElseThrow(NotFoundException::new);
    }

    public WorkLogForm form(Long id) {
        WorkLog log = get(id);
        WorkLogForm form = new WorkLogForm();
        form.setId(log.id);
        form.setEmployeeId(log.employeeId);
        form.setEmploymentId(log.employmentId);
        form.setDepartmentAssignmentId(log.departmentAssignmentId);
        form.setProjectId(log.projectId);
        form.setContractStageId(log.contractStageId);
        form.setWorkDate(log.workDate);
        form.setHours(log.hours);
        form.setWorkDescription(log.workDescription);
        form.setBillable(log.billable);
        form.setSupersedesWorkLogId(log.supersedesWorkLogId);
        form.setSubmitForApproval(Codes.WORK_PENDING.equals(log.approvalStatus));
        return form;
    }

    @Transactional
    public long save(WorkLogForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        WorkLog stored = null;
        if (form.getId() != null) {
            stored = logs.findById(form.getId()).orElse(null);
            if (stored == null) {
                errors.add("id", "工时不存在");
            } else if (Codes.WORK_APPROVED.equals(stored.approvalStatus)) {
                errors.add("hours", "已通过的工时不能直接修改，请登记更正");
            } else if (Codes.WORK_PENDING.equals(stored.approvalStatus)) {
                errors.add("hours", "待审工时不能修改");
            }
        }
        validateBody(form, errors);
        errors.throwIfAny();
        WorkLog log = stored == null ? new WorkLog() : stored;
        apply(log, form);
        log.recordStatus = Codes.WORK_RECORD_EFFECTIVE;
        log.approvedByEmployeeId = null;
        log.approvedAt = null;
        log.approvalComment = null;
        if (form.isSubmitForApproval()) {
            log.approvalStatus = Codes.WORK_PENDING;
            log.submittedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        } else {
            log.approvalStatus = Codes.WORK_DRAFT;
            log.submittedAt = null;
        }
        return logs.save(log).id;
    }

    @Transactional
    public void approve(Long id, WorkLogDecisionForm decision) {
        WorkLog log = get(id);
        FieldErrors errors = new FieldErrors();
        if (!Codes.WORK_PENDING.equals(log.approvalStatus)) {
            errors.add("approvalStatus", "只有待审工时可以批准");
        }
        requireApprover(log, decision, errors, false);
        LaborCostRate rate = applicableRate(log);
        if (rate == null) {
            errors.add("hours", "工作日没有覆盖该雇佣的已批准标准成本率");
        }
        if (log.supersedesWorkLogId != null) {
            WorkLog prior = logs.findById(log.supersedesWorkLogId).orElse(null);
            if (prior == null || !Codes.WORK_APPROVED.equals(prior.approvalStatus)
                    || !Codes.WORK_RECORD_EFFECTIVE.equals(prior.recordStatus)) {
                errors.add("supersedesWorkLogId", "更正只能替代一条仍有效且已通过的工时");
            }
        }
        errors.throwIfAny();
        log.approvalStatus = Codes.WORK_APPROVED;
        log.recordStatus = Codes.WORK_RECORD_EFFECTIVE;
        log.approvedByEmployeeId = decision.getApprovedByEmployeeId();
        log.approvedAt = decision.getApprovedAt().truncatedTo(ChronoUnit.MINUTES);
        log.approvalComment = Text.blankToNull(decision.getApprovalComment());
        logs.save(log);
        if (log.supersedesWorkLogId != null) {
            WorkLog prior = logs.findById(log.supersedesWorkLogId).orElseThrow();
            prior.recordStatus = Codes.WORK_RECORD_SUPERSEDED;
            logs.save(prior);
        }
        costOccurrences.recordApprovedLabor(log, rate);
    }

    @Transactional
    public void reject(Long id, WorkLogDecisionForm decision) {
        WorkLog log = get(id);
        FieldErrors errors = new FieldErrors();
        if (!Codes.WORK_PENDING.equals(log.approvalStatus)) {
            errors.add("approvalStatus", "只有待审工时可以驳回");
        }
        requireApprover(log, decision, errors, true);
        errors.throwIfAny();
        log.approvalStatus = Codes.WORK_REJECTED;
        log.recordStatus = Codes.WORK_RECORD_EFFECTIVE;
        log.approvedByEmployeeId = decision.getApprovedByEmployeeId();
        log.approvedAt = decision.getApprovedAt().truncatedTo(ChronoUnit.MINUTES);
        log.approvalComment = Text.blankToNull(decision.getApprovalComment());
        logs.save(log);
    }

    private void validateBody(WorkLogForm form, FieldErrors errors) {
        if (form.getWorkDate() == null) {
            errors.add("workDate", "请填写工作日");
        }
        if (form.getHours() == null) {
            errors.add("hours", "请填写工时");
        } else if (form.getHours().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("hours", "工时须大于 0");
        }
        if (form.getWorkDescription() == null) {
            errors.add("workDescription", "请填写工作内容");
        } else if (form.getWorkDescription().length() > 1000) {
            errors.add("workDescription", "工作内容过长");
        }
        Employee employee = form.getEmployeeId() == null ? null : employees.findById(form.getEmployeeId()).orElse(null);
        if (employee == null) {
            errors.add("employeeId", "请选择员工");
        }
        EmployeeEmployment employment = form.getEmploymentId() == null
                ? null
                : employments.findById(form.getEmploymentId()).orElse(null);
        if (employment == null) {
            errors.add("employmentId", "请选择雇佣");
        } else if (employee != null && !employee.id.equals(employment.employeeId)) {
            errors.add("employmentId", "雇佣不属于该员工");
        }
        if (form.getDepartmentAssignmentId() == null) {
            errors.add("departmentAssignmentId", "请选择任职");
        }
        EmployeeDepartmentAssignment assignment = form.getDepartmentAssignmentId() == null
                ? null
                : assignments.findById(form.getDepartmentAssignmentId()).orElse(null);
        if (form.getDepartmentAssignmentId() != null && assignment == null) {
            errors.add("departmentAssignmentId", "任职不存在");
        } else if (assignment != null && employee != null && !employee.id.equals(assignment.employeeId)) {
            errors.add("departmentAssignmentId", "任职不属于该员工");
        } else if (assignment != null && employment != null && !employment.id.equals(assignment.employmentId)) {
            errors.add("departmentAssignmentId", "任职不属于该雇佣");
        }
        Project project = form.getProjectId() == null ? null : projects.findById(form.getProjectId()).orElse(null);
        if (project == null) {
            errors.add("projectId", "请选择项目");
        }
        if (form.getContractStageId() != null) {
            ContractStage stage = stages.findById(form.getContractStageId()).orElse(null);
            if (stage == null) {
                errors.add("contractStageId", "阶段不存在");
            } else if (project != null && !stageOnProject(project.id, stage)) {
                errors.add("contractStageId", "阶段的合同不在该项目中");
            }
        }
        if (form.getSupersedesWorkLogId() != null) {
            if (form.getSupersedesWorkLogId().equals(form.getId()) || cycle(form.getId(), form.getSupersedesWorkLogId())) {
                errors.add("supersedesWorkLogId", "更正关系不能成环");
            } else {
                WorkLog prior = logs.findById(form.getSupersedesWorkLogId()).orElse(null);
                if (prior == null) {
                    errors.add("supersedesWorkLogId", "被更正的工时不存在");
                } else if (employee != null && !employee.id.equals(prior.employeeId)) {
                    errors.add("supersedesWorkLogId", "更正须属于同一员工");
                }
            }
        }
        if (employee != null && project != null && employment != null && assignment != null && form.getWorkDate() != null) {
            explainMembership(form, employee.id, project.id, employment, assignment, errors);
        }
    }

    private void explainMembership(
            WorkLogForm form,
            Long employeeId,
            Long projectId,
            EmployeeEmployment employment,
            EmployeeDepartmentAssignment assignment,
            FieldErrors errors) {
        List<ProjectMember> rows = members.findByProjectIdAndEmployeeIdOrderByIdAsc(projectId, employeeId);
        if (rows.isEmpty()) {
            errors.add("projectId", "该员工不是该项目成员，不能填报工时");
            return;
        }
        boolean employmentMatch = false;
        boolean assignmentMatch = false;
        boolean covered = false;
        for (ProjectMember member : rows) {
            if (!employment.id.equals(member.employmentId)) {
                continue;
            }
            employmentMatch = true;
            if (member.departmentAssignmentId != null && !member.departmentAssignmentId.equals(assignment.id)) {
                continue;
            }
            assignmentMatch = true;
            if (covers(member.validFrom, member.validTo, form.getWorkDate())
                    && covers(employment.employmentStartDate, employment.employmentEndDate, form.getWorkDate())
                    && covers(assignment.validFrom, assignment.validTo, form.getWorkDate())) {
                covered = true;
            }
        }
        if (covered) {
            return;
        }
        if (!employmentMatch) {
            errors.add("employmentId", "雇佣与项目成员不一致");
        } else if (!assignmentMatch) {
            errors.add("departmentAssignmentId", "任职与项目成员不一致");
        } else {
            errors.add("workDate", "工时日期不在项目成员、雇佣或任职的有效期内");
        }
    }

    private boolean stageOnProject(Long projectId, ContractStage stage) {
        Contract contract = contracts.findById(stage.contractId).orElse(null);
        if (contract == null) {
            return false;
        }
        for (ProjectContract link : projectContracts.findByProjectIdOrderByIdAsc(projectId)) {
            if (contract.id.equals(link.contractId)) {
                return true;
            }
        }
        return false;
    }

    private LaborCostRate applicableRate(WorkLog log) {
        LaborCostRate chosen = null;
        for (LaborCostRate rate : rates.findByEmploymentIdAndCostBasisOrderByRateVersionNumberAsc(
                log.employmentId, Codes.LABOR_BASIS_STANDARD)) {
            if (!Codes.RATE_APPROVED.equals(rate.approvalStatus)) {
                continue;
            }
            if (!covers(rate.validFrom, rate.validTo, log.workDate)) {
                continue;
            }
            if (chosen != null) {
                throw new FieldErrorsException(java.util.Map.of("hours", "工作日匹配到多条标准成本率"));
            }
            chosen = rate;
        }
        return chosen;
    }

    private void requireApprover(WorkLog log, WorkLogDecisionForm decision, FieldErrors errors, boolean reasonRequired) {
        if (decision == null || decision.getApprovedByEmployeeId() == null
                || employees.findById(decision.getApprovedByEmployeeId()).isEmpty()) {
            errors.add("approvedByEmployeeId", "请选择审批人");
        } else if (decision.getApprovedByEmployeeId().equals(log.employeeId)) {
            errors.add("approvedByEmployeeId", "不能审批自己的工时");
        } else if (!administrativeManager(log, decision.getApprovedByEmployeeId())) {
            errors.add("approvedByEmployeeId", "审批人不是该员工在工作日的行政上级");
        }
        if (decision == null || decision.getApprovedAt() == null) {
            errors.add("approvedAt", "请填写审批时间");
        }
        if (reasonRequired && (decision == null || Text.blankToNull(decision.getApprovalComment()) == null)) {
            errors.add("approvalComment", "驳回须填写原因");
        } else if (decision != null && decision.getApprovalComment() != null && decision.getApprovalComment().length() > 1000) {
            errors.add("approvalComment", "审批意见过长");
        }
    }

    private boolean administrativeManager(WorkLog log, Long approverEmployeeId) {
        if (log.departmentAssignmentId == null || log.workDate == null) {
            return false;
        }
        for (EmployeeReportingLine line : reportingLines.findByEmployeeIdOrderByIdAsc(log.employeeId)) {
            if (!Codes.REPORTING_ADMIN.equals(line.reportingType)) {
                continue;
            }
            if (!log.departmentAssignmentId.equals(line.assignmentId)) {
                continue;
            }
            if (!covers(line.validFrom, line.validTo, log.workDate)) {
                continue;
            }
            EmployeeDepartmentAssignment managerAssignment = assignments.findById(line.managerAssignmentId).orElse(null);
            if (managerAssignment == null || !approverEmployeeId.equals(managerAssignment.employeeId)) {
                continue;
            }
            if (covers(managerAssignment.validFrom, managerAssignment.validTo, log.workDate)) {
                return true;
            }
        }
        return false;
    }

    private boolean cycle(Long selfId, Long targetId) {
        Set<Long> seen = new HashSet<>();
        Long cursor = targetId;
        while (cursor != null) {
            if (cursor.equals(selfId) || !seen.add(cursor)) {
                return true;
            }
            WorkLog row = logs.findById(cursor).orElse(null);
            cursor = row == null ? null : row.supersedesWorkLogId;
        }
        return false;
    }

    private void apply(WorkLog log, WorkLogForm form) {
        log.employeeId = form.getEmployeeId();
        log.employmentId = form.getEmploymentId();
        log.departmentAssignmentId = form.getDepartmentAssignmentId();
        log.projectId = form.getProjectId();
        log.contractStageId = form.getContractStageId();
        log.workDate = form.getWorkDate();
        log.hours = form.getHours();
        log.workDescription = form.getWorkDescription();
        log.billable = form.isBillable();
        log.supersedesWorkLogId = form.getSupersedesWorkLogId();
    }

    private void normalize(WorkLogForm form) {
        form.setWorkDescription(Text.blankToNull(form.getWorkDescription()));
    }

    private Row row(WorkLog log) {
        CostOccurrence cost = costOccurrences.findEffectiveLabor(log.id);
        return new Row(
                log.id,
                employeeLabel(log.employeeId),
                log.workDate,
                plain(log.hours),
                projectLabel(log.projectId),
                log.workDescription,
                log.approvalStatus,
                log.recordStatus,
                cost == null ? null : plain(cost.hourlyCostAmountSnapshot),
                cost == null ? null : plain(cost.costAmount),
                cost == null ? null : cost.currency,
                cost == null ? null : cost.laborCostRateId,
                log.billable);
    }

    private String employeeLabel(Long employeeId) {
        Employee employee = employees.findById(employeeId).orElse(null);
        if (employee == null) {
            return "员工 #" + employeeId;
        }
        Person person = persons.findById(employee.personId).orElse(null);
        return employee.employeeCode + (person == null ? "" : " " + person.name);
    }

    private String projectLabel(Long projectId) {
        Project project = projects.findById(projectId).orElse(null);
        return project == null ? "项目 #" + projectId : project.projectCode + " " + project.name;
    }

    private static boolean covers(LocalDate from, LocalDate to, LocalDate day) {
        return Periods.contains(from, to, day, day.plusDays(1));
    }

    private static BigDecimal plain(BigDecimal value) {
        return LaborCostRateService.plain(value);
    }
}
