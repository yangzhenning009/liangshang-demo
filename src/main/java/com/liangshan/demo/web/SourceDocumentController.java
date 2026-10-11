package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.SourceDocumentService;
import com.liangshan.demo.web.form.SourceDocumentForm;
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
@RequestMapping("/source-documents")
public class SourceDocumentController {

    private final SourceDocumentService documents;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeService employees;

    public SourceDocumentController(
            SourceDocumentService documents,
            CompanyRepository companies,
            LegalPersonRepository legalPersons,
            PersonRepository persons,
            EmployeeService employees) {
        this.documents = documents;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "documents";
    }

    @ModelAttribute("documentStatuses")
    public List<String> documentStatuses() {
        return Codes.DOCUMENT_STATUS;
    }

    @ModelAttribute("companies")
    public List<com.liangshan.demo.domain.Company> companies() {
        return companies.findAllByOrderByCompanyCodeAsc();
    }

    @ModelAttribute("legalPersons")
    public List<com.liangshan.demo.domain.LegalPerson> legalPersons() {
        return legalPersons.findAllByOrderByLegalNameAsc();
    }

    @ModelAttribute("persons")
    public List<com.liangshan.demo.domain.Person> persons() {
        return persons.findAllByOrderByNameAsc();
    }

    @ModelAttribute("employeeRows")
    public List<EmployeeService.EmployeeRow> employeeRows() {
        return employees.list();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", documents.list());
        return "source-document/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new SourceDocumentForm());
        return "source-document/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("form", documents.form(id));
        return "source-document/form";
    }

    @PostMapping
    public String save(
            @ModelAttribute("form") SourceDocumentForm form,
            BindingResult binding,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "source-document/form";
        }
        try {
            long id = documents.save(form);
            redirect.addFlashAttribute("message", "来源材料已保存");
            return "redirect:/source-documents/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "source-document/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            documents.delete(id);
            redirect.addFlashAttribute("message", "来源材料已删除");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/source-documents";
    }
}
