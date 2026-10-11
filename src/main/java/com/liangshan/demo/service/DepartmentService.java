package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.DepartmentServiceCompany;
import com.liangshan.demo.domain.DepartmentVersion;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.DepartmentServiceCompanyRepository;
import com.liangshan.demo.repository.DepartmentVersionRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.DepartmentForm;
import com.liangshan.demo.web.form.DepartmentForm.ServiceCompanyForm;
import com.liangshan.demo.web.form.DepartmentForm.VersionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class DepartmentService {

    private final DepartmentRepository departments;
    private final DepartmentVersionRepository versions;
    private final DepartmentServiceCompanyRepository serviceCompanies;
    private final CompanyRepository companies;
    private final EmployeeRepository employees;
    private final PersonRepository persons;

    public record DepartmentRow(
            Long id,
            String departmentCode,
            String name,
            String departmentType,
            String companyName,
            String parentName,
            String headLabel,
            String status) {
    }

    public List<DepartmentRow> list() {
        Map<Long, Company> companyMap = companies.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Department> departmentMap = departments.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, String> heads = headLabels();
        return departments.findAllByOrderByDepartmentCodeAsc().stream().map(department -> new DepartmentRow(
                department.id,
                department.departmentCode,
                department.name,
                department.departmentType,
                labelCompany(companyMap.get(department.companyId)),
                department.parentDepartmentId == null ? "—"
                        : departmentMap.containsKey(department.parentDepartmentId)
                        ? departmentMap.get(department.parentDepartmentId).name : "未找到上级",
                department.departmentHeadEmployeeId == null ? "—"
                        : heads.getOrDefault(department.departmentHeadEmployeeId, "未找到员工"),
                department.status)).toList();
    }

    public Map<Long, String> headLabels() {
        Map<Long, Person> people = persons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, String> labels = new java.util.LinkedHashMap<>();
        for (Employee employee : employees.findAllByOrderByEmployeeCodeAsc()) {
            Person person = people.get(employee.personId);
            String name = person == null ? "未关联自然人" : person.name;
            labels.put(employee.id, employee.employeeCode + " " + name);
        }
        return labels;
    }

    public Department get(Long id) {
        return departments.findById(id).orElseThrow(NotFoundException::new);
    }

    public DepartmentForm form(Long id) {
        Department department = get(id);
        DepartmentForm form = new DepartmentForm();
        form.setId(department.id);
        form.setCompanyId(department.companyId);
        form.setParentDepartmentId(department.parentDepartmentId);
        form.setDepartmentCode(department.departmentCode);
        form.setName(department.name);
        form.setDepartmentType(department.departmentType);
        form.setDepartmentHeadEmployeeId(department.departmentHeadEmployeeId);
        form.setDepartmentContactPhone(department.departmentContactPhone);
        form.setDepartmentContactEmail(department.departmentContactEmail);
        form.setValidFrom(department.validFrom);
        form.setValidTo(department.validTo);
        form.setStatus(department.status);
        form.setVersions(versions.findByDepartmentIdOrderByVersionNumberAsc(id).stream().map(row -> {
            VersionForm item = new VersionForm();
            item.setId(row.id);
            item.setVersionNumber(row.versionNumber);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setBasisDocumentId(row.basisDocumentId);
            return item;
        }).toList());
        form.setServiceCompanies(serviceCompanies.findByDepartmentIdOrderByIdAsc(id).stream().map(row -> {
            ServiceCompanyForm item = new ServiceCompanyForm();
            item.setId(row.id);
            item.setServiceCompanyId(row.serviceCompanyId);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setBasisDocumentId(row.basisDocumentId);
            return item;
        }).toList());
        return form;
    }

    @Transactional
    public long save(DepartmentForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validate(form, errors);
        errors.throwIfAny();

        Department department = form.getId() == null ? new Department() : get(form.getId());
        department.companyId = form.getCompanyId();
        department.parentDepartmentId = form.getParentDepartmentId();
        department.departmentCode = form.getDepartmentCode();
        department.name = form.getName();
        department.departmentType = form.getDepartmentType();
        department.departmentHeadEmployeeId = form.getDepartmentHeadEmployeeId();
        department.departmentContactPhone = form.getDepartmentContactPhone();
        department.departmentContactEmail = form.getDepartmentContactEmail();
        department.validFrom = form.getValidFrom();
        department.validTo = form.getValidTo();
        department.status = form.getStatus();
        department = departments.saveAndFlush(department);
        syncServiceCompanies(department.id, form.getServiceCompanies());
        syncVersions(department, form.getVersions());
        return department.id;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (!departments.findByParentDepartmentId(id).isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "仍有下级部门，不能删除"));
        }
        versions.findByDepartmentIdOrderByVersionNumberAsc(id).forEach(versions::delete);
        serviceCompanies.findByDepartmentIdOrderByIdAsc(id).forEach(serviceCompanies::delete);
        departments.deleteById(id);
    }

    private void normalize(DepartmentForm form) {
        form.setDepartmentCode(Text.blankToNull(form.getDepartmentCode()));
        form.setName(Text.blankToNull(form.getName()));
        form.setDepartmentType(Text.blankToNull(form.getDepartmentType()));
        form.setDepartmentContactPhone(Text.blankToNull(form.getDepartmentContactPhone()));
        form.setDepartmentContactEmail(Text.blankToNull(form.getDepartmentContactEmail()));
        form.setStatus(Text.blankToNull(form.getStatus()));

        List<VersionForm> versionRows = new ArrayList<>();
        for (VersionForm row : Periods.mutable(form.getVersions())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            if (row.getId() == null && row.getVersionNumber() == null && row.getValidFrom() == null
                    && row.getValidTo() == null && row.getBasisDocumentId() == null) {
                continue;
            }
            versionRows.add(row);
        }
        form.setVersions(versionRows);

        List<ServiceCompanyForm> services = new ArrayList<>();
        for (ServiceCompanyForm row : Periods.mutable(form.getServiceCompanies())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            if (row.getId() == null && row.getServiceCompanyId() == null && row.getValidFrom() == null
                    && row.getValidTo() == null && row.getBasisDocumentId() == null) {
                continue;
            }
            services.add(row);
        }
        form.setServiceCompanies(services);
    }

    private void validate(DepartmentForm form, FieldErrors errors) {
        if (form.getCompanyId() == null || companies.findById(form.getCompanyId()).isEmpty()) {
            errors.add("companyId", "请选择部门所属公司或分公司");
        }
        if (form.getDepartmentCode() == null) {
            errors.add("departmentCode", "请填写部门编号");
        } else if (form.getCompanyId() != null && departments.findByCompanyIdAndDepartmentCode(form.getCompanyId(), form.getDepartmentCode())
                .stream().anyMatch(department -> form.getId() == null || !department.id.equals(form.getId()))) {
            errors.add("departmentCode", "同一公司内部门编号不能重复");
        }
        if (form.getName() == null) {
            errors.add("name", "请填写部门名称");
        }
        if (form.getDepartmentType() == null || !Codes.DEPARTMENT_TYPES.contains(form.getDepartmentType())) {
            errors.add("departmentType", "请选择部门类型");
        }
        if (form.getStatus() == null || !Codes.DEPARTMENT_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择部门状态");
        }
        if (form.getValidFrom() == null) {
            errors.add("validFrom", "请填写部门生效日");
        }
        errors.add("validTo", Periods.orderError(form.getValidFrom(), form.getValidTo()));
        if (!Text.emailOk(form.getDepartmentContactEmail())) {
            errors.add("departmentContactEmail", "部门邮箱格式不正确");
        }
        if (form.getDepartmentHeadEmployeeId() != null
                && employees.findById(form.getDepartmentHeadEmployeeId()).isEmpty()) {
            errors.add("departmentHeadEmployeeId", "部门负责人不存在");
        }
        if (form.getParentDepartmentId() != null) {
            if (form.getParentDepartmentId().equals(form.getId())) {
                errors.add("parentDepartmentId", "上级部门不能是自己");
            } else {
                Department parent = departments.findById(form.getParentDepartmentId()).orElse(null);
                if (parent == null) {
                    errors.add("parentDepartmentId", "上级部门不存在");
                } else if (form.getCompanyId() != null && !form.getCompanyId().equals(parent.companyId)) {
                    errors.add("parentDepartmentId", "上级部门必须属于同一公司或分公司");
                } else if (createsCycle(form.getId(), form.getParentDepartmentId())) {
                    errors.add("parentDepartmentId", "部门层级不能形成循环");
                }
            }
        }
        if (form.getId() != null && form.getCompanyId() != null) {
            for (Department child : departments.findByParentDepartmentId(form.getId())) {
                if (!form.getCompanyId().equals(child.companyId)) {
                    errors.add("companyId", "调整所属公司前，请先移走下级部门");
                    break;
                }
            }
        }

        Set<Long> seenServices = new HashSet<>();
        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        List<Long> serviceIds = new ArrayList<>();
        for (int i = 0; i < form.getServiceCompanies().size(); i++) {
            ServiceCompanyForm row = form.getServiceCompanies().get(i);
            String prefix = "serviceCompanies[" + i + "]";
            if (row.getServiceCompanyId() == null || companies.findById(row.getServiceCompanyId()).isEmpty()) {
                errors.add(prefix + ".serviceCompanyId", "请选择服务公司");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写服务开始日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            froms.add(row.getValidFrom());
            tos.add(row.getValidTo());
            serviceIds.add(row.getServiceCompanyId());
            seenServices.add(row.getServiceCompanyId());
        }
        for (Long serviceId : seenServices) {
            List<LocalDate> groupFrom = new ArrayList<>();
            List<LocalDate> groupTo = new ArrayList<>();
            for (int i = 0; i < serviceIds.size(); i++) {
                if (serviceId != null && serviceId.equals(serviceIds.get(i))) {
                    groupFrom.add(froms.get(i));
                    groupTo.add(tos.get(i));
                }
            }
            errors.add("serviceCompanies", Periods.overlapError(groupFrom, groupTo, "同一服务公司"));
        }

        int openVersions = 0;
        for (int i = 0; i < form.getVersions().size(); i++) {
            VersionForm row = form.getVersions().get(i);
            String prefix = "versions[" + i + "]";
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写版本生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            if (row.getValidTo() == null) {
                openVersions++;
            }
        }
        if (openVersions > 1) {
            errors.add("versions", "同时只能有一条未结束的部门版本");
        }
    }

    private boolean createsCycle(Long selfId, Long parentId) {
        Set<Long> seen = new HashSet<>();
        Long cursor = parentId;
        while (cursor != null) {
            if (!seen.add(cursor)) {
                return true;
            }
            if (cursor.equals(selfId)) {
                return true;
            }
            Department next = departments.findById(cursor).orElse(null);
            cursor = next == null ? null : next.parentDepartmentId;
        }
        return false;
    }

    private void syncServiceCompanies(Long departmentId, List<ServiceCompanyForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (ServiceCompanyForm row : rows) {
            DepartmentServiceCompany entity;
            if (row.getId() != null) {
                entity = serviceCompanies.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "服务公司记录不存在")));
                if (!departmentId.equals(entity.departmentId)) {
                    throw new FieldErrorsException(Map.of("*", "服务公司记录不属于当前部门"));
                }
            } else {
                entity = new DepartmentServiceCompany();
                entity.departmentId = departmentId;
            }
            entity.serviceCompanyId = row.getServiceCompanyId();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.basisDocumentId = row.getBasisDocumentId();
            keep.add(serviceCompanies.save(entity).id);
        }
        for (DepartmentServiceCompany existing : serviceCompanies.findByDepartmentIdOrderByIdAsc(departmentId)) {
            if (!keep.contains(existing.id)) {
                serviceCompanies.delete(existing);
            }
        }
    }

    private void syncVersions(Department department, List<VersionForm> submitted) {
        List<DepartmentVersion> existing = versions.findByDepartmentIdOrderByVersionNumberAsc(department.id);
        List<VersionForm> working = new ArrayList<>(submitted);
        DepartmentVersion dbOpen = existing.stream().filter(version -> version.validTo == null).findFirst().orElse(null);
        LocalDate today = LocalDate.now();
        boolean split = dbOpen != null && !sameCore(dbOpen, department)
                && dbOpen.validFrom != null && dbOpen.validFrom.isBefore(today);
        if (split) {
            VersionForm matched = working.stream().filter(row -> dbOpen.id.equals(row.getId())).findFirst().orElse(null);
            Long newBasis = matched == null ? dbOpen.basisDocumentId : matched.getBasisDocumentId();
            if (matched != null) {
                matched.setValidTo(today);
                matched.setValidFrom(dbOpen.validFrom);
                matched.setBasisDocumentId(dbOpen.basisDocumentId);
            } else {
                VersionForm closed = new VersionForm();
                closed.setId(dbOpen.id);
                closed.setVersionNumber(dbOpen.versionNumber);
                closed.setValidFrom(dbOpen.validFrom);
                closed.setValidTo(today);
                closed.setBasisDocumentId(dbOpen.basisDocumentId);
                working.add(closed);
            }
            working.removeIf(row -> row.getId() == null && row.getValidTo() == null);
            VersionForm fresh = new VersionForm();
            fresh.setVersionNumber(nextNumber(existing, working));
            fresh.setValidFrom(today);
            fresh.setBasisDocumentId(newBasis);
            working.add(fresh);
        }
        if (working.stream().noneMatch(row -> row.getValidTo() == null)) {
            if (dbOpen != null && !split) {
                working.removeIf(row -> dbOpen.id.equals(row.getId()));
                VersionForm keep = new VersionForm();
                keep.setId(dbOpen.id);
                keep.setVersionNumber(dbOpen.versionNumber);
                keep.setValidFrom(dbOpen.validFrom);
                keep.setBasisDocumentId(dbOpen.basisDocumentId);
                working.add(keep);
            } else {
                VersionForm fresh = new VersionForm();
                fresh.setVersionNumber(nextNumber(existing, working));
                fresh.setValidFrom(department.validFrom != null ? department.validFrom : today);
                working.add(fresh);
            }
        }
        assertVersionPeriods(working);
        Set<Long> keep = new HashSet<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (VersionForm row : working) {
            DepartmentVersion entity;
            if (row.getId() != null) {
                entity = versions.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "部门版本不存在")));
                if (!department.id.equals(entity.departmentId)) {
                    throw new FieldErrorsException(Map.of("*", "部门版本不属于当前部门"));
                }
                entity.validFrom = row.getValidFrom();
                entity.validTo = row.getValidTo();
                entity.basisDocumentId = row.getBasisDocumentId();
                if (row.getValidTo() == null) {
                    copyCore(department, entity);
                }
            } else {
                entity = new DepartmentVersion();
                entity.departmentId = department.id;
                entity.versionNumber = row.getVersionNumber() == null ? nextNumber(existing, working) : row.getVersionNumber();
                entity.validFrom = row.getValidFrom();
                entity.validTo = row.getValidTo();
                entity.basisDocumentId = row.getBasisDocumentId();
                entity.recordedAt = now;
                copyCore(department, entity);
            }
            keep.add(versions.save(entity).id);
        }
        for (DepartmentVersion version : versions.findByDepartmentIdOrderByVersionNumberAsc(department.id)) {
            if (!keep.contains(version.id)) {
                versions.delete(version);
            }
        }
    }

    private void assertVersionPeriods(List<VersionForm> rows) {
        if (rows.stream().filter(row -> row.getValidTo() == null).count() != 1) {
            throw new FieldErrorsException(Map.of("versions", "须保留一条当前部门版本"));
        }
        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        for (VersionForm row : rows) {
            if (row.getValidFrom() == null) {
                throw new FieldErrorsException(Map.of("versions", "部门版本缺少生效日"));
            }
            String order = Periods.orderError(row.getValidFrom(), row.getValidTo());
            if (order != null) {
                throw new FieldErrorsException(Map.of("versions", order));
            }
            froms.add(row.getValidFrom());
            tos.add(row.getValidTo());
        }
        String overlap = Periods.overlapError(froms, tos, "部门版本");
        if (overlap != null) {
            throw new FieldErrorsException(Map.of("versions", overlap));
        }
    }

    private int nextNumber(List<DepartmentVersion> existing, List<VersionForm> forms) {
        int max = 0;
        for (DepartmentVersion version : existing) {
            max = Math.max(max, version.versionNumber);
        }
        for (VersionForm form : forms) {
            if (form.getVersionNumber() != null) {
                max = Math.max(max, form.getVersionNumber());
            }
        }
        return max + 1;
    }

    private boolean sameCore(DepartmentVersion version, Department department) {
        return Cmp.eq(version.companyId, department.companyId)
                && Cmp.eq(version.parentDepartmentId, department.parentDepartmentId)
                && Cmp.eq(version.departmentCode, department.departmentCode)
                && Cmp.eq(version.name, department.name)
                && Cmp.eq(version.departmentType, department.departmentType)
                && Cmp.eq(version.departmentHeadEmployeeId, department.departmentHeadEmployeeId)
                && Cmp.eq(version.departmentContactPhone, department.departmentContactPhone)
                && Cmp.eq(version.departmentContactEmail, department.departmentContactEmail)
                && Cmp.eq(version.status, department.status);
    }

    private void copyCore(Department department, DepartmentVersion version) {
        version.companyId = department.companyId;
        version.parentDepartmentId = department.parentDepartmentId;
        version.departmentCode = department.departmentCode;
        version.name = department.name;
        version.departmentType = department.departmentType;
        version.departmentHeadEmployeeId = department.departmentHeadEmployeeId;
        version.departmentContactPhone = department.departmentContactPhone;
        version.departmentContactEmail = department.departmentContactEmail;
        version.status = department.status;
    }

    private String labelCompany(Company company) {
        if (company == null) {
            return "未找到公司";
        }
        return company.companyCode + " " + company.name;
    }
}
