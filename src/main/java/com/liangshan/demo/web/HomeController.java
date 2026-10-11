package com.liangshan.demo.web;

import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
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

    public HomeController(
            LegalPersonRepository legalPersons,
            CompanyRepository companies,
            PersonRepository persons,
            EmployeeRepository employees,
            DepartmentRepository departments) {
        this.legalPersons = legalPersons;
        this.companies = companies;
        this.persons = persons;
        this.employees = employees;
        this.departments = departments;
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
        return "home";
    }
}
