package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.service.ContractService;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.FulfillmentService;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.AcceptanceEventForm;
import com.liangshan.demo.web.form.ContractPlanForm;
import com.liangshan.demo.web.form.DeliveryEventForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/contracts")
public class FulfillmentController {

    private final FulfillmentService fulfillment;
    private final ContractService contracts;
    private final CompanyRepository companies;
    private final LegalPersonService legalPersons;
    private final PersonService persons;
    private final EmployeeService employees;
    private final ProjectRepository projects;

    public FulfillmentController(
            FulfillmentService fulfillment,
            ContractService contracts,
            CompanyRepository companies,
            LegalPersonService legalPersons,
            PersonService persons,
            EmployeeService employees,
            ProjectRepository projects) {
        this.fulfillment = fulfillment;
        this.contracts = contracts;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
        this.projects = projects;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "contracts";
    }

    @ModelAttribute("companies")
    public List<Company> companies() {
        return companies.findAllByOrderByCompanyCodeAsc();
    }

    @ModelAttribute("legalPersons")
    public List<LegalPerson> legalPersons() {
        return legalPersons.list();
    }

    @ModelAttribute("persons")
    public List<Person> persons() {
        return persons.list();
    }

    @ModelAttribute("employeeRows")
    public List<EmployeeService.EmployeeRow> employeeRows() {
        return employees.list();
    }

    @ModelAttribute("projectRows")
    public List<Project> projectRows() {
        return projects.findAllByOrderByProjectCodeAsc();
    }

    @ModelAttribute("billingTriggers")
    public List<String> billingTriggers() {
        return Codes.BILLING_TRIGGERS;
    }

    @ModelAttribute("paymentTriggers")
    public List<String> paymentTriggers() {
        return Codes.PAYMENT_TRIGGERS;
    }

    @ModelAttribute("paymentPurposes")
    public List<String> paymentPurposes() {
        return Codes.PAYMENT_PURPOSES;
    }

    @GetMapping("/{id}/plans")
    public String plans(@PathVariable Long id, Model model) {
        fill(id, model);
        model.addAttribute("planForm", fulfillment.planForm(id));
        return "contract/plans";
    }

    @PostMapping("/{id}/plans")
    public String savePlans(
            @PathVariable Long id,
            @ModelAttribute("planForm") ContractPlanForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        fill(id, model);
        if (binding.hasErrors()) {
            return "contract/plans";
        }
        try {
            fulfillment.savePlans(id, form);
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "contract/plans";
        }
        redirect.addFlashAttribute("message", "开票计划和收付款计划已保存");
        return "redirect:/contracts/" + id + "/plans";
    }

    @GetMapping("/{id}/performance")
    public String performance(@PathVariable Long id, Model model) {
        fill(id, model);
        if (!model.containsAttribute("deliveryForm")) {
            DeliveryEventForm draft = new DeliveryEventForm();
            draft.getLines().add(new DeliveryEventForm.Line());
            model.addAttribute("deliveryForm", draft);
        }
        return "contract/performance";
    }

    @PostMapping("/{id}/deliveries")
    public String recordDelivery(
            @PathVariable Long id,
            @ModelAttribute("deliveryForm") DeliveryEventForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            fill(id, model);
            model.addAttribute("deliveryErrors", messages(binding));
            return "contract/performance";
        }
        try {
            fulfillment.recordDelivery(id, form);
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            fill(id, model);
            model.addAttribute("deliveryErrors", messages(binding));
            return "contract/performance";
        }
        redirect.addFlashAttribute("message", "交付已记录");
        return "redirect:/contracts/" + id + "/performance";
    }

    @PostMapping("/{id}/acceptances")
    public String recordAcceptance(
            @PathVariable Long id,
            @ModelAttribute("acceptanceForm") AcceptanceEventForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            fill(id, model);
            model.addAttribute("deliveryForm", blankDelivery());
            model.addAttribute("acceptanceErrors", messages(binding));
            return "contract/performance";
        }
        try {
            fulfillment.recordAcceptance(id, form);
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            fill(id, model);
            model.addAttribute("deliveryForm", blankDelivery());
            model.addAttribute("acceptanceErrors", messages(binding));
            return "contract/performance";
        }
        redirect.addFlashAttribute("message", "验收已记录");
        return "redirect:/contracts/" + id + "/performance";
    }

    @PostMapping("/{id}/deliveries/{deliveryId}/withdraw")
    public String withdraw(
            @PathVariable Long id,
            @PathVariable Long deliveryId,
            RedirectAttributes redirect) {
        try {
            fulfillment.withdrawDelivery(id, deliveryId);
            redirect.addFlashAttribute("message", "交付已撤回");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/contracts/" + id + "/performance";
    }

    private void fill(Long id, Model model) {
        model.addAttribute("header", contracts.header(id));
        model.addAttribute("progress", fulfillment.progress(id));
        model.addAttribute("terms", fulfillment.terms(id));
        model.addAttribute("stages", fulfillment.stageChoices(id));
        model.addAttribute("linkedProjects", fulfillment.linkedProjects(id));
        model.addAttribute("deliverableChoices", fulfillment.deliverableChoices(id));
        model.addAttribute("deliveryViews", fulfillment.deliveries(id));
    }

    private DeliveryEventForm blankDelivery() {
        DeliveryEventForm draft = new DeliveryEventForm();
        draft.getLines().add(new DeliveryEventForm.Line());
        return draft;
    }

    private List<String> messages(BindingResult binding) {
        return binding.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
    }
}
