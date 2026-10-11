package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.service.DepartmentService;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.service.ProjectService;
import com.liangshan.demo.web.form.ProjectDetailForm;
import com.liangshan.demo.web.form.ProjectForm;
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
import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projects;
    private final CompanyRepository companies;
    private final LegalPersonService legalPersons;
    private final PersonService persons;
    private final EmployeeService employees;
    private final DepartmentService departments;

    public ProjectController(
            ProjectService projects,
            CompanyRepository companies,
            LegalPersonService legalPersons,
            PersonService persons,
            EmployeeService employees,
            DepartmentService departments) {
        this.projects = projects;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
        this.departments = departments;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "projects";
    }

    @ModelAttribute("companies")
    public List<com.liangshan.demo.domain.Company> companies() {
        return companies.findAllByOrderByCompanyCodeAsc();
    }

    @ModelAttribute("legalPersons")
    public List<com.liangshan.demo.domain.LegalPerson> legalPersons() {
        return legalPersons.list();
    }

    @ModelAttribute("persons")
    public List<com.liangshan.demo.domain.Person> persons() {
        return persons.list();
    }

    @ModelAttribute("employeeRows")
    public List<EmployeeService.EmployeeRow> employeeRows() {
        return employees.list();
    }

    @ModelAttribute("departmentRows")
    public List<DepartmentService.DepartmentRow> departmentRows() {
        return departments.list();
    }

    @ModelAttribute("employmentChoices")
    public List<ProjectService.EmploymentChoice> employmentChoices() {
        return projects.employmentChoices();
    }

    @ModelAttribute("assignmentChoices")
    public List<ProjectService.AssignmentChoice> assignmentChoices() {
        return projects.assignmentChoices();
    }

    @ModelAttribute("projectTypes")
    public List<String> projectTypes() {
        return Codes.PROJECT_TYPES;
    }

    @ModelAttribute("projectStatuses")
    public List<String> projectStatuses() {
        return Codes.PROJECT_STATUS;
    }

    @ModelAttribute("projectRoles")
    public List<String> projectRoles() {
        return Codes.PROJECT_ROLES;
    }

    @ModelAttribute("costBases")
    public List<String> costBases() {
        return Codes.BUDGET_COST_BASIS;
    }

    @ModelAttribute("approvalStatuses")
    public List<String> approvalStatuses() {
        return Codes.BUDGET_APPROVAL_STATUS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", projects.list());
        return "project/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        ProjectForm form = new ProjectForm();
        form.setPlannedStartAt(LocalDate.now());
        form.setPlannedEndAt(LocalDate.now().plusMonths(6));
        model.addAttribute("form", form);
        return "project/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", projects.form(id));
        return "project/form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") ProjectForm form, BindingResult binding, RedirectAttributes redirect) {
        return saveHeader(form, binding, redirect);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") ProjectForm form,
            BindingResult binding,
            RedirectAttributes redirect) {
        form.setId(id);
        return saveHeader(form, binding, redirect);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("header", projects.header(id));
        model.addAttribute("form", projects.detailForm(id));
        model.addAttribute("versions", projects.versionHistory(id));
        return "project/detail";
    }

    @PostMapping("/{id}/details")
    public String saveDetails(
            @PathVariable Long id,
            @ModelAttribute("form") ProjectDetailForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        model.addAttribute("header", projects.header(id));
        model.addAttribute("versions", projects.versionHistory(id));
        if (binding.hasErrors()) {
            return "project/detail";
        }
        try {
            projects.saveDetails(id, form);
            redirect.addFlashAttribute("message", "成员、预算和分工已保存");
            return "redirect:/projects/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "project/detail";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            projects.delete(id);
            redirect.addFlashAttribute("message", "项目已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/projects";
    }

    private String saveHeader(ProjectForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "project/form";
        }
        try {
            long id = projects.save(form);
            redirect.addFlashAttribute("message", "项目已保存");
            return "redirect:/projects/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "project/form";
        }
    }
}
