package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.CompanyRegistrationRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CompanyShareholdingRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalRepresentativeTermRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.PersonForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final LegalRepresentativeTermRepository representativeTerms;
    private final CompanyRepository companies;
    private final CompanyRegistrationRepository companyRegistrations;
    private final CompanyShareholdingRepository shareholdings;
    private final SupplierLinkGuard supplierLinks;
    private final ProjectLinkGuard projectLinks;
    private final ContractLinkGuard contractLinks;
    private final FulfillmentLinkGuard fulfillmentLinks;

    public List<Person> list() {
        return persons.findAllByOrderByNameAsc();
    }

    public Person get(Long id) {
        return persons.findById(id).orElseThrow(NotFoundException::new);
    }

    public PersonForm form(Long id) {
        Person person = get(id);
        PersonForm form = new PersonForm();
        form.setId(person.id);
        form.setName(person.name);
        form.setMobilePhone(person.mobilePhone);
        form.setEmail(person.email);
        form.setStatus(person.status);
        return form;
    }

    @Transactional
    public long save(PersonForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validate(form, errors);
        errors.throwIfAny();

        Person person = form.getId() == null ? new Person() : get(form.getId());
        person.name = form.getName();
        person.mobilePhone = form.getMobilePhone();
        person.email = form.getEmail();
        person.status = form.getStatus();
        return persons.save(person).id;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (!employees.findByPersonId(id).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人已有员工档案，不能删除"));
        }
        if (!representativeTerms.findByPersonId(id).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍有法定代表人任期，不能删除"));
        }
        if (!companies.findByBranchManagerPersonId(id).isEmpty()
                || !companyRegistrations.findByBranchManagerPersonId(id).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍被登记为分公司负责人，不能删除"));
        }
        if (!shareholdings.findByShareholderPersonId(id).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍是股东，不能删除"));
        }
        if (supplierLinks.personUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍被供应商档案引用，不能删除"));
        }
        if (projectLinks.personUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍被项目引用，不能删除"));
        }
        if (contractLinks.personUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍被合同引用，不能删除"));
        }
        if (fulfillmentLinks.personUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该自然人仍被交付或验收引用，不能删除"));
        }
        persons.deleteById(id);
    }

    private void normalize(PersonForm form) {
        form.setName(Text.blankToNull(form.getName()));
        form.setMobilePhone(Text.blankToNull(form.getMobilePhone()));
        form.setEmail(Text.blankToNull(form.getEmail()));
        form.setStatus(Text.blankToNull(form.getStatus()));
    }

    private void validate(PersonForm form, FieldErrors errors) {
        if (form.getName() == null) {
            errors.add("name", "请填写姓名");
        }
        if (form.getStatus() == null || !Codes.PERSON_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择自然人状态");
        }
        if (!Text.emailOk(form.getEmail())) {
            errors.add("email", "邮箱格式不正确");
        }
    }
}
