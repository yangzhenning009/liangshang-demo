package com.liangshan.demo.web;

import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.seed.SeedRunner;
import com.liangshan.demo.service.FieldErrorsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class SeedController {

    private final SeedRunner runner;
    private final EmployeeRepository employees;

    public SeedController(SeedRunner runner, EmployeeRepository employees) {
        this.runner = runner;
        this.employees = employees;
    }

    @GetMapping("/seed")
    public String page(Model model) {
        model.addAttribute("activeNav", "seed");
        model.addAttribute("stages", runner.stages());
        model.addAttribute("heroCount", heroCount());
        return "seed/index";
    }

    @PostMapping("/seed")
    public String load(RedirectAttributes redirect) {
        try {
            List<String> codes = runner.run();
            redirect.addFlashAttribute("message", "种子已载入：" + String.join("、", codes));
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/seed";
    }

    private long heroCount() {
        long count = 0;
        for (Employee employee : employees.findAll()) {
            if (employee.employeeCode != null && employee.employeeCode.startsWith("LS-E-")) {
                count++;
            }
        }
        return count;
    }
}
