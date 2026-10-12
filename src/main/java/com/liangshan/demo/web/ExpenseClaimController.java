package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.service.ExpenseClaimService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.web.form.ExpenseClaimForm;
import com.liangshan.demo.web.form.WorkLogDecisionForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/expense-claims")
public class ExpenseClaimController {

    private final ExpenseClaimService claims;
    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final CompanyRepository companies;
    private final CostOccurrenceRepository costs;

    public ExpenseClaimController(
            ExpenseClaimService claims,
            EmployeeRepository employees,
            PersonRepository persons,
            CompanyRepository companies,
            CostOccurrenceRepository costs) {
        this.claims = claims;
        this.employees = employees;
        this.persons = persons;
        this.companies = companies;
        this.costs = costs;
    }

    public record Choice(Long id, String label) {
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "claims";
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

    @ModelAttribute("companyChoices")
    public List<Choice> companyChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Company company : companies.findAllByOrderByCompanyCodeAsc()) {
            rows.add(new Choice(company.id, company.companyCode + " " + company.name));
        }
        return rows;
    }

    @ModelAttribute("costChoices")
    public List<Choice> costChoices() {
        List<Choice> rows = new ArrayList<>();
        for (CostOccurrence cost : costs.findAllByOrderByIncurredAtDescIdDesc()) {
            String advance = cost.employeeAdvancedAmount == null ? "无垫付" : "垫付 " + cost.employeeAdvancedAmount;
            rows.add(new Choice(cost.id, cost.costNumber + " " + cost.costType + " " + cost.costAmount + " " + cost.currency + " · " + advance));
        }
        return rows;
    }

    @ModelAttribute("claimTypes")
    public List<String> claimTypes() {
        return Codes.CLAIM_TYPES;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", claims.list());
        return "claim/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new ExpenseClaimForm());
        model.addAttribute("locked", false);
        model.addAttribute("pending", false);
        model.addAttribute("payable", null);
        return "claim/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var stored = claims.get(id);
        model.addAttribute("form", claims.form(id));
        model.addAttribute("locked", Codes.CLAIM_APPROVED.equals(stored.approvalStatus) || Codes.CLAIM_PENDING.equals(stored.approvalStatus));
        model.addAttribute("pending", Codes.CLAIM_PENDING.equals(stored.approvalStatus));
        model.addAttribute("payable", claims.payableOf(id));
        model.addAttribute("approvalStatus", stored.approvalStatus);
        model.addAttribute("approvalComment", stored.approvalComment);
        return "claim/form";
    }

    @PostMapping
    public String create(
            @ModelAttribute("form") ExpenseClaimForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        return save(form, binding, redirect, model);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") ExpenseClaimForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return save(form, binding, redirect, model);
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, WorkLogDecisionForm decision, RedirectAttributes redirect) {
        try {
            claims.approve(id, decision);
            redirect.addFlashAttribute("message", "报销已批准，已形成对员工本人的应付");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/expense-claims/" + id + "/edit";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, WorkLogDecisionForm decision, RedirectAttributes redirect) {
        try {
            claims.reject(id, decision);
            redirect.addFlashAttribute("message", "报销已驳回");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/expense-claims/" + id + "/edit";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, Long confirmedByEmployeeId, RedirectAttributes redirect) {
        try {
            claims.payApprovedClaim(id, confirmedByEmployeeId);
            redirect.addFlashAttribute("message", "已按应付付给员工，没有另记成本");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/expense-claims/" + id + "/edit";
    }

    private String save(ExpenseClaimForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("locked", false);
        model.addAttribute("pending", false);
        model.addAttribute("payable", null);
        if (binding.hasErrors()) {
            return "claim/form";
        }
        try {
            long id = claims.save(form);
            redirect.addFlashAttribute("message", form.isSubmitForApproval() ? "报销已提交审批" : "报销草稿已保存");
            return "redirect:/expense-claims/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "claim/form";
        }
    }
}
