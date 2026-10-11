package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.EmployeeReportingLine;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeReportingLineRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.EmployeeOrgForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.AssignmentForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.EmploymentForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.ReportingForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeOrgService {

    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final EmployeeReportingLineRepository reportingLines;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final DepartmentRepository departments;
    private final SourceDocumentService documents;
    private final ProjectLinkGuard projectLinks;

    public record Header(Long id, String employeeCode, String personName, String status) {
    }

    public record Choice(String key, String label) {
    }

    public Header header(Long employeeId) {
        Employee employee = employees.findById(employeeId).orElseThrow(NotFoundException::new);
        Person person = persons.findById(employee.personId).orElse(null);
        return new Header(employee.id, employee.employeeCode, person == null ? "未找到自然人" : person.name, employee.status);
    }

    public boolean hasOrgRows(Long employeeId) {
        return !employments.findByEmployeeIdOrderByIdAsc(employeeId).isEmpty()
                || assignments.existsByEmployeeId(employeeId)
                || reportingLines.existsByEmployeeId(employeeId);
    }

    public EmployeeOrgForm form(Long employeeId) {
        header(employeeId);
        EmployeeOrgForm form = new EmployeeOrgForm();
        form.setEmployeeId(employeeId);
        for (EmployeeEmployment row : employments.findByEmployeeIdOrderByIdAsc(employeeId)) {
            EmploymentForm item = new EmploymentForm();
            item.setId(row.id);
            item.setRowKey("id:" + row.id);
            item.setEmployerCompanyId(row.employerCompanyId);
            item.setEmployerLegalPersonId(row.employerLegalPersonId);
            item.setEmployeeNumber(row.employeeNumber);
            item.setEmploymentType(row.employmentType);
            item.setEmploymentContractNumber(row.employmentContractNumber);
            item.setEmploymentStartDate(row.employmentStartDate);
            item.setEmploymentEndDate(row.employmentEndDate);
            item.setTerminationReason(row.terminationReason);
            item.setEmploymentContractStartDate(row.employmentContractStartDate);
            item.setEmploymentContractEndDate(row.employmentContractEndDate);
            item.setRecordedAt(row.recordedAt);
            item.setStatus(row.status);
            item.setEmploymentDocumentId(row.employmentDocumentId);
            form.getEmployments().add(item);
        }
        for (EmployeeDepartmentAssignment row : assignments.findByEmployeeIdOrderByIdAsc(employeeId)) {
            AssignmentForm item = new AssignmentForm();
            item.setId(row.id);
            item.setRowKey("id:" + row.id);
            item.setEmploymentKey("id:" + row.employmentId);
            item.setDepartmentId(row.departmentId);
            item.setAssignmentType(row.assignmentType);
            item.setPositionTitle(row.positionTitle);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setStatus(row.status);
            form.getAssignments().add(item);
        }
        for (EmployeeReportingLine row : reportingLines.findByEmployeeIdOrderByIdAsc(employeeId)) {
            ReportingForm item = new ReportingForm();
            item.setId(row.id);
            item.setAssignmentKey("id:" + row.assignmentId);
            item.setManagerAssignmentKey("id:" + row.managerAssignmentId);
            item.setReportingType(row.reportingType);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            form.getReportingLines().add(item);
        }
        prepare(form);
        return form;
    }

    public List<Choice> otherAssignments(Long employeeId) {
        Map<Long, String> names = employeeNames();
        Map<Long, Department> departmentMap = departments.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        List<Choice> choices = new ArrayList<>();
        for (EmployeeDepartmentAssignment row : assignments.findAll()) {
            if (employeeId.equals(row.employeeId)) {
                continue;
            }
            choices.add(new Choice("id:" + row.id, assignmentLabel(names.get(row.employeeId), departmentMap.get(row.departmentId),
                    row.assignmentType, row.positionTitle, row.validFrom, row.validTo)));
        }
        return choices;
    }

    public void prepare(EmployeeOrgForm form) {
        Map<Long, Company> companyMap = companies.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, LegalPerson> legalMap = legalPersons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Department> departmentMap = departments.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        for (EmploymentForm row : form.getEmployments()) {
            if (row.getId() != null && row.getRecordedAt() == null) {
                employments.findById(row.getId()).ifPresent(existing -> row.setRecordedAt(existing.recordedAt));
            }
            row.setDisplayLabel(employmentLabel(row, companyMap, legalMap));
        }
        for (AssignmentForm row : form.getAssignments()) {
            Department department = row.getDepartmentId() == null ? null : departmentMap.get(row.getDepartmentId());
            row.setDisplayLabel(assignmentLabel("本员工", department, row.getAssignmentType(), row.getPositionTitle(),
                    row.getValidFrom(), row.getValidTo()));
        }
    }

    @Transactional
    public void save(Long employeeId, EmployeeOrgForm form) {
        header(employeeId);
        form.setEmployeeId(employeeId);
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validate(form, errors);
        errors.throwIfAny();

        Map<String, Long> employmentIds = syncEmployments(employeeId, form.getEmployments());
        Map<String, Long> assignmentIds = syncAssignments(employeeId, form.getAssignments(), employmentIds);
        syncReporting(employeeId, form.getReportingLines(), assignmentIds);
    }

    private void normalize(EmployeeOrgForm form) {
        List<EmploymentForm> employmentRows = new ArrayList<>();
        int employmentIndex = 0;
        for (EmploymentForm row : Periods.mutable(form.getEmployments())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            row.setEmployeeNumber(Text.blankToNull(row.getEmployeeNumber()));
            row.setEmploymentType(Text.blankToNull(row.getEmploymentType()));
            row.setEmploymentContractNumber(Text.blankToNull(row.getEmploymentContractNumber()));
            row.setTerminationReason(Text.blankToNull(row.getTerminationReason()));
            row.setStatus(Text.blankToNull(row.getStatus()));
            row.setRowKey(Text.blankToNull(row.getRowKey()));
            if (row.getId() == null && row.getEmployerCompanyId() == null && row.getEmployerLegalPersonId() == null
                    && row.getEmployeeNumber() == null && row.getEmploymentStartDate() == null
                    && row.getEmploymentEndDate() == null && row.getEmploymentContractNumber() == null
                    && row.getTerminationReason() == null && row.getEmploymentDocumentId() == null
                    && row.getEmploymentContractStartDate() == null && row.getEmploymentContractEndDate() == null) {
                continue;
            }
            if (row.getRowKey() == null) {
                row.setRowKey(row.getId() == null ? "new-e" + employmentIndex : "id:" + row.getId());
            }
            employmentRows.add(row);
            employmentIndex++;
        }
        form.setEmployments(employmentRows);

        List<AssignmentForm> assignmentRows = new ArrayList<>();
        int assignmentIndex = 0;
        for (AssignmentForm row : Periods.mutable(form.getAssignments())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            row.setEmploymentKey(Text.blankToNull(row.getEmploymentKey()));
            row.setAssignmentType(Text.blankToNull(row.getAssignmentType()));
            row.setPositionTitle(Text.blankToNull(row.getPositionTitle()));
            row.setStatus(Text.blankToNull(row.getStatus()));
            row.setRowKey(Text.blankToNull(row.getRowKey()));
            if (row.getId() == null && row.getEmploymentKey() == null && row.getDepartmentId() == null
                    && row.getPositionTitle() == null && row.getValidFrom() == null && row.getValidTo() == null) {
                continue;
            }
            if (row.getRowKey() == null) {
                row.setRowKey(row.getId() == null ? "new-a" + assignmentIndex : "id:" + row.getId());
            }
            assignmentRows.add(row);
            assignmentIndex++;
        }
        form.setAssignments(assignmentRows);

        List<ReportingForm> reportingRows = new ArrayList<>();
        for (ReportingForm row : Periods.mutable(form.getReportingLines())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            row.setAssignmentKey(Text.blankToNull(row.getAssignmentKey()));
            row.setManagerAssignmentKey(Text.blankToNull(row.getManagerAssignmentKey()));
            row.setReportingType(Text.blankToNull(row.getReportingType()));
            if (row.getId() == null && row.getAssignmentKey() == null && row.getManagerAssignmentKey() == null
                    && row.getValidFrom() == null && row.getValidTo() == null) {
                continue;
            }
            reportingRows.add(row);
        }
        form.setReportingLines(reportingRows);
    }

    private void validate(EmployeeOrgForm form, FieldErrors errors) {
        Set<String> numbers = new HashSet<>();
        Map<String, EmploymentForm> employmentByKey = new HashMap<>();
        for (int i = 0; i < form.getEmployments().size(); i++) {
            EmploymentForm row = form.getEmployments().get(i);
            String prefix = "employments[" + i + "]";
            employmentByKey.put(row.getRowKey(), row);
            if (row.getId() != null) {
                EmployeeEmployment existing = employments.findById(row.getId()).orElse(null);
                if (existing == null || !form.getEmployeeId().equals(existing.employeeId)) {
                    errors.add(prefix, "雇佣记录不属于当前员工");
                }
            }
            int selected = 0;
            if (row.getEmployerCompanyId() != null) {
                selected++;
                if (companies.findById(row.getEmployerCompanyId()).isEmpty()) {
                    errors.add(prefix + ".employerCompanyId", "用人公司不存在");
                }
            }
            if (row.getEmployerLegalPersonId() != null) {
                selected++;
                if (legalPersons.findById(row.getEmployerLegalPersonId()).isEmpty()) {
                    errors.add(prefix + ".employerLegalPersonId", "用人法人不存在");
                }
            }
            if (selected != 1) {
                errors.add(prefix + ".employerCompanyId", "用人对象须在公司与法人中恰好选择一个");
            }
            if (row.getEmployeeNumber() == null) {
                errors.add(prefix + ".employeeNumber", "请填写用工编号");
            } else if (selected == 1) {
                String numberKey = employerKey(row) + "|" + row.getEmployeeNumber();
                if (!numbers.add(numberKey)) {
                    errors.add(prefix + ".employeeNumber", "同一用人对象下的用工编号不能重复");
                }
                for (EmployeeEmployment other : sameNumber(row)) {
                    if (other.id.equals(row.getId()) || form.getEmployeeId().equals(other.employeeId)) {
                        continue;
                    }
                    errors.add(prefix + ".employeeNumber", "该用人对象下用工编号已存在");
                }
            }
            if (row.getEmploymentType() == null || !Codes.EMPLOYMENT_TYPES.contains(row.getEmploymentType())) {
                errors.add(prefix + ".employmentType", "请选择用工类型");
            }
            if (row.getEmploymentStartDate() == null) {
                errors.add(prefix + ".employmentStartDate", "请填写入职日期");
            }
            errors.add(prefix + ".employmentEndDate",
                    Periods.orderError(row.getEmploymentStartDate(), row.getEmploymentEndDate()));
            if (row.getEmploymentContractStartDate() != null && row.getEmploymentContractEndDate() != null
                    && row.getEmploymentContractEndDate().isBefore(row.getEmploymentContractStartDate())) {
                errors.add(prefix + ".employmentContractEndDate", "合同结束日不能早于开始日");
            }
            if (row.getStatus() == null || !Codes.EMPLOYMENT_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择雇佣状态");
            }
            documents.require(row.getEmploymentDocumentId(), errors, prefix + ".employmentDocumentId");
        }

        Map<String, AssignmentForm> assignmentByKey = new HashMap<>();
        Set<Long> keptAssignmentIds = new HashSet<>();
        for (int i = 0; i < form.getAssignments().size(); i++) {
            AssignmentForm row = form.getAssignments().get(i);
            String prefix = "assignments[" + i + "]";
            assignmentByKey.put(row.getRowKey(), row);
            if (row.getId() != null) {
                keptAssignmentIds.add(row.getId());
                assignmentByKey.put("id:" + row.getId(), row);
                EmployeeDepartmentAssignment existing = assignments.findById(row.getId()).orElse(null);
                if (existing == null || !form.getEmployeeId().equals(existing.employeeId)) {
                    errors.add(prefix, "任职记录不属于当前员工");
                }
            }
            EmploymentForm employment = employmentByKey.get(row.getEmploymentKey());
            if (employment == null) {
                errors.add(prefix + ".employmentKey", "任职必须选择本员工的一条雇佣记录");
            }
            if (row.getDepartmentId() == null || departments.findById(row.getDepartmentId()).isEmpty()) {
                errors.add(prefix + ".departmentId", "请选择部门");
            }
            if (row.getAssignmentType() == null || !Codes.ASSIGNMENT_TYPES.contains(row.getAssignmentType())) {
                errors.add(prefix + ".assignmentType", "请选择任职类型");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写任职开始日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            if (employment != null && row.getValidFrom() != null
                    && !Periods.contains(employment.getEmploymentStartDate(), employment.getEmploymentEndDate(),
                    row.getValidFrom(), row.getValidTo())) {
                errors.add(prefix + ".validFrom", "任职期间必须落在雇佣有效期内。终止日当天起已不在该雇佣中，期间按左闭右开");
            }
            if (row.getStatus() == null || !Codes.ASSIGNMENT_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择任职状态");
            }
        }

        List<Edge> edges = new ArrayList<>();
        for (EmployeeReportingLine existing : reportingLines.findAll()) {
            if (form.getEmployeeId().equals(existing.employeeId)) {
                continue;
            }
            edges.add(new Edge(existing.id, "id:" + existing.assignmentId, "id:" + existing.managerAssignmentId,
                    existing.reportingType, existing.validFrom, existing.validTo, false));
        }
        for (int i = 0; i < form.getReportingLines().size(); i++) {
            ReportingForm row = form.getReportingLines().get(i);
            String prefix = "reportingLines[" + i + "]";
            if (row.getId() != null) {
                EmployeeReportingLine existing = reportingLines.findById(row.getId()).orElse(null);
                if (existing == null || !form.getEmployeeId().equals(existing.employeeId)) {
                    errors.add(prefix, "汇报记录不属于当前员工");
                }
            }
            if (row.getAssignmentKey() == null || !assignmentByKey.containsKey(row.getAssignmentKey())) {
                errors.add(prefix + ".assignmentKey", "汇报的任职必须属于本员工");
            }
            if (row.getManagerAssignmentKey() == null) {
                errors.add(prefix + ".managerAssignmentKey", "请选择上级任职");
            } else if (row.getAssignmentKey() != null && row.getAssignmentKey().equals(row.getManagerAssignmentKey())) {
                errors.add(prefix + ".managerAssignmentKey", "不能向自身任职汇报");
            }
            if (row.getReportingType() == null || !Codes.REPORTING_TYPES.contains(row.getReportingType())) {
                errors.add(prefix + ".reportingType", "请选择汇报类型");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写汇报开始日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            edges.add(new Edge(row.getId(), row.getAssignmentKey(), row.getManagerAssignmentKey(),
                    row.getReportingType(), row.getValidFrom(), row.getValidTo(), true));
        }

        Map<String, Span> spans = assignmentSpans(form, assignmentByKey, keptAssignmentIds);
        for (Edge edge : edges) {
            if (edge.assignmentKey == null || edge.managerKey == null || edge.from == null) {
                continue;
            }
            Span self = spans.get(edge.assignmentKey);
            Span manager = spans.get(edge.managerKey);
            String field = edge.fromThisForm ? "reportingLines" : "*";
            if (self == null || manager == null) {
                errors.add(field, "汇报线引用的任职不存在或已删除");
                continue;
            }
            if (edge.assignmentKey.equals(edge.managerKey)) {
                errors.add(field, "不能向自身任职汇报");
                continue;
            }
            if (!Periods.contains(self.from, self.to, edge.from, edge.to)
                    || !Periods.contains(manager.from, manager.to, edge.from, edge.to)) {
                errors.add(field, "汇报期间必须同时落在本人任职和上级任职的有效期内");
            }
        }
        if (administrativeCycle(edges)) {
            errors.add("reportingLines", "行政汇报链不能形成循环");
        }
    }

    private Map<String, Span> assignmentSpans(
            EmployeeOrgForm form,
            Map<String, AssignmentForm> assignmentByKey,
            Set<Long> keptAssignmentIds) {
        Map<String, Span> spans = new HashMap<>();
        for (EmployeeDepartmentAssignment row : assignments.findAll()) {
            if (form.getEmployeeId().equals(row.employeeId) && !keptAssignmentIds.contains(row.id)) {
                continue;
            }
            AssignmentForm overlay = assignmentByKey.get("id:" + row.id);
            if (overlay != null) {
                continue;
            }
            spans.put("id:" + row.id, new Span(row.validFrom, row.validTo));
        }
        for (AssignmentForm row : form.getAssignments()) {
            Span span = new Span(row.getValidFrom(), row.getValidTo());
            spans.put(row.getRowKey(), span);
            if (row.getId() != null) {
                spans.put("id:" + row.getId(), span);
            }
        }
        return spans;
    }

    private boolean administrativeCycle(List<Edge> edges) {
        List<Edge> admin = edges.stream()
                .filter(edge -> Codes.REPORTING_ADMIN.equals(edge.type))
                .filter(edge -> edge.assignmentKey != null && edge.managerKey != null && edge.from != null)
                .filter(edge -> !edge.assignmentKey.equals(edge.managerKey))
                .toList();
        for (Edge start : admin) {
            if (reaches(start.managerKey, start.assignmentKey, start.from, start.to, admin, new HashSet<>())) {
                return true;
            }
        }
        return false;
    }

    private boolean reaches(String current, String target, LocalDate from, LocalDate to, List<Edge> admin, Set<String> seen) {
        if (from == null || (to != null && !from.isBefore(to))) {
            return false;
        }
        if (!seen.add(current + "|" + from + "|" + to)) {
            return false;
        }
        for (Edge edge : admin) {
            if (!current.equals(edge.assignmentKey) || !Periods.overlaps(from, to, edge.from, edge.to)) {
                continue;
            }
            LocalDate[] window = Periods.intersection(from, to, edge.from, edge.to);
            if (edge.managerKey.equals(target)) {
                return true;
            }
            if (reaches(edge.managerKey, target, window[0], window[1], admin, seen)) {
                return true;
            }
        }
        return false;
    }

    private List<EmployeeEmployment> sameNumber(EmploymentForm row) {
        if (row.getEmployerCompanyId() != null) {
            return employments.findByEmployerCompanyIdAndEmployeeNumber(row.getEmployerCompanyId(), row.getEmployeeNumber());
        }
        if (row.getEmployerLegalPersonId() != null) {
            return employments.findByEmployerLegalPersonIdAndEmployeeNumber(
                    row.getEmployerLegalPersonId(), row.getEmployeeNumber());
        }
        return List.of();
    }

    private String employerKey(EmploymentForm row) {
        if (row.getEmployerCompanyId() != null) {
            return "c:" + row.getEmployerCompanyId();
        }
        return "l:" + row.getEmployerLegalPersonId();
    }

    private Map<String, Long> syncEmployments(Long employeeId, List<EmploymentForm> rows) {
        Map<String, Long> keys = new HashMap<>();
        Set<Long> keep = new HashSet<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (EmploymentForm row : rows) {
            EmployeeEmployment entity;
            if (row.getId() != null) {
                entity = employments.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "雇佣记录不存在")));
            } else {
                entity = new EmployeeEmployment();
                entity.employeeId = employeeId;
                entity.recordedAt = now;
            }
            entity.employerCompanyId = row.getEmployerCompanyId();
            entity.employerLegalPersonId = row.getEmployerLegalPersonId();
            entity.employeeNumber = row.getEmployeeNumber();
            entity.employmentType = row.getEmploymentType();
            entity.employmentContractNumber = row.getEmploymentContractNumber();
            entity.employmentStartDate = row.getEmploymentStartDate();
            entity.employmentEndDate = row.getEmploymentEndDate();
            entity.terminationReason = row.getTerminationReason();
            entity.employmentContractStartDate = row.getEmploymentContractStartDate();
            entity.employmentContractEndDate = row.getEmploymentContractEndDate();
            entity.status = row.getStatus();
            entity.employmentDocumentId = row.getEmploymentDocumentId();
            Long id = employments.save(entity).id;
            keep.add(id);
            keys.put(row.getRowKey(), id);
            keys.put("id:" + id, id);
        }
        for (EmployeeEmployment existing : employments.findByEmployeeIdOrderByIdAsc(employeeId)) {
            if (!keep.contains(existing.id)) {
                if (projectLinks.employmentUsed(existing.id)) {
                    throw new FieldErrorsException(Map.of("*", "该雇佣仍被项目成员引用，不能删除"));
                }
                employments.delete(existing);
            }
        }
        return keys;
    }

    private Map<String, Long> syncAssignments(Long employeeId, List<AssignmentForm> rows, Map<String, Long> employmentIds) {
        Map<String, Long> keys = new HashMap<>();
        for (EmployeeDepartmentAssignment existing : assignments.findAll()) {
            keys.put("id:" + existing.id, existing.id);
        }
        Set<Long> keep = new HashSet<>();
        for (AssignmentForm row : rows) {
            Long employmentId = employmentIds.get(row.getEmploymentKey());
            if (employmentId == null) {
                throw new FieldErrorsException(Map.of("*", "任职没有对应的雇佣记录"));
            }
            EmployeeDepartmentAssignment entity;
            if (row.getId() != null) {
                entity = assignments.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "任职记录不存在")));
            } else {
                entity = new EmployeeDepartmentAssignment();
                entity.employeeId = employeeId;
            }
            entity.employmentId = employmentId;
            entity.departmentId = row.getDepartmentId();
            entity.assignmentType = row.getAssignmentType();
            entity.positionTitle = row.getPositionTitle();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.status = row.getStatus();
            Long id = assignments.save(entity).id;
            keep.add(id);
            keys.put(row.getRowKey(), id);
            keys.put("id:" + id, id);
        }
        for (EmployeeDepartmentAssignment existing : assignments.findByEmployeeIdOrderByIdAsc(employeeId)) {
            if (!keep.contains(existing.id)) {
                if (projectLinks.assignmentUsed(existing.id)) {
                    throw new FieldErrorsException(Map.of("*", "该任职仍被项目成员引用，不能删除"));
                }
                assignments.delete(existing);
            }
        }
        return keys;
    }

    private void syncReporting(Long employeeId, List<ReportingForm> rows, Map<String, Long> assignmentIds) {
        Set<Long> keep = new HashSet<>();
        for (ReportingForm row : rows) {
            Long assignmentId = assignmentIds.get(row.getAssignmentKey());
            Long managerId = assignmentIds.get(row.getManagerAssignmentKey());
            if (assignmentId == null || managerId == null) {
                throw new FieldErrorsException(Map.of("*", "汇报线没有对应的任职记录"));
            }
            EmployeeReportingLine entity;
            if (row.getId() != null) {
                entity = reportingLines.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "汇报记录不存在")));
            } else {
                entity = new EmployeeReportingLine();
                entity.employeeId = employeeId;
            }
            entity.assignmentId = assignmentId;
            entity.managerAssignmentId = managerId;
            entity.reportingType = row.getReportingType();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            keep.add(reportingLines.save(entity).id);
        }
        for (EmployeeReportingLine existing : reportingLines.findByEmployeeIdOrderByIdAsc(employeeId)) {
            if (!keep.contains(existing.id)) {
                reportingLines.delete(existing);
            }
        }
    }

    private Map<Long, String> employeeNames() {
        Map<Long, Person> people = persons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, String> names = new HashMap<>();
        for (Employee employee : employees.findAll()) {
            Person person = people.get(employee.personId);
            names.put(employee.id, employee.employeeCode + " " + (person == null ? "未找到自然人" : person.name));
        }
        return names;
    }

    private String employmentLabel(EmploymentForm row, Map<Long, Company> companies, Map<Long, LegalPerson> legalPersons) {
        String employer;
        if (row.getEmployerCompanyId() != null) {
            Company company = companies.get(row.getEmployerCompanyId());
            employer = company == null ? "未找到公司" : company.companyCode + " " + company.name;
        } else if (row.getEmployerLegalPersonId() != null) {
            LegalPerson legalPerson = legalPersons.get(row.getEmployerLegalPersonId());
            employer = legalPerson == null ? "未找到法人" : legalPerson.legalName;
        } else {
            employer = "未选用人对象";
        }
        String number = row.getEmployeeNumber() == null ? "无编号" : row.getEmployeeNumber();
        String start = row.getEmploymentStartDate() == null ? "未填入职日" : row.getEmploymentStartDate().toString();
        return number + " · " + employer + " · " + start;
    }

    private String assignmentLabel(
            String who,
            Department department,
            String type,
            String title,
            LocalDate from,
            LocalDate to) {
        String departmentLabel = department == null ? "未选部门" : department.departmentCode + " " + department.name;
        String post = title == null || title.isBlank() ? "" : " " + title;
        String end = to == null ? "未结束" : to.toString();
        String start = from == null ? "未填" : from.toString();
        return who + " · " + departmentLabel + " · " + (type == null ? "" : type) + post + " · " + start + " 至 " + end;
    }

    private record Edge(
            Long id,
            String assignmentKey,
            String managerKey,
            String type,
            LocalDate from,
            LocalDate to,
            boolean fromThisForm) {
    }

    private record Span(LocalDate from, LocalDate to) {
    }
}
