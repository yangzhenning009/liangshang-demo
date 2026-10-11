package com.liangshan.demo;

import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.CompanyRegistration;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.SupplierVersion;
import com.liangshan.demo.repository.CompanyRegistrationRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CompanyVersionRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.repository.SupplierVersionRepository;
import com.liangshan.demo.service.CompanyService;
import com.liangshan.demo.service.DepartmentService;
import com.liangshan.demo.service.EmployeeOrgService;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.SourceDocumentService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.service.SupplierService;
import com.liangshan.demo.web.form.CompanyForm;
import com.liangshan.demo.web.form.DepartmentForm;
import com.liangshan.demo.web.form.EmployeeForm;
import com.liangshan.demo.web.form.EmployeeOrgForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.AssignmentForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.EmploymentForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.ReportingForm;
import com.liangshan.demo.web.form.SourceDocumentForm;
import com.liangshan.demo.web.form.SupplierDetailForm;
import com.liangshan.demo.web.form.SupplierDetailForm.ContactForm;
import com.liangshan.demo.web.form.SupplierDetailForm.QualificationForm;
import com.liangshan.demo.web.form.SupplierForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.function.Executable;
import com.liangshan.demo.web.form.LegalPersonForm;
import com.liangshan.demo.web.form.PersonForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
@Import(SupplierUsageProbe.class)
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
    @Autowired
    private EmployeeOrgService organizations;
    @Autowired
    private SourceDocumentService sourceDocuments;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private SupplierVersionRepository supplierVersions;

    @AfterEach
    void clearSupplierReferences() {
        SupplierUsageProbe.REFERENCED.clear();
    }

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
                "department_service_company",
                "employee_employment",
                "employee_department_assignment",
                "employee_reporting_line",
                "source_document",
                "supplier",
                "supplier_contact",
                "supplier_supply_category",
                "supplier_qualification",
                "supplier_version")));
        Map<String, Object> supplierId = column("supplier_version", "id");
        assertEquals("integer", String.valueOf(supplierId.get("type")).toLowerCase(Locale.ROOT));
        assertEquals(1, ((Number) column("supplier", "supplier_code").get("notnull")).intValue());
        assertEquals(1, ((Number) column("supplier", "purchasing_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("supplier", "company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("supplier", "legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("supplier", "person_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("supplier_contact", "person_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("supplier_qualification", "has_no_expiry").get("notnull")).intValue());
        assertEquals(0, ((Number) column("supplier_qualification", "qualification_document_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("supplier_qualification", "expires_on").get("notnull")).intValue());
    }

    @Test
    void homePageListsMasterData() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("梁山集团")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("法人")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("公司")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("供应商")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("基础管理")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("采购")));
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
    void companyFormRendersRuleErrors() throws Exception {
        long legalId = legalPersons.save(legalPerson("校验法人"));
        mvc.perform(post("/companies")
                        .param("companyCode", "LS-ERR")
                        .param("name", "缺形式")
                        .param("companyType", "company")
                        .param("status", "存续")
                        .param("legalPersonId", String.valueOf(legalId)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("请选择公司形式")));
    }

    @Test
    void employmentPeriodsReportingCycleAndDocuments() throws Exception {
        long companyId = companyForOrg("LS-ORG", "任职公司");
        long otherCompanyId = companyForOrg("LS-ORG-2", "另一用人公司");
        long departmentId = department(companyId, "ORG-HR", "人事部");
        long songId = employee("宋江", "E-SONG");
        long luId = employee("卢俊义", "E-LU");

        EmployeeOrgForm bothEmployers = new EmployeeOrgForm();
        EmploymentForm mixed = employment("E-1", companyId, null);
        mixed.setEmployerLegalPersonId(legalPersons.save(legalPerson("多余法人")));
        bothEmployers.getEmployments().add(mixed);
        assertTrue(messageOf(() -> organizations.save(songId, bothEmployers)).contains("恰好选择一个"));

        EmployeeOrgForm song = new EmployeeOrgForm();
        song.getEmployments().add(employment("GH-1", companyId, null));
        organizations.save(songId, song);
        EmployeeOrgForm duplicate = new EmployeeOrgForm();
        duplicate.getEmployments().add(employment("GH-1", companyId, null));
        assertTrue(messageOf(() -> organizations.save(luId, duplicate)).contains("用工编号已存在"));

        EmployeeOrgForm otherEmployer = new EmployeeOrgForm();
        otherEmployer.getEmployments().add(employment("GH-1", otherCompanyId, null));
        organizations.save(luId, otherEmployer);

        EmployeeOrgForm outside = organizations.form(songId);
        AssignmentForm early = assignment(outside.getEmployments().get(0).getRowKey(), departmentId,
                LocalDate.of(2010, 1, 1), null);
        outside.getAssignments().add(early);
        assertTrue(messageOf(() -> organizations.save(songId, outside)).contains("任职期间必须落在雇佣有效期内"));

        EmployeeOrgForm assigned = organizations.form(songId);
        assigned.getAssignments().add(assignment(assigned.getEmployments().get(0).getRowKey(), departmentId,
                LocalDate.of(2020, 1, 1), null));
        organizations.save(songId, assigned);

        EmployeeOrgForm luAssigned = organizations.form(luId);
        luAssigned.getAssignments().add(assignment(luAssigned.getEmployments().get(0).getRowKey(), departmentId,
                LocalDate.of(2020, 1, 1), null));
        organizations.save(luId, luAssigned);

        EmployeeOrgForm selfReport = organizations.form(songId);
        String songAssignment = selfReport.getAssignments().get(0).getRowKey();
        selfReport.getReportingLines().add(reporting(songAssignment, songAssignment, "行政"));
        assertTrue(messageOf(() -> organizations.save(songId, selfReport)).contains("不能向自身任职汇报"));

        EmployeeOrgForm songReports = organizations.form(songId);
        String managerKey = "id:" + organizations.form(luId).getAssignments().get(0).getId();
        songReports.getReportingLines().add(reporting(songReports.getAssignments().get(0).getRowKey(), managerKey, "行政"));
        organizations.save(songId, songReports);

        EmployeeOrgForm cycle = organizations.form(luId);
        cycle.getReportingLines().add(reporting(
                cycle.getAssignments().get(0).getRowKey(),
                "id:" + organizations.form(songId).getAssignments().get(0).getId(),
                "行政"));
        assertTrue(messageOf(() -> organizations.save(luId, cycle)).contains("行政汇报链不能形成循环"));

        cycle.getReportingLines().get(0).setReportingType("职能");
        organizations.save(luId, cycle);

        EmployeeOrgForm songFunctional = organizations.form(songId);
        songFunctional.getReportingLines().get(0).setReportingType("职能");
        organizations.save(songId, songFunctional);

        SourceDocumentForm document = new SourceDocumentForm();
        document.setDocumentType("劳动合同");
        document.setDocumentNumber("LD-SONG-1");
        document.setIssuerCompanyId(companyId);
        document.setStatus("有效");
        long documentId = sourceDocuments.save(document);
        EmployeeOrgForm withDocument = organizations.form(songId);
        withDocument.getEmployments().get(0).setEmploymentDocumentId(documentId);
        organizations.save(songId, withDocument);
        assertTrue(messageOf(() -> sourceDocuments.delete(documentId)).contains("仍被业务记录引用"));

        mvc.perform(get("/employees/" + songId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GH-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("LD-SONG-1")));
        mvc.perform(get("/source-documents"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("LD-SONG-1")));

        CompanyForm badDocument = company("LS-DOC", "错误材料", "company");
        badDocument.setLegalPersonId(legalPersons.save(legalPerson("材料法人")));
        badDocument.setCompanyForm("有限责任公司");
        badDocument.getRegistrations().add(registration(1, "91370100MA3C99999Z", LocalDate.now().minusDays(3)));
        badDocument.getRegistrations().get(0).setSourceDocumentId(999999L);
        assertTrue(messageOf(() -> companies.save(badDocument)).contains("来源材料不存在"));
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

    @Test
    void supplierRulesContactsQualificationsAndVersions() throws Exception {
        long buyer = companyForOrg("WP04-BUY", "采购甲");
        long otherBuyer = companyForOrg("WP04-BUY2", "采购乙");
        long thirdBuyer = companyForOrg("WP04-BUY3", "采购丙");
        long vendorLegalId = legalPersons.save(legalPerson("供货甲法人"));
        CompanyForm vendorCompany = company("WP04-VEN", "供货甲公司", "company");
        vendorCompany.setLegalPersonId(vendorLegalId);
        vendorCompany.setCompanyForm("有限责任公司");
        long vendorCompanyId = companies.save(vendorCompany);
        long outsiderLegalId = legalPersons.save(legalPerson("外部供货法人"));
        long personA = person("WP04吴用");
        long personB = person("WP04公孙胜");
        long personC = person("WP04阮小二");
        long ownerId = employee("林冲", "WP04-E1");

        SupplierForm missingObject = supplier("SP-NONE", "缺对象", buyer);
        assertTrue(messageOf(() -> supplierService.save(missingObject)).contains("恰好选择一个"));
        SupplierForm twoObjects = supplier("SP-TWO", "两个对象", buyer);
        twoObjects.setCompanyId(vendorCompanyId);
        twoObjects.setPersonId(personA);
        assertTrue(messageOf(() -> supplierService.save(twoObjects)).contains("恰好选择一个"));

        SupplierForm first = supplier("SP-A", "甲供档案", buyer);
        first.setPersonId(personA);
        long firstId = supplierService.save(first);
        List<SupplierVersion> created = supplierVersions.findBySupplierIdOrderByVersionNumberAsc(firstId);
        assertEquals(1, created.size());
        assertEquals(1, created.get(0).versionNumber.intValue());
        assertEquals("待提交", created.get(0).admissionStatus);
        assertEquals(LocalDate.now(), created.get(0).validFrom);
        assertNull(created.get(0).validTo);

        SupplierForm duplicateCode = supplier("SP-A", "重复编号", buyer);
        duplicateCode.setPersonId(personB);
        assertTrue(messageOf(() -> supplierService.save(duplicateCode)).contains("编号"));
        SupplierForm sameCodeOtherBuyer = supplier("SP-A", "乙公司同号", otherBuyer);
        sameCodeOtherBuyer.setPersonId(personA);
        supplierService.save(sameCodeOtherBuyer);
        SupplierForm duplicateObject = supplier("SP-B", "重复对象", buyer);
        duplicateObject.setPersonId(personA);
        assertTrue(messageOf(() -> supplierService.save(duplicateObject)).contains("一份档案"));

        SupplierForm companySupplier = supplier("SP-C", "公司供货", buyer);
        companySupplier.setCompanyId(vendorCompanyId);
        long companySupplierId = supplierService.save(companySupplier);
        SupplierForm legalDuplicate = supplier("SP-D", "法人重复", buyer);
        legalDuplicate.setLegalPersonId(vendorLegalId);
        assertTrue(messageOf(() -> supplierService.save(legalDuplicate)).contains("对应法人"));
        SupplierForm otherLegal = supplier("SP-E", "乙公司法人供货", otherBuyer);
        otherLegal.setLegalPersonId(outsiderLegalId);
        supplierService.save(otherLegal);

        SupplierForm admitted = supplierService.form(firstId);
        admitted.setAdmissionStatus("通过");
        admitted.setAdmissionSubmittedAt(LocalDateTime.now());
        assertTrue(messageOf(() -> supplierService.save(admitted)).contains("采购负责人"));
        admitted.setProcurementOwnerEmployeeId(ownerId);
        admitted.setAdmissionReviewedAt(LocalDateTime.now());
        assertTrue(messageOf(() -> supplierService.save(admitted)).contains("审核人"));
        admitted.setAdmissionReviewedByEmployeeId(ownerId);
        supplierService.save(admitted);
        assertEquals(1, supplierVersions.findBySupplierIdOrderByVersionNumberAsc(firstId).size());
        assertEquals("通过", supplierRepository.findById(firstId).orElseThrow().admissionStatus);

        SupplierForm rejected = supplierService.form(companySupplierId);
        rejected.setAdmissionStatus("驳回");
        rejected.setAdmissionSubmittedAt(LocalDateTime.now());
        rejected.setAdmissionReviewedByEmployeeId(ownerId);
        rejected.setAdmissionReviewedAt(LocalDateTime.now());
        assertTrue(messageOf(() -> supplierService.save(rejected)).contains("驳回"));
        rejected.setAdmissionRejectionReason("资料不全");
        supplierService.save(rejected);

        SupplierForm invoice = supplierService.form(firstId);
        invoice.setDefaultPaymentDueBasis("发票确认日");
        assertTrue(messageOf(() -> supplierService.save(invoice)).contains("账期"));
        invoice.setDefaultPaymentTermDays(30);
        supplierService.save(invoice);
        SupplierForm fixedDay = supplierService.form(firstId);
        fixedDay.setDefaultPaymentDueBasis("约定固定日");
        fixedDay.setDefaultPaymentTermDays(null);
        fixedDay.setDefaultPaymentDayOfMonth(0);
        assertTrue(messageOf(() -> supplierService.save(fixedDay)).contains("1 到 31"));
        fixedDay.setDefaultPaymentDayOfMonth(15);
        supplierService.save(fixedDay);
        SupplierForm contractual = supplierService.form(firstId);
        contractual.setDefaultPaymentDueBasis("合同另定");
        contractual.setDefaultPaymentDayOfMonth(null);
        contractual.setDefaultPaymentTermDays(10);
        assertTrue(messageOf(() -> supplierService.save(contractual)).contains("留空"));

        SupplierForm cooperating = supplier("SP-F", "直接合作", buyer);
        cooperating.setPersonId(personC);
        cooperating.setCooperationStatus("合作中");
        cooperating.setCooperationStartedOn(LocalDate.of(2024, 6, 1));
        long cooperatingId = supplierService.save(cooperating);
        assertNull(supplierRepository.findById(cooperatingId).orElseThrow().cooperationStatusReason);

        SupplierDetailForm details = new SupplierDetailForm();
        ContactForm bare = contact(personA, "商务", null, null, true);
        details.getContacts().add(bare);
        assertTrue(messageOf(() -> supplierService.saveDetails(firstId, details)).contains("电话或邮箱"));
        bare.setBusinessPhone("13800001111");
        ContactForm secondPrimary = contact(personB, "商务", "13900002222", null, true);
        details.getContacts().add(secondPrimary);
        assertTrue(messageOf(() -> supplierService.saveDetails(firstId, details)).contains("主联系人"));
        secondPrimary.setContactRole("财务");
        secondPrimary.setBusinessEmail("not-an-email");
        assertTrue(messageOf(() -> supplierService.saveDetails(firstId, details)).contains("邮箱"));
        secondPrimary.setBusinessEmail("finance@example.com");
        supplierService.saveDetails(firstId, details);

        SourceDocumentForm document = new SourceDocumentForm();
        document.setDocumentType("资质证书");
        document.setDocumentNumber("ZZ-WP04");
        document.setStatus("有效");
        long documentId = sourceDocuments.save(document);
        SupplierDetailForm qualificationForm = supplierService.detailForm(firstId);
        QualificationForm missingDocument = qualification("道路运输");
        qualificationForm.getQualifications().add(missingDocument);
        assertTrue(messageOf(() -> supplierService.saveDetails(firstId, qualificationForm)).contains("资质材料"));
        missingDocument.setQualificationDocumentId(documentId);
        missingDocument.setNoExpiry(true);
        missingDocument.setExpiresOn(LocalDate.now().plusYears(1));
        assertTrue(messageOf(() -> supplierService.saveDetails(firstId, qualificationForm)).contains("到期日必须留空"));
        missingDocument.setNoExpiry(false);
        missingDocument.setIssuedOn(LocalDate.of(2024, 1, 1));
        missingDocument.setExpiresOn(LocalDate.of(2023, 1, 1));
        assertTrue(messageOf(() -> supplierService.saveDetails(firstId, qualificationForm)).contains("早于"));
        missingDocument.setExpiresOn(null);
        supplierService.saveDetails(firstId, qualificationForm);
        assertTrue(messageOf(() -> sourceDocuments.delete(documentId)).contains("仍被业务记录引用"));

        SupplierVersion open = supplierVersions.findBySupplierIdOrderByVersionNumberAsc(firstId).stream()
                .filter(version -> version.validTo == null)
                .findFirst()
                .orElseThrow();
        open.validFrom = LocalDate.now().minusDays(1);
        supplierVersions.save(open);
        SupplierForm started = supplierService.form(firstId);
        started.setCooperationStatus("合作中");
        started.setCooperationStartedOn(LocalDate.of(2024, 1, 1));
        started.setChangeReason("开始供货");
        supplierService.save(started);
        List<SupplierVersion> history = supplierVersions.findBySupplierIdOrderByVersionNumberAsc(firstId);
        assertEquals(2, history.size());
        assertEquals(LocalDate.now(), history.get(0).validTo);
        assertEquals("合作中", history.get(1).cooperationStatus);
        assertEquals("合作中", supplierRepository.findById(firstId).orElseThrow().cooperationStatus);
        assertEquals(LocalDate.now(), history.get(1).validFrom);

        SupplierForm paused = supplierService.form(firstId);
        paused.setCooperationStatus("暂停");
        assertTrue(messageOf(() -> supplierService.save(paused)).contains("原因"));

        SupplierForm reassigned = supplierService.form(firstId);
        reassigned.setPersonId(personB);
        supplierService.save(reassigned);
        SupplierUsageProbe.REFERENCED.add(firstId);
        try {
            SupplierForm locked = supplierService.form(firstId);
            locked.setPurchasingCompanyId(thirdBuyer);
            assertTrue(messageOf(() -> supplierService.save(locked)).contains("不能更换"));
            assertTrue(messageOf(() -> supplierService.delete(firstId)).contains("不能删除"));
        } finally {
            SupplierUsageProbe.REFERENCED.clear();
        }

        mvc.perform(get("/suppliers"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("甲供档案")));
        mvc.perform(get("/suppliers/" + firstId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("WP04吴用")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("道路运输")));
        mvc.perform(get("/suppliers/" + firstId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("甲供档案")));
    }

    private String messageOf(Executable executable) {
        return assertThrows(FieldErrorsException.class, executable).getMessage();
    }

    private long companyForOrg(String code, String name) {
        long legalId = legalPersons.save(legalPerson(name + "法人"));
        CompanyForm form = company(code, name, "company");
        form.setLegalPersonId(legalId);
        form.setCompanyForm("有限责任公司");
        return companies.save(form);
    }

    private long department(long companyId, String code, String name) {
        DepartmentForm form = new DepartmentForm();
        form.setCompanyId(companyId);
        form.setDepartmentCode(code);
        form.setName(name);
        form.setDepartmentType("部门");
        form.setValidFrom(LocalDate.of(2019, 1, 1));
        form.setStatus("有效");
        return departments.save(form);
    }

    private long employee(String name, String code) {
        PersonForm person = new PersonForm();
        person.setName(name);
        person.setStatus("有效");
        long personId = persons.save(person);
        EmployeeForm employee = new EmployeeForm();
        employee.setPersonId(personId);
        employee.setEmployeeCode(code);
        employee.setStatus("有效");
        return employees.save(employee);
    }

    private EmploymentForm employment(String number, Long companyId, Long legalPersonId) {
        EmploymentForm row = new EmploymentForm();
        row.setRowKey("e-" + number + "-" + (companyId == null ? legalPersonId : companyId));
        row.setEmployerCompanyId(companyId);
        row.setEmployerLegalPersonId(legalPersonId);
        row.setEmployeeNumber(number);
        row.setEmploymentType("全职");
        row.setEmploymentStartDate(LocalDate.of(2020, 1, 1));
        row.setStatus("在职");
        return row;
    }

    private AssignmentForm assignment(String employmentKey, long departmentId, LocalDate from, LocalDate to) {
        AssignmentForm row = new AssignmentForm();
        row.setRowKey("a-" + employmentKey + "-" + from);
        row.setEmploymentKey(employmentKey);
        row.setDepartmentId(departmentId);
        row.setAssignmentType("主岗");
        row.setValidFrom(from);
        row.setValidTo(to);
        row.setStatus("有效");
        return row;
    }

    private ReportingForm reporting(String assignmentKey, String managerKey, String type) {
        ReportingForm row = new ReportingForm();
        row.setAssignmentKey(assignmentKey);
        row.setManagerAssignmentKey(managerKey);
        row.setReportingType(type);
        row.setValidFrom(LocalDate.of(2020, 1, 1));
        return row;
    }

    private Map<String, Object> column(String table, String name) {
        return jdbc.queryForList("pragma table_info('" + table + "')").stream()
                .filter(row -> name.equalsIgnoreCase(String.valueOf(row.get("name"))))
                .findFirst()
                .orElseThrow();
    }

    private long person(String name) {
        PersonForm form = new PersonForm();
        form.setName(name);
        form.setStatus("有效");
        return persons.save(form);
    }

    private SupplierForm supplier(String code, String name, long purchasingCompanyId) {
        SupplierForm form = new SupplierForm();
        form.setSupplierCode(code);
        form.setSupplierName(name);
        form.setPurchasingCompanyId(purchasingCompanyId);
        form.setAdmissionStatus("待提交");
        form.setCooperationStatus("未启用");
        form.setCooperationStatusEffectiveAt(LocalDateTime.now());
        return form;
    }

    private ContactForm contact(long personId, String role, String phone, String email, boolean primary) {
        ContactForm row = new ContactForm();
        row.setPersonId(personId);
        row.setContactRole(role);
        row.setBusinessPhone(phone);
        row.setBusinessEmail(email);
        row.setPrimary(primary);
        row.setValidFrom(LocalDate.of(2024, 1, 1));
        row.setStatus("有效");
        return row;
    }

    private QualificationForm qualification(String name) {
        QualificationForm row = new QualificationForm();
        row.setQualificationName(name);
        row.setNoExpiry(false);
        row.setReviewStatus("待审核");
        row.setStatus("有效");
        return row;
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
