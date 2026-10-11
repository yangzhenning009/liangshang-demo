package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.CompanyService;
import com.liangshan.demo.service.DepartmentService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.web.form.DepartmentForm;
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
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CompanyService companyService;

    public DepartmentController(DepartmentService departmentService, CompanyService companyService) {
        this.departmentService = departmentService;
        this.companyService = companyService;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "departments";
    }

    @ModelAttribute("departmentTypes")
    public List<String> departmentTypes() {
        return Codes.DEPARTMENT_TYPES;
    }

    @ModelAttribute("departmentStatuses")
    public List<String> departmentStatuses() {
        return Codes.DEPARTMENT_STATUS;
    }

    @ModelAttribute("companies")
    public List<com.liangshan.demo.domain.Company> companies() {
        return companyService.list().stream().map(row -> companyService.get(row.id())).toList();
    }

    @ModelAttribute("departments")
    public List<com.liangshan.demo.domain.Department> departments() {
        return departmentService.list().stream().map(row -> departmentService.get(row.id())).toList();
    }

    @ModelAttribute("headLabels")
    public java.util.Map<Long, String> headLabels() {
        return departmentService.headLabels();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", departmentService.list());
        return "department/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new DepartmentForm());
        return "department/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", departmentService.form(id));
        return "department/form";
    }

    @PostMapping
    public String save(@ModelAttribute("form") DepartmentForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "department/form";
        }
        try {
            long id = departmentService.save(form);
            redirect.addFlashAttribute("message", "部门已保存");
            return "redirect:/departments/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "department/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            departmentService.delete(id);
            redirect.addFlashAttribute("message", "部门已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/departments";
    }
}
