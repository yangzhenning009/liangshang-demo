package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.ProjectService;
import com.liangshan.demo.service.SourceDocumentService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component("labels")
public class Labels {

    public String companyType(String code) {
        return Codes.companyTypeLabel(code);
    }

    public String dash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public String number(Integer value) {
        return value == null ? "—" : value.toString();
    }

    public String when(LocalDateTime value) {
        return value == null ? "—" : value.toString();
    }

    public String budgetTotal(BigDecimal labor, BigDecimal nonlabor) {
        BigDecimal total = ProjectService.budgetTotal(labor, nonlabor);
        return total == null ? "未编制" : total.toPlainString();
    }

    public String company(List<Company> companies, Long id) {
        if (id == null || companies == null) {
            return "—";
        }
        for (Company company : companies) {
            if (id.equals(company.id)) {
                return company.name;
            }
        }
        return "—";
    }

    public String employee(List<EmployeeService.EmployeeRow> rows, Long id) {
        if (id == null || rows == null) {
            return "—";
        }
        for (EmployeeService.EmployeeRow row : rows) {
            if (id.equals(row.id())) {
                return row.employeeCode() + " " + row.personName();
            }
        }
        return "—";
    }

    public String document(List<SourceDocumentService.Option> documents, Long id) {
        if (id == null || documents == null) {
            return "—";
        }
        for (SourceDocumentService.Option document : documents) {
            if (id.equals(document.id())) {
                return document.label();
            }
        }
        return "—";
    }

    public String customer(
            List<Company> companies,
            List<LegalPerson> legalPersons,
            List<Person> persons,
            Long companyId,
            Long legalPersonId,
            Long personId) {
        if (companyId != null) {
            return company(companies, companyId);
        }
        if (legalPersonId != null && legalPersons != null) {
            for (LegalPerson legalPerson : legalPersons) {
                if (legalPersonId.equals(legalPerson.id)) {
                    return legalPerson.legalName;
                }
            }
            return "—";
        }
        if (personId != null && persons != null) {
            for (Person person : persons) {
                if (personId.equals(person.id)) {
                    return person.name;
                }
            }
        }
        return "—";
    }
}
