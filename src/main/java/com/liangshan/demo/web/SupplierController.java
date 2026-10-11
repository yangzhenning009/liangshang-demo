package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.service.SupplierService;
import com.liangshan.demo.web.form.SupplierDetailForm;
import com.liangshan.demo.web.form.SupplierForm;
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
import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService suppliers;
    private final CompanyRepository companies;
    private final LegalPersonService legalPersons;
    private final PersonService persons;
    private final EmployeeService employees;

    public SupplierController(
            SupplierService suppliers,
            CompanyRepository companies,
            LegalPersonService legalPersons,
            PersonService persons,
            EmployeeService employees) {
        this.suppliers = suppliers;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "suppliers";
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

    @ModelAttribute("admissionStatuses")
    public List<String> admissionStatuses() {
        return Codes.ADMISSION_STATUS;
    }

    @ModelAttribute("cooperationStatuses")
    public List<String> cooperationStatuses() {
        return Codes.COOPERATION_STATUS;
    }

    @ModelAttribute("paymentBases")
    public List<String> paymentBases() {
        return Codes.PAYMENT_DUE_BASIS;
    }

    @ModelAttribute("contactRoles")
    public List<String> contactRoles() {
        return Codes.CONTACT_ROLES;
    }

    @ModelAttribute("contactStatuses")
    public List<String> contactStatuses() {
        return Codes.CONTACT_STATUS;
    }

    @ModelAttribute("categoryStatuses")
    public List<String> categoryStatuses() {
        return Codes.SUPPLY_CATEGORY_STATUS;
    }

    @ModelAttribute("reviewStatuses")
    public List<String> reviewStatuses() {
        return Codes.QUALIFICATION_REVIEW_STATUS;
    }

    @ModelAttribute("qualificationStatuses")
    public List<String> qualificationStatuses() {
        return Codes.QUALIFICATION_STATUS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", suppliers.list());
        return "supplier/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        SupplierForm form = new SupplierForm();
        form.setCooperationStatusEffectiveAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        model.addAttribute("form", form);
        model.addAttribute("identityLocked", false);
        return "supplier/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", suppliers.form(id));
        model.addAttribute("identityLocked", suppliers.referenced(id));
        return "supplier/form";
    }

    @PostMapping
    public String create(@ModelAttribute("form") SupplierForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        return saveHeader(form, binding, model, redirect);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") SupplierForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        form.setId(id);
        return saveHeader(form, binding, model, redirect);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("header", suppliers.header(id));
        model.addAttribute("form", suppliers.detailForm(id));
        model.addAttribute("versions", suppliers.versionHistory(id));
        return "supplier/detail";
    }

    @PostMapping("/{id}/details")
    public String saveDetails(
            @PathVariable Long id,
            @ModelAttribute("form") SupplierDetailForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        model.addAttribute("header", suppliers.header(id));
        model.addAttribute("versions", suppliers.versionHistory(id));
        if (binding.hasErrors()) {
            return "supplier/detail";
        }
        try {
            suppliers.saveDetails(id, form);
            redirect.addFlashAttribute("message", "联系人、供货类别和资质已保存");
            return "redirect:/suppliers/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "supplier/detail";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            suppliers.delete(id);
            redirect.addFlashAttribute("message", "供应商已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/suppliers";
    }

    private String saveHeader(SupplierForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        model.addAttribute("identityLocked", suppliers.referenced(form.getId()));
        if (binding.hasErrors()) {
            return "supplier/form";
        }
        try {
            long id = suppliers.save(form);
            redirect.addFlashAttribute("message", "供应商已保存");
            return "redirect:/suppliers/" + id;
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "supplier/form";
        }
    }
}
