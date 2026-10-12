package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.WorkLogService;
import com.liangshan.demo.web.form.WorkLogDecisionForm;
import com.liangshan.demo.web.form.WorkLogForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/work-logs")
public class WorkLogController {

    private final WorkLogService logs;
    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final DepartmentRepository departments;
    private final ProjectRepository projects;
    private final ContractStageRepository stages;
    private final ContractRepository contracts;

    public WorkLogController(
            WorkLogService logs,
            EmployeeRepository employees,
            PersonRepository persons,
            EmployeeEmploymentRepository employments,
            EmployeeDepartmentAssignmentRepository assignments,
            DepartmentRepository departments,
            ProjectRepository projects,
            ContractStageRepository stages,
            ContractRepository contracts) {
        this.logs = logs;
        this.employees = employees;
        this.persons = persons;
        this.employments = employments;
        this.assignments = assignments;
        this.departments = departments;
        this.projects = projects;
        this.stages = stages;
        this.contracts = contracts;
    }

    public record Choice(Long id, String label) {
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "work-logs";
    }

    @ModelAttribute("employeeChoices")
    public List<Choice> employeeChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Employee employee : employees.findAllByOrderByEmployeeCodeAsc()) {
            Person person = persons.findById(employee.personId).orElse(null);
            rows.add(new Choice(employee.id, employee.employeeCode + (person == null ? "" : " " + person.name)));
        }
        return rows;
    }

    @ModelAttribute("employmentChoices")
    public List<Choice> employmentChoices() {
        List<Choice> rows = new ArrayList<>();
        for (EmployeeEmployment employment : employments.findAll()) {
            String who = employeeChoices().stream()
                    .filter(choice -> choice.id().equals(employment.employeeId))
                    .map(Choice::label)
                    .findFirst()
                    .orElse("员工");
            rows.add(new Choice(employment.id, who + " · " + employment.employeeNumber));
        }
        return rows;
    }

    @ModelAttribute("assignmentChoices")
    public List<Choice> assignmentChoices() {
        List<Choice> rows = new ArrayList<>();
        for (EmployeeDepartmentAssignment assignment : assignments.findAll()) {
            String who = employeeChoices().stream()
                    .filter(choice -> choice.id().equals(assignment.employeeId))
                    .map(Choice::label)
                    .findFirst()
                    .orElse("员工");
            Department department = departments.findById(assignment.departmentId).orElse(null);
            String name = department == null ? "部门" : department.name;
            rows.add(new Choice(assignment.id, who + " · " + name));
        }
        return rows;
    }

    @ModelAttribute("projectChoices")
    public List<Choice> projectChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Project project : projects.findAllByOrderByProjectCodeAsc()) {
            rows.add(new Choice(project.id, project.projectCode + " " + project.name));
        }
        return rows;
    }

    @ModelAttribute("stageChoices")
    public List<Choice> stageChoices() {
        List<Choice> rows = new ArrayList<>();
        for (ContractStage stage : stages.findAll()) {
            Contract contract = contracts.findById(stage.contractId).orElse(null);
            String prefix = contract == null ? "合同" : contract.contractNumber;
            rows.add(new Choice(stage.id, prefix + " · " + stage.stageCode + " " + stage.stageName));
        }
        return rows;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", logs.list());
        return "work-log/list";
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {
        model.addAttribute("items", logs.queue());
        model.addAttribute("activeNav", "work-approvals");
        return "work-log/approvals";
    }

    @GetMapping("/new")
    public String create(Model model) {
        WorkLogForm form = new WorkLogForm();
        form.setWorkDate(LocalDate.now());
        model.addAttribute("form", form);
        prepareForm(model);
        return "work-log/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var stored = logs.get(id);
        model.addAttribute("form", logs.form(id));
        model.addAttribute("stored", stored);
        model.addAttribute("locked", Codes.WORK_APPROVED.equals(stored.approvalStatus)
                || Codes.WORK_PENDING.equals(stored.approvalStatus));
        return "work-log/form";
    }

    @PostMapping
    public String create(
            @ModelAttribute("form") WorkLogForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        return save(form, binding, redirect, model);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") WorkLogForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return save(form, binding, redirect, model);
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, WorkLogDecisionForm decision, RedirectAttributes redirect) {
        try {
            logs.approve(id, decision);
            redirect.addFlashAttribute("message", "工时已批准，人工成本已按当时的成本率记入快照");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/work-logs/approvals";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, WorkLogDecisionForm decision, RedirectAttributes redirect) {
        try {
            logs.reject(id, decision);
            redirect.addFlashAttribute("message", "工时已驳回");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/work-logs/approvals";
    }

    private String save(WorkLogForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        prepareForm(model);
        if (binding.hasErrors()) {
            return "work-log/form";
        }
        try {
            long id = logs.save(form);
            redirect.addFlashAttribute("message", form.isSubmitForApproval() ? "工时已提交审批" : "工时草稿已保存");
            return "redirect:/work-logs/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "work-log/form";
        }
    }

    private void prepareForm(Model model) {
        model.addAttribute("stored", null);
        model.addAttribute("locked", false);
    }
}
