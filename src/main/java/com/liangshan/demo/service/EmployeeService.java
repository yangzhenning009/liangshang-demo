package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.DepartmentVersionRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.EmployeeForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final DepartmentRepository departments;
    private final DepartmentVersionRepository departmentVersions;

    public record EmployeeRow(Long id, String employeeCode, String personName, String workEmail, String workPhone, String status) {
    }

    public List<EmployeeRow> list() {
        Map<Long, Person> people = persons.findAll().stream().collect(java.util.stream.Collectors.toMap(p -> p.id, p -> p));
        return employees.findAllByOrderByEmployeeCodeAsc().stream()
                .map(employee -> new EmployeeRow(
                        employee.id,
                        employee.employeeCode,
                        people.containsKey(employee.personId) ? people.get(employee.personId).name : "未找到自然人",
                        employee.workEmail,
                        employee.workPhone,
                        employee.status))
                .toList();
    }

    public Employee get(Long id) {
        return employees.findById(id).orElseThrow(NotFoundException::new);
    }

    public EmployeeForm form(Long id) {
        Employee employee = get(id);
        EmployeeForm form = new EmployeeForm();
        form.setId(employee.id);
        form.setPersonId(employee.personId);
        form.setEmployeeCode(employee.employeeCode);
        form.setWorkEmail(employee.workEmail);
        form.setWorkPhone(employee.workPhone);
        form.setStatus(employee.status);
        return form;
    }

    public List<Person> selectablePersons(Long employeeId) {
        return persons.findAllByOrderByNameAsc().stream()
                .filter(person -> {
                    List<Employee> linked = employees.findByPersonId(person.id);
                    return linked.isEmpty() || linked.stream().anyMatch(employee -> employee.id.equals(employeeId));
                })
                .toList();
    }

    @Transactional
    public long save(EmployeeForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validate(form, errors);
        errors.throwIfAny();

        Employee employee = form.getId() == null ? new Employee() : get(form.getId());
        employee.personId = form.getPersonId();
        employee.employeeCode = form.getEmployeeCode();
        employee.workEmail = form.getWorkEmail();
        employee.workPhone = form.getWorkPhone();
        employee.status = form.getStatus();
        return employees.save(employee).id;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (!departments.findByDepartmentHeadEmployeeId(id).isEmpty()
                || !departmentVersions.findByDepartmentHeadEmployeeId(id).isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "该员工仍是部门负责人，不能删除"));
        }
        employees.deleteById(id);
    }

    private void normalize(EmployeeForm form) {
        form.setEmployeeCode(Text.blankToNull(form.getEmployeeCode()));
        form.setWorkEmail(Text.blankToNull(form.getWorkEmail()));
        form.setWorkPhone(Text.blankToNull(form.getWorkPhone()));
        form.setStatus(Text.blankToNull(form.getStatus()));
    }

    private void validate(EmployeeForm form, FieldErrors errors) {
        if (form.getPersonId() == null || persons.findById(form.getPersonId()).isEmpty()) {
            errors.add("personId", "请选择自然人");
        } else if (employees.findByPersonId(form.getPersonId()).stream()
                .anyMatch(employee -> form.getId() == null || !employee.id.equals(form.getId()))) {
            errors.add("personId", "该自然人已有员工档案，员工与自然人须一对一");
        }
        if (form.getEmployeeCode() == null) {
            errors.add("employeeCode", "请填写员工编号");
        } else if (employees.findByEmployeeCode(form.getEmployeeCode()).stream()
                .anyMatch(employee -> form.getId() == null || !employee.id.equals(form.getId()))) {
            errors.add("employeeCode", "员工编号已存在");
        }
        if (form.getStatus() == null || !Codes.EMPLOYEE_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择员工档案状态");
        }
        if (!Text.emailOk(form.getWorkEmail())) {
            errors.add("workEmail", "工作邮箱格式不正确");
        }
    }
}
