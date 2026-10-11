package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.PersonForm;
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
@RequestMapping("/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "persons";
    }

    @ModelAttribute("personStatuses")
    public List<String> personStatuses() {
        return Codes.PERSON_STATUS;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", personService.list());
        return "person/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new PersonForm());
        return "person/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", personService.form(id));
        return "person/form";
    }

    @PostMapping
    public String save(@ModelAttribute("form") PersonForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "person/form";
        }
        try {
            long id = personService.save(form);
            redirect.addFlashAttribute("message", "自然人已保存");
            return "redirect:/persons/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "person/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            personService.delete(id);
            redirect.addFlashAttribute("message", "自然人已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/persons";
    }
}
