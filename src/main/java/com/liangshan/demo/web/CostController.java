package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.service.CostOccurrenceService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.web.form.CostForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/costs")
public class CostController {

    private final CostOccurrenceService costs;
    private final CostOccurrenceRepository costRepository;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final SupplierRepository suppliers;
    private final ProjectRepository projects;
    private final ContractRepository contracts;
    private final ContractStageRepository stages;

    public CostController(
            CostOccurrenceService costs,
            CostOccurrenceRepository costRepository,
            CompanyRepository companies,
            LegalPersonRepository legalPersons,
            EmployeeRepository employees,
            PersonRepository persons,
            SupplierRepository suppliers,
            ProjectRepository projects,
            ContractRepository contracts,
            ContractStageRepository stages) {
        this.costs = costs;
        this.costRepository = costRepository;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.employees = employees;
        this.persons = persons;
        this.suppliers = suppliers;
        this.projects = projects;
        this.contracts = contracts;
        this.stages = stages;
    }

    public record Choice(Long id, String label) {
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "costs";
    }

    @ModelAttribute("companyChoices")
    public List<Choice> companyChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Company company : companies.findAllByOrderByCompanyCodeAsc()) {
            rows.add(new Choice(company.id, company.companyCode + " " + company.name));
        }
        return rows;
    }

    @ModelAttribute("legalPersonChoices")
    public List<Choice> legalPersonChoices() {
        List<Choice> rows = new ArrayList<>();
        for (LegalPerson legalPerson : legalPersons.findAllByOrderByLegalNameAsc()) {
            rows.add(new Choice(legalPerson.id, legalPerson.legalName));
        }
        return rows;
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

    @ModelAttribute("supplierChoices")
    public List<Choice> supplierChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Supplier supplier : suppliers.findAllByOrderBySupplierCodeAsc()) {
            rows.add(new Choice(supplier.id, supplier.supplierCode + " " + supplier.supplierName));
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

    @ModelAttribute("contractChoices")
    public List<Choice> contractChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Contract contract : contracts.findAll()) {
            rows.add(new Choice(contract.id, contract.contractNumber + " " + contract.contractTitle));
        }
        return rows;
    }

    @ModelAttribute("costTypes")
    public List<String> costTypes() {
        return Codes.COST_TYPES;
    }

    @ModelAttribute("costBases")
    public List<String> costBases() {
        return Codes.COST_BASIS;
    }

    @ModelAttribute("approvalStatuses")
    public List<String> approvalStatuses() {
        return Codes.COST_APPROVAL_STATUS;
    }

    @ModelAttribute("recordStatuses")
    public List<String> recordStatuses() {
        return Codes.COST_RECORD_STATUS;
    }

    @ModelAttribute("allocationMethods")
    public List<String> allocationMethods() {
        return Codes.ALLOCATION_METHODS;
    }

    @ModelAttribute("allocationStatuses")
    public List<String> allocationStatuses() {
        return Codes.ALLOCATION_STATUS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", costs.list());
        return "cost/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        CostForm form = new CostForm();
        form.setIncurredAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        model.addAttribute("form", form);
        model.addAttribute("laborLocked", false);
        model.addAttribute("stored", null);
        return "cost/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", costs.form(id));
        model.addAttribute("laborLocked", costs.laborGenerated(id));
        CostOccurrence stored = costRepository.findById(id).orElse(null);
        model.addAttribute("stored", stored);
        return "cost/form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") CostForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return save(form, binding, redirect, model);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") CostForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return save(form, binding, redirect, model);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            costs.delete(id);
            redirect.addFlashAttribute("message", "费用已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/costs";
    }

    private String save(CostForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("laborLocked", form.getId() != null && costs.laborGenerated(form.getId()));
        model.addAttribute("stored", form.getId() == null ? null : costRepository.findById(form.getId()).orElse(null));
        if (binding.hasErrors()) {
            return "cost/form";
        }
        try {
            long id = costs.save(form);
            redirect.addFlashAttribute("message", "费用已保存");
            return "redirect:/costs/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "cost/form";
        }
    }
}
