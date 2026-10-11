package com.liangshan.demo;

import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.CompanyRegistration;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.CompanyRegistrationRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CompanyVersionRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.service.CompanyService;
import com.liangshan.demo.service.DepartmentService;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.CompanyForm;
import com.liangshan.demo.web.form.DepartmentForm;
import com.liangshan.demo.web.form.EmployeeForm;
import com.liangshan.demo.web.form.LegalPersonForm;
import com.liangshan.demo.web.form.PersonForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MasterDataDemoTest {

    static {
        try {
            Files.deleteIfExists(Path.of("target/test-liangshan.db"));
            Files.deleteIfExists(Path.of("target/test-liangshan.db-wal"));
            Files.deleteIfExists(Path.of("target/test-liangshan.db-shm"));
        } catch (Exception ignored) {
            // A previous process may still hold the file; create-drop is not required for a fresh file name.
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private LegalPersonService legalPersons;
    @Autowired
    private CompanyService companies;
    @Autowired
    private PersonService persons;
    @Autowired
    private EmployeeService employees;
    @Autowired
    private DepartmentService departments;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyRegistrationRepository registrationRepository;
    @Autowired
    private CompanyVersionRepository versionRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void schemaUsesOnlyBasicConstraints() {
        List<String> sql = jdbc.queryForList("select sql from sqlite_master where sql is not null", String.class);
        assertFalse(sql.isEmpty());
        for (String statement : sql) {
            String normalized = statement.toLowerCase(Locale.ROOT);
            assertFalse(normalized.contains("foreign key"), statement);
            assertFalse(normalized.contains("references"), statement);
            assertFalse(normalized.matches("(?s).*\\bcheck\\s*\\(.*"), statement);
            assertFalse(normalized.contains("unique"), statement);
        }
        String company = jdbc.queryForObject(
                "select sql from sqlite_master where type = 'table' and name = 'company'", String.class);
        assertTrue(company.toLowerCase(Locale.ROOT).contains("company_code"));
        assertTrue(company.toLowerCase(Locale.ROOT).contains("not null"));
        List<String> tables = jdbc.queryForList(
                "select name from sqlite_master where type = 'table' order by name", String.class);
        assertTrue(tables.containsAll(List.of(
                "legal_person",
                "legal_person_registration",
                "legal_representative_term",
                "company",
                "company_registration",
                "company_bank_account",
                "company_shareholding",
                "company_version",
                "person",
                "employee",
                "department",
                "department_version",
                "department_service_company")));
    }

    @Test
    void homePageListsMasterData() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("梁山集团")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("法人")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("公司")));
    }

    @Test
    void companyTypeRulesAndRegistrationSync() throws Exception {
        long legalPersonId = legalPersons.save(legalPerson("梁山集团法人"));
        long otherLegalPersonId = legalPersons.save(legalPerson("清风观法人"));

        CompanyForm missingLegalPerson = company("LS-NO-LP", "缺法人公司", "company");
        assertThrows(FieldErrorsException.class, () -> companies.save(missingLegalPerson));

        CompanyForm head = company("LS-HQ", "梁山集团有限公司", "company");
        head.setLegalPersonId(legalPersonId);
        head.setCompanyForm("有限责任公司");
        head.getRegistrations().add(registration(1, "91370100MA3C12345X", LocalDate.now().minusDays(1)));
        long headId = companies.save(head);

        Company stored = companyRepository.findById(headId).orElseThrow();
        assertEquals("91370100MA3C12345X", stored.unifiedSocialCreditCode);
        assertEquals("91370100MA3C12345X", stored.taxpayerIdentificationNumber);
        assertNull(stored.headCompanyId);
        assertEquals(1, versionRepository.findByCompanyIdOrderByVersionNumberAsc(headId).size());

        CompanyForm duplicateLegalPerson = company("LS-DUP", "重复法人", "company");
        duplicateLegalPerson.setLegalPersonId(legalPersonId);
        duplicateLegalPerson.setCompanyForm("有限责任公司");
        assertThrows(FieldErrorsException.class, () -> companies.save(duplicateLegalPerson));

        CompanyForm branch = company("LS-SH", "梁山集团上海分公司", "branch");
        branch.setHeadCompanyId(headId);
        branch.setLegalPersonId(otherLegalPersonId);
        branch.setCompanyForm("有限责任公司");
        branch.getShareholdings().add(share(headId));
        assertThrows(FieldErrorsException.class, () -> companies.save(branch));

        branch.getShareholdings().clear();
        long branchId = companies.save(branch);
        Company storedBranch = companyRepository.findById(branchId).orElseThrow();
        assertNull(storedBranch.legalPersonId);
        assertNull(storedBranch.companyForm);
        assertEquals(headId, storedBranch.headCompanyId);

        CompanyForm branchHead = company("LS-BAD", "分公司不能当总公司", "branch");
        branchHead.setHeadCompanyId(branchId);
        assertThrows(FieldErrorsException.class, () -> companies.save(branchHead));

        CompanyForm edited = companies.form(headId);
        edited.setName("梁山集团有限公司（更名）");
        edited.getRegistrations().get(0).setUnifiedSocialCreditCode("91370100MA3C12345Y");
        edited.getRegistrations().get(0).setTaxpayerIdentificationNumber("91370100MA3C12345Y");
        companies.save(edited);

        List<CompanyRegistration> history = registrationRepository.findByCompanyIdOrderByVersionNumberAsc(headId);
        assertEquals(2, history.size());
        assertEquals("91370100MA3C12345X", history.get(0).unifiedSocialCreditCode);
        assertEquals(LocalDate.now(), history.get(0).validTo);
        assertEquals("91370100MA3C12345Y", history.get(1).unifiedSocialCreditCode);
        assertNull(history.get(1).validTo);
        assertEquals("91370100MA3C12345Y", companyRepository.findById(headId).orElseThrow().unifiedSocialCreditCode);
        assertTrue(versionRepository.findByCompanyIdOrderByVersionNumberAsc(headId).size() >= 2);

        mvc.perform(get("/companies")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("梁山集团有限公司（更名）")));
    }

    @Test
    void personEmployeeAreOneToOneAndBlankBecomesNull() {
        PersonForm person = new PersonForm();
        person.setName("林冲");
        person.setMobilePhone("   ");
        person.setStatus("有效");
        long personId = persons.save(person);
        Person stored = personRepository.findById(personId).orElseThrow();
        assertNull(stored.mobilePhone);

        EmployeeForm employee = new EmployeeForm();
        employee.setPersonId(personId);
        employee.setEmployeeCode("LS-E-001");
        employee.setStatus("有效");
        long employeeId = employees.save(employee);
        assertEquals(personId, employeeRepository.findById(employeeId).orElseThrow().personId);

        EmployeeForm again = new EmployeeForm();
        again.setPersonId(personId);
        again.setEmployeeCode("LS-E-002");
        again.setStatus("有效");
        assertThrows(FieldErrorsException.class, () -> employees.save(again));

        long ownerLegalPersonId = legalPersons.save(legalPerson("雇佣测试法人"));
        CompanyForm owner = company("LS-EMP", "雇佣测试公司", "company");
        owner.setLegalPersonId(ownerLegalPersonId);
        owner.setCompanyForm("有限责任公司");
        long ownerId = companies.save(owner);

        DepartmentForm department = new DepartmentForm();
        department.setCompanyId(ownerId);
        department.setDepartmentCode("HQ-OPS");
        department.setName("总经办");
        department.setDepartmentType("部门");
        department.setDepartmentHeadEmployeeId(employeeId);
        department.setValidFrom(LocalDate.now().minusDays(10));
        department.setStatus("有效");
        long departmentId = departments.save(department);

        DepartmentForm child = new DepartmentForm();
        child.setCompanyId(department.getCompanyId());
        child.setParentDepartmentId(departmentId);
        child.setDepartmentCode("HQ-OPS");
        child.setName("重复编号");
        child.setDepartmentType("团队");
        child.setValidFrom(LocalDate.now());
        child.setStatus("有效");
        assertThrows(FieldErrorsException.class, () -> departments.save(child));

        child.setDepartmentCode("HQ-OPS-2");
        child.setParentDepartmentId(departmentId);
        long childId = departments.save(child);
        DepartmentForm cycle = departments.form(departmentId);
        cycle.setParentDepartmentId(childId);
        assertThrows(FieldErrorsException.class, () -> departments.save(cycle));
    }

    @Test
    void browserCanCreateLegalPerson() throws Exception {
        mvc.perform(post("/legal-persons")
                        .param("legalName", "页面法人")
                        .param("legalPersonType", "事业单位法人")
                        .param("status", "存续"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/legal-persons/*/edit"));
        mvc.perform(get("/legal-persons"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("页面法人")));
    }

    private LegalPersonForm legalPerson(String name) {
        LegalPersonForm form = new LegalPersonForm();
        form.setLegalName(name);
        form.setLegalPersonType("企业法人");
        form.setStatus("存续");
        return form;
    }

    private CompanyForm company(String code, String name, String type) {
        CompanyForm form = new CompanyForm();
        form.setCompanyCode(code);
        form.setName(name);
        form.setCompanyType(type);
        form.setStatus("存续");
        form.setEstablishedAt(LocalDate.now().minusYears(3));
        return form;
    }

    private CompanyForm.RegistrationForm registration(int version, String code, LocalDate validFrom) {
        CompanyForm.RegistrationForm row = new CompanyForm.RegistrationForm();
        row.setVersionNumber(version);
        row.setRegistrationType("设立登记");
        row.setRegistrationStatus("存续");
        row.setUnifiedSocialCreditCode(code);
        row.setTaxpayerIdentificationNumber(code);
        row.setRegistrationAuthority("济南市市场监督管理局");
        row.setRegistrationJurisdiction("中国");
        row.setRegisteredName("梁山集团有限公司");
        row.setRegisteredAddress("山东省济南市梁山路 1 号");
        row.setRegisteredAt(validFrom);
        row.setValidFrom(validFrom);
        row.setRegisteredCapitalAmount(new BigDecimal("10000000.00"));
        row.setRegisteredCapitalCurrency("CNY");
        return row;
    }

    private CompanyForm.ShareholdingForm share(Long shareholderCompanyId) {
        CompanyForm.ShareholdingForm row = new CompanyForm.ShareholdingForm();
        row.setShareholderCompanyId(shareholderCompanyId);
        row.setShareClass("普通股");
        row.setOwnershipRatio(new BigDecimal("1"));
        row.setValidFrom(LocalDate.now());
        return row;
    }
}
