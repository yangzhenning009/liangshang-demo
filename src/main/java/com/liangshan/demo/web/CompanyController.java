package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.CompanyService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.CompanyForm;
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
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final LegalPersonService legalPersonService;
    private final PersonService personService;

    public CompanyController(CompanyService companyService, LegalPersonService legalPersonService, PersonService personService) {
        this.companyService = companyService;
        this.legalPersonService = legalPersonService;
        this.personService = personService;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "companies";
    }

    @ModelAttribute("companyTypes")
    public List<String> companyTypes() {
        return Codes.COMPANY_TYPES;
    }

    @ModelAttribute("companyStatuses")
    public List<String> companyStatuses() {
        return Codes.COMPANY_STATUS;
    }

    @ModelAttribute("companyForms")
    public List<String> companyForms() {
        return Codes.COMPANY_FORMS;
    }

    @ModelAttribute("registrationTypes")
    public List<String> registrationTypes() {
        return Codes.REGISTRATION_TYPES;
    }

    @ModelAttribute("registrationStatuses")
    public List<String> registrationStatuses() {
        return Codes.REGISTRATION_STATUS;
    }

    @ModelAttribute("bankStatuses")
    public List<String> bankStatuses() {
        return Codes.BANK_STATUS;
    }

    @ModelAttribute("shareClasses")
    public List<String> shareClasses() {
        return Codes.SHARE_CLASSES;
    }

    @ModelAttribute("legalPersons")
    public List<com.liangshan.demo.domain.LegalPerson> legalPersons() {
        return legalPersonService.list();
    }

    @ModelAttribute("persons")
    public List<com.liangshan.demo.domain.Person> persons() {
        return personService.list();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", companyService.list());
        return "company/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new CompanyForm());
        model.addAttribute("headCompanies", companyService.headChoices(null));
        model.addAttribute("shareholderCompanies", companyService.headChoices(null));
        return "company/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", companyService.form(id));
        model.addAttribute("headCompanies", companyService.headChoices(id));
        model.addAttribute("shareholderCompanies", companyService.headChoices(id));
        return "company/form";
    }

    @PostMapping
    public String save(@ModelAttribute("form") CompanyForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        model.addAttribute("headCompanies", companyService.headChoices(form.getId()));
        model.addAttribute("shareholderCompanies", companyService.headChoices(form.getId()));
        if (binding.hasErrors()) {
            return "company/form";
        }
        try {
            long id = companyService.save(form);
            redirect.addFlashAttribute("message", "公司已保存");
            return "redirect:/companies/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "company/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            companyService.delete(id);
            redirect.addFlashAttribute("message", "公司已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/companies";
    }
}
