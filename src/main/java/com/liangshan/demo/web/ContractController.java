package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.service.ContractService;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.FulfillmentService;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.ContractAmendmentForm;
import com.liangshan.demo.web.form.ContractDetailForm;
import com.liangshan.demo.web.form.ContractForm;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contracts;
    private final CompanyRepository companies;
    private final LegalPersonService legalPersons;
    private final PersonService persons;
    private final EmployeeService employees;
    private final SupplierRepository suppliers;
    private final ProjectRepository projects;
    private final FulfillmentService fulfillment;

    public ContractController(
            ContractService contracts,
            CompanyRepository companies,
            LegalPersonService legalPersons,
            PersonService persons,
            EmployeeService employees,
            SupplierRepository suppliers,
            ProjectRepository projects,
            FulfillmentService fulfillment) {
        this.contracts = contracts;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
        this.suppliers = suppliers;
        this.projects = projects;
        this.fulfillment = fulfillment;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "contracts";
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

    @ModelAttribute("suppliers")
    public List<com.liangshan.demo.domain.Supplier> suppliers() {
        return suppliers.findAllByOrderBySupplierCodeAsc();
    }

    @ModelAttribute("projectRows")
    public List<com.liangshan.demo.domain.Project> projectRows() {
        return projects.findAllByOrderByProjectCodeAsc();
    }

    @ModelAttribute("contractTypes")
    public List<String> contractTypes() {
        return Codes.CONTRACT_TYPES;
    }

    @ModelAttribute("pricingMethods")
    public List<String> pricingMethods() {
        return Codes.PRICING_METHODS;
    }

    @ModelAttribute("approvalStatuses")
    public List<String> approvalStatuses() {
        return Codes.CONTRACT_APPROVAL_STATUS;
    }

    @ModelAttribute("contractStatuses")
    public List<String> contractStatuses() {
        return Codes.CONTRACT_STATUS;
    }

    @ModelAttribute("stageStatuses")
    public List<String> stageStatuses() {
        return Codes.STAGE_STATUS;
    }

    @ModelAttribute("signatoryRoles")
    public List<String> signatoryRoles() {
        return Codes.SIGNATORY_ROLES;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", contracts.list());
        return "contract/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        ContractForm form = new ContractForm();
        form.setValidFrom(LocalDate.now());
        model.addAttribute("form", form);
        return "contract/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", contracts.form(id));
        return "contract/form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") ContractForm form, BindingResult binding, RedirectAttributes redirect) {
        return saveHeader(form, binding, redirect);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") ContractForm form,
            BindingResult binding,
            RedirectAttributes redirect) {
        form.setId(id);
        return saveHeader(form, binding, redirect);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        fillDetail(id, model);
        model.addAttribute("form", contracts.detailForm(id));
        model.addAttribute("amendment", contracts.amendmentDraft(id));
        return "contract/detail";
    }

    @PostMapping("/{id}/details")
    public String saveDetails(
            @PathVariable Long id,
            @ModelAttribute("form") ContractDetailForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        fillDetail(id, model);
        model.addAttribute("amendment", contracts.amendmentDraft(id));
        if (binding.hasErrors()) {
            model.addAttribute("detailErrors", messages(binding));
            return "contract/detail";
        }
        try {
            contracts.saveDetails(id, form);
            redirect.addFlashAttribute("message", "品项、阶段和关联已保存");
            return "redirect:/contracts/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            model.addAttribute("detailErrors", messages(binding));
            return "contract/detail";
        }
    }

    @PostMapping("/{id}/amendments")
    public String amend(
            @PathVariable Long id,
            @ModelAttribute("amendment") ContractAmendmentForm amendment,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        fillDetail(id, model);
        model.addAttribute("form", contracts.detailForm(id));
        if (binding.hasErrors()) {
            model.addAttribute("amendmentErrors", messages(binding));
            return "contract/detail";
        }
        try {
            contracts.registerAmendment(id, amendment);
            redirect.addFlashAttribute("message", "变更已登记，原条款版本仍保留");
            return "redirect:/contracts/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            model.addAttribute("amendmentErrors", messages(binding));
            return "contract/detail";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            contracts.delete(id);
            redirect.addFlashAttribute("message", "合同已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/contracts";
    }

    private void fillDetail(Long id, Model model) {
        model.addAttribute("header", contracts.header(id));
        model.addAttribute("versions", contracts.termHistory(id));
        model.addAttribute("stageHistory", contracts.stageHistory(id));
        model.addAttribute("lineChoices", contracts.lineChoices(id));
        model.addAttribute("stageChoices", contracts.stageChoices(id));
        model.addAttribute("progress", fulfillment.progress(id));
    }

    private List<String> messages(BindingResult binding) {
        return binding.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
    }

    private String saveHeader(ContractForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "contract/form";
        }
        try {
            long id = contracts.save(form);
            redirect.addFlashAttribute("message", "合同已保存");
            return "redirect:/contracts/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "contract/form";
        }
    }
}
