package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LaborCostRateService;
import com.liangshan.demo.web.form.LaborCostRateForm;
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
@RequestMapping("/labor-cost-rates")
public class LaborCostRateController {

    private final LaborCostRateService rates;
    private final WorkLogController workLogs;

    public LaborCostRateController(LaborCostRateService rates, WorkLogController workLogs) {
        this.rates = rates;
        this.workLogs = workLogs;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "labor-rates";
    }

    @ModelAttribute("employmentChoices")
    public List<WorkLogController.Choice> employmentChoices() {
        return workLogs.employmentChoices();
    }

    @ModelAttribute("employeeChoices")
    public List<WorkLogController.Choice> employeeChoices() {
        return workLogs.employeeChoices();
    }

    @ModelAttribute("costBases")
    public List<String> costBases() {
        return Codes.LABOR_COST_BASIS;
    }

    @ModelAttribute("approvalStatuses")
    public List<String> approvalStatuses() {
        return Codes.RATE_APPROVAL_STATUS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", rates.list());
        return "labor-rate/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        LaborCostRateForm form = new LaborCostRateForm();
        form.setValidFrom(LocalDate.now());
        model.addAttribute("form", form);
        return "labor-rate/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", rates.form(id));
        return "labor-rate/form";
    }

    @PostMapping
    public String create(
            @ModelAttribute("form") LaborCostRateForm form,
            BindingResult binding,
            RedirectAttributes redirect) {
        return save(form, binding, redirect);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") LaborCostRateForm form,
            BindingResult binding,
            RedirectAttributes redirect) {
        form.setId(id);
        return save(form, binding, redirect);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            rates.delete(id);
            redirect.addFlashAttribute("message", "成本率已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/labor-cost-rates";
    }

    private String save(LaborCostRateForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "labor-rate/form";
        }
        try {
            long id = rates.save(form);
            redirect.addFlashAttribute("message", "成本率已保存");
            return "redirect:/labor-cost-rates/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "labor-rate/form";
        }
    }
}
