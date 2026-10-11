package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.LegalPersonForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/legal-persons")
public class LegalPersonController {

    private final LegalPersonService legalPersons;
    private final PersonService persons;

    public LegalPersonController(LegalPersonService legalPersons, PersonService persons) {
        this.legalPersons = legalPersons;
        this.persons = persons;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "legal-persons";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", legalPersons.list());
        return "legal-person/list";
    }

    @ModelAttribute("legalPersonTypes")
    public java.util.List<String> legalPersonTypes() {
        return Codes.LEGAL_PERSON_TYPES;
    }

    @ModelAttribute("legalPersonStatuses")
    public java.util.List<String> legalPersonStatuses() {
        return Codes.LEGAL_PERSON_STATUS;
    }

    @ModelAttribute("registrationTypes")
    public java.util.List<String> registrationTypes() {
        return Codes.REGISTRATION_TYPES;
    }

    @ModelAttribute("registrationStatuses")
    public java.util.List<String> registrationStatuses() {
        return Codes.REGISTRATION_STATUS;
    }

    @ModelAttribute("persons")
    public java.util.List<com.liangshan.demo.domain.Person> persons() {
        return this.persons.list();
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new LegalPersonForm());
        return "legal-person/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", legalPersons.form(id));
        return "legal-person/form";
    }

    @PostMapping
    public String save(@ModelAttribute("form") LegalPersonForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "legal-person/form";
        }
        try {
            long id = legalPersons.save(form);
            redirect.addFlashAttribute("message", "法人已保存");
            return "redirect:/legal-persons/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "legal-person/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            legalPersons.delete(id);
            redirect.addFlashAttribute("message", "法人已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/legal-persons";
    }
}
