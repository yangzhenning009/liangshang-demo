package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.service.DepartmentService;
import com.liangshan.demo.service.EmployeeOrgService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.web.form.EmployeeOrgForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employees/{employeeId}")
public class EmployeeOrgController {

    private final EmployeeOrgService organizations;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final DepartmentService departments;

    public EmployeeOrgController(
            EmployeeOrgService organizations,
            CompanyRepository companies,
            LegalPersonRepository legalPersons,
            DepartmentService departments) {
        this.organizations = organizations;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.departments = departments;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "employees";
    }

    @ModelAttribute("companies")
    public List<com.liangshan.demo.domain.Company> companies() {
        return companies.findAllByOrderByCompanyCodeAsc();
    }

    @ModelAttribute("legalPersons")
    public List<com.liangshan.demo.domain.LegalPerson> legalPersons() {
        return legalPersons.findAllByOrderByLegalNameAsc();
    }

    @ModelAttribute("departmentRows")
    public List<DepartmentService.DepartmentRow> departmentRows() {
        return departments.list();
    }

    @ModelAttribute("employmentTypes")
    public List<String> employmentTypes() {
        return Codes.EMPLOYMENT_TYPES;
    }

    @ModelAttribute("employmentStatuses")
    public List<String> employmentStatuses() {
        return Codes.EMPLOYMENT_STATUS;
    }

    @ModelAttribute("assignmentTypes")
    public List<String> assignmentTypes() {
        return Codes.ASSIGNMENT_TYPES;
    }

    @ModelAttribute("assignmentStatuses")
    public List<String> assignmentStatuses() {
        return Codes.ASSIGNMENT_STATUS;
    }

    @ModelAttribute("reportingTypes")
    public List<String> reportingTypes() {
        return Codes.REPORTING_TYPES;
    }

    @GetMapping
    public String detail(@PathVariable Long employeeId, Model model) {
        model.addAttribute("header", organizations.header(employeeId));
        model.addAttribute("form", organizations.form(employeeId));
        model.addAttribute("otherAssignments", organizations.otherAssignments(employeeId));
        return "employee/detail";
    }

    @PostMapping("/org")
    public String save(
            @PathVariable Long employeeId,
            @ModelAttribute("form") EmployeeOrgForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        model.addAttribute("header", organizations.header(employeeId));
        model.addAttribute("otherAssignments", organizations.otherAssignments(employeeId));
        if (binding.hasErrors()) {
            organizations.prepare(form);
            return "employee/detail";
        }
        try {
            organizations.save(employeeId, form);
            redirect.addFlashAttribute("message", "任职明细已保存");
            return "redirect:/employees/" + employeeId;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            organizations.prepare(form);
            return "employee/detail";
        }
    }
}
