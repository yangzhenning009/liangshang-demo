package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.web.form.EmployeeForm;
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
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "employees";
    }

    @ModelAttribute("employeeStatuses")
    public List<String> employeeStatuses() {
        return Codes.EMPLOYEE_STATUS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", employeeService.list());
        return "employee/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new EmployeeForm());
        model.addAttribute("selectablePersons", employeeService.selectablePersons(null));
        return "employee/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        EmployeeForm form = employeeService.form(id);
        model.addAttribute("form", form);
        model.addAttribute("selectablePersons", employeeService.selectablePersons(form.getId()));
        return "employee/form";
    }

    @PostMapping
    public String save(@ModelAttribute("form") EmployeeForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        model.addAttribute("selectablePersons", employeeService.selectablePersons(form.getId()));
        if (binding.hasErrors()) {
            return "employee/form";
        }
        try {
            long id = employeeService.save(form);
            redirect.addFlashAttribute("message", "员工已保存");
            return "redirect:/employees/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "employee/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            employeeService.delete(id);
            redirect.addFlashAttribute("message", "员工已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/employees";
    }
}
