package com.liangshan.demo.web;

import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.SourceDocumentRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.SupplierRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

    private final LegalPersonRepository legalPersons;
    private final CompanyRepository companies;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final SourceDocumentRepository documents;
    private final SupplierRepository suppliers;
    private final ProjectRepository projectRepository;

    public HomeController(
            LegalPersonRepository legalPersons,
            CompanyRepository companies,
            PersonRepository persons,
            EmployeeRepository employees,
            DepartmentRepository departments,
            SourceDocumentRepository documents,
            SupplierRepository suppliers,
            ProjectRepository projectRepository) {
        this.legalPersons = legalPersons;
        this.companies = companies;
        this.persons = persons;
        this.employees = employees;
        this.departments = departments;
        this.documents = documents;
        this.suppliers = suppliers;
        this.projectRepository = projectRepository;
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "home";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("legalPersonCount", legalPersons.count());
        model.addAttribute("companyCount", companies.count());
        model.addAttribute("personCount", persons.count());
        model.addAttribute("employeeCount", employees.count());
        model.addAttribute("departmentCount", departments.count());
        model.addAttribute("documentCount", documents.count());
        model.addAttribute("supplierCount", suppliers.count());
        model.addAttribute("projectCount", projectRepository.count());
        return "home";
    }
}
