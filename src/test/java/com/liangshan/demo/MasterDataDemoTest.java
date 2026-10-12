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
import com.liangshan.demo.domain.LaborCostRate;
import com.liangshan.demo.domain.WorkLog;
import com.liangshan.demo.domain.AcceptanceEvent;
import com.liangshan.demo.domain.AcceptanceLine;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractLine;
import com.liangshan.demo.domain.ContractLineTermVersion;
import com.liangshan.demo.domain.ContractStageDeliverable;
import com.liangshan.demo.domain.ContractStageTermVersion;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.ProjectBudgetVersion;
import com.liangshan.demo.domain.ProjectVersion;
import com.liangshan.demo.repository.LaborCostRateRepository;
import com.liangshan.demo.repository.WorkLogRepository;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.AcceptanceLineRepository;
import com.liangshan.demo.repository.ContractLineRepository;
import com.liangshan.demo.repository.DeliveryLineRepository;
import com.liangshan.demo.repository.ContractLineTermVersionRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageDeliverableRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractStageTermVersionRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.ProjectBudgetVersionRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ProjectVersionRepository;
import com.liangshan.demo.service.LaborCostRateService;
import com.liangshan.demo.service.WorkLogService;
import com.liangshan.demo.service.ContractService;
import com.liangshan.demo.service.FulfillmentService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.service.ProjectService;
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
import com.liangshan.demo.web.form.LaborCostRateForm;
import com.liangshan.demo.web.form.WorkLogDecisionForm;
import com.liangshan.demo.web.form.WorkLogForm;
import com.liangshan.demo.web.form.AcceptanceEventForm;
import com.liangshan.demo.web.form.ContractAmendmentForm;
import com.liangshan.demo.web.form.ContractPlanForm;
import com.liangshan.demo.web.form.DeliveryEventForm;
import com.liangshan.demo.web.form.ContractDetailForm;
import com.liangshan.demo.web.form.ContractDetailForm.DeliverableForm;
import com.liangshan.demo.web.form.ContractDetailForm.LineForm;
import com.liangshan.demo.web.form.ContractDetailForm.StageForm;
import com.liangshan.demo.web.form.ContractForm;
import com.liangshan.demo.web.form.ProjectDetailForm;
import com.liangshan.demo.web.form.ProjectDetailForm.BudgetForm;
import com.liangshan.demo.web.form.ProjectDetailForm.DeliveryForm;
import com.liangshan.demo.web.form.ProjectDetailForm.MemberForm;
import com.liangshan.demo.web.form.ProjectDetailForm.SupportForm;
import com.liangshan.demo.web.form.ProjectForm;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectVersionRepository projectVersions;
    @Autowired
    private ProjectBudgetVersionRepository projectBudgets;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ContractTermVersionRepository contractTerms;
    @Autowired
    private ContractLineRepository contractLines;
    @Autowired
    private ContractLineTermVersionRepository contractLineTerms;
    @Autowired
    private ContractStageRepository contractStages;
    @Autowired
    private ContractStageTermVersionRepository contractStageTerms;
    @Autowired
    private ContractStageDeliverableRepository contractDeliverables;
    @Autowired
    private FulfillmentService fulfillment;
    @Autowired
    private DeliveryLineRepository deliveryLines;
    @Autowired
    private AcceptanceEventRepository acceptanceEvents;
    @Autowired
    private AcceptanceLineRepository acceptanceLines;
    @Autowired
    private WorkLogService workLogs;
    @Autowired
    private WorkLogRepository workLogRepository;
    @Autowired
    private LaborCostRateService laborCostRates;
    @Autowired
    private LaborCostRateRepository laborCostRateRepository;

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
                "supplier_version",
                "project",
                "project_member",
                "project_budget_version",
                "project_version",
                "project_delivery_company",
                "project_support_department",
                "contract",
                "contract_term_version",
                "contract_line",
                "contract_line_term_version",
                "contract_amendment",
                "contract_stage",
                "contract_stage_term_version",
                "contract_stage_deliverable",
                "project_contract",
                "contract_additional_signatory",
                "contract_billing_schedule",
                "contract_payment_schedule",
                "delivery_event",
                "delivery_line",
                "acceptance_event",
                "acceptance_line",
                "work_log",
                "labor_cost_rate")));
        assertEquals("integer", String.valueOf(column("contract_term_version", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals("integer", String.valueOf(column("contract_stage_term_version", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals(1, ((Number) column("contract", "contract_number").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "buyer_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "buyer_legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "buyer_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "seller_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "seller_legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "seller_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "supplier_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "contract_net_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "contract_tax_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract", "contract_gross_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_line_term_version", "line_net_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_line_term_version", "line_tax_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_line_term_version", "line_gross_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_additional_signatory", "signatory_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_additional_signatory", "signatory_legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_additional_signatory", "signatory_person_id").get("notnull")).intValue());
        assertEquals("integer", String.valueOf(column("project_version", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals(1, ((Number) column("project", "owner_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("project", "customer_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("project", "customer_legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("project", "customer_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("project_budget_version", "labor_cost_budget_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("project_budget_version", "nonlabor_cost_budget_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("project_member", "department_assignment_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("project_member", "employment_id").get("notnull")).intValue());
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
        assertEquals("integer", String.valueOf(column("contract_billing_schedule", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals("integer", String.valueOf(column("delivery_event", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals("integer", String.valueOf(column("acceptance_event", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals(0, ((Number) column("contract_billing_schedule", "contract_stage_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_billing_schedule", "supersedes_billing_schedule_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("contract_billing_schedule", "planned_invoice_gross_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_payment_schedule", "contract_stage_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("contract_payment_schedule", "supersedes_payment_schedule_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("contract_payment_schedule", "planned_payment_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("delivery_event", "submitter_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("delivery_event", "submitter_legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("delivery_event", "submitter_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("delivery_event", "resubmission_of_delivery_event_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("delivery_line", "submitted_quantity").get("notnull")).intValue());
        assertEquals(0, ((Number) column("acceptance_event", "acceptor_company_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("acceptance_event", "acceptor_legal_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("acceptance_event", "acceptor_person_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("acceptance_event", "supersedes_acceptance_event_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("acceptance_line", "reviewed_quantity").get("notnull")).intValue());
        assertEquals(1, ((Number) column("acceptance_line", "accepted_quantity").get("notnull")).intValue());
        assertEquals(1, ((Number) column("acceptance_line", "rejected_quantity").get("notnull")).intValue());
        assertEquals("integer", String.valueOf(column("work_log", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals("integer", String.valueOf(column("labor_cost_rate", "id").get("type")).toLowerCase(Locale.ROOT));
        assertEquals(1, ((Number) column("work_log", "employee_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("work_log", "employment_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("work_log", "project_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("work_log", "hours").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "department_assignment_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "contract_stage_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "approved_by_employee_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "supersedes_work_log_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "labor_cost_rate_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "hourly_cost_amount_snapshot").get("notnull")).intValue());
        assertEquals(0, ((Number) column("work_log", "labor_cost_amount").get("notnull")).intValue());
        assertEquals(1, ((Number) column("labor_cost_rate", "employment_id").get("notnull")).intValue());
        assertEquals(1, ((Number) column("labor_cost_rate", "hourly_cost_amount").get("notnull")).intValue());
        assertEquals(0, ((Number) column("labor_cost_rate", "basis_document_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("labor_cost_rate", "approved_by_employee_id").get("notnull")).intValue());
        assertEquals(0, ((Number) column("labor_cost_rate", "approved_at").get("notnull")).intValue());
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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("采购")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("项目管理")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("条款、品项与阶段")));
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

    @Test
    void projectMembersBudgetsAndVersions() throws Exception {
        assertNull(ProjectService.budgetTotal(null, new BigDecimal("100.00")));
        assertNull(ProjectService.budgetTotal(new BigDecimal("10.00"), null));
        assertEquals(0, new BigDecimal("5.00").compareTo(ProjectService.budgetTotal(BigDecimal.ZERO, new BigDecimal("5.00"))));
        assertEquals(0, new BigDecimal("30.00").compareTo(ProjectService.budgetTotal(new BigDecimal("10.00"), new BigDecimal("20.00"))));

        long ownerId = companyForOrg("WP05-OWN", "项目主公司");
        long otherCompanyId = companyForOrg("WP05-OTH", "项目另一公司");
        long departmentId = department(ownerId, "WP05-D", "项目部");
        long otherDepartmentId = department(otherCompanyId, "WP05-X", "外部门");
        long customerId = person("WP05客户");
        long memberId = employee("武松", "WP05-E1");
        long otherMemberId = employee("鲁智深", "WP05-E2");
        long employmentId = employ(memberId, ownerId, departmentId, "WP05-GH1");
        long otherEmploymentId = employ(otherMemberId, ownerId, departmentId, "WP05-GH2");
        long assignmentId = organizations.form(memberId).getAssignments().get(0).getId();

        ProjectForm twoCustomers = project("PJ-TWO", "两个客户", ownerId);
        twoCustomers.setCustomerCompanyId(otherCompanyId);
        twoCustomers.setCustomerPersonId(customerId);
        assertTrue(messageOf(() -> projectService.save(twoCustomers)).contains("最多选择一个"));

        ProjectForm wrongDepartment = project("PJ-DEPT", "错部门", ownerId);
        wrongDepartment.setLeadDepartmentId(otherDepartmentId);
        assertTrue(messageOf(() -> projectService.save(wrongDepartment)).contains("牵头部门"));

        ProjectForm first = project("PJ-A", "聚义厅", ownerId);
        first.setCustomerPersonId(customerId);
        first.setLeadDepartmentId(departmentId);
        first.setProjectManagerEmployeeId(memberId);
        long projectId = projectService.save(first);
        List<ProjectVersion> created = projectVersions.findByProjectIdOrderByVersionNumberAsc(projectId);
        assertEquals(1, created.size());
        assertEquals("聚义厅", created.get(0).name);
        assertEquals(LocalDate.now(), created.get(0).validFrom);
        assertNull(created.get(0).validTo);
        assertNull(projectRepository.findById(projectId).orElseThrow().approvedBudgetVersionId);

        ProjectForm duplicate = project("PJ-A", "重复编号", ownerId);
        assertTrue(messageOf(() -> projectService.save(duplicate)).contains("项目编号已存在"));

        ProjectDetailForm details = new ProjectDetailForm();
        MemberForm foreignEmployment = member(memberId, otherEmploymentId, null, LocalDate.of(2020, 6, 1));
        details.getMembers().add(foreignEmployment);
        assertTrue(messageOf(() -> projectService.saveDetails(projectId, details)).contains("不属于该员工"));
        foreignEmployment.setEmploymentId(employmentId);
        foreignEmployment.setValidFrom(LocalDate.of(2019, 1, 1));
        assertTrue(messageOf(() -> projectService.saveDetails(projectId, details)).contains("雇佣有效期"));
        foreignEmployment.setValidFrom(LocalDate.of(2020, 6, 1));
        foreignEmployment.setDepartmentAssignmentId(organizations.form(otherMemberId).getAssignments().get(0).getId());
        assertTrue(messageOf(() -> projectService.saveDetails(projectId, details)).contains("任职不属于"));
        foreignEmployment.setDepartmentAssignmentId(assignmentId);

        BudgetForm partial = budget(1, null, new BigDecimal("100.00"));
        details.getBudgets().add(partial);
        projectService.saveDetails(projectId, details);
        ProjectBudgetVersion storedPartial = projectBudgets.findByProjectIdOrderByVersionNumberAsc(projectId).get(0);
        assertNull(storedPartial.laborCostBudgetAmount);
        assertEquals(0, new BigDecimal("100.00").compareTo(storedPartial.nonlaborCostBudgetAmount));
        assertNull(projectRepository.findById(projectId).orElseThrow().approvedBudgetVersionId);

        ProjectDetailForm priced = projectService.detailForm(projectId);
        BudgetForm amounts = priced.getBudgets().get(0);
        amounts.setLaborCostBudgetAmount(new BigDecimal("10.00"));
        amounts.setNonlaborCostBudgetAmount(new BigDecimal("20.00"));
        amounts.setPlannedLaborHours(new BigDecimal("8.00"));
        amounts.setBudgetCostBasis("标准人工");
        amounts.setApprovalStatus("已批准");
        amounts.setSubmittedAt(LocalDateTime.now());
        assertTrue(messageOf(() -> projectService.saveDetails(projectId, priced)).contains("审批人"));
        amounts.setApprovedByEmployeeId(memberId);
        amounts.setApprovedAt(LocalDateTime.now());
        projectService.saveDetails(projectId, priced);
        ProjectBudgetVersion approved = projectBudgets.findByProjectIdOrderByVersionNumberAsc(projectId).get(0);
        assertEquals(0, new BigDecimal("30.00").compareTo(ProjectService.budgetTotal(
                approved.laborCostBudgetAmount, approved.nonlaborCostBudgetAmount)));
        assertEquals(approved.id, projectRepository.findById(projectId).orElseThrow().approvedBudgetVersionId);

        amounts.setLaborCostBudgetAmount(BigDecimal.ZERO);
        amounts.setNonlaborCostBudgetAmount(new BigDecimal("5.00"));
        projectService.saveDetails(projectId, priced);
        ProjectBudgetVersion zeroLabor = projectBudgets.findByProjectIdOrderByVersionNumberAsc(projectId).get(0);
        assertEquals(0, BigDecimal.ZERO.compareTo(zeroLabor.laborCostBudgetAmount));
        assertEquals(0, new BigDecimal("5.00").compareTo(ProjectService.budgetTotal(
                zeroLabor.laborCostBudgetAmount, zeroLabor.nonlaborCostBudgetAmount)));

        ProjectDetailForm staffing = projectService.detailForm(projectId);
        DeliveryForm wrongLead = new DeliveryForm();
        wrongLead.setDeliveryCompanyId(ownerId);
        wrongLead.setLeadDepartmentId(otherDepartmentId);
        wrongLead.setResponsibilityDescription("现场交付");
        wrongLead.setValidFrom(LocalDate.of(2020, 6, 1));
        staffing.getDeliveries().add(wrongLead);
        assertTrue(messageOf(() -> projectService.saveDetails(projectId, staffing)).contains("交付牵头部门"));
        wrongLead.setLeadDepartmentId(departmentId);
        SupportForm support = new SupportForm();
        support.setDepartmentId(otherDepartmentId);
        support.setResponsibilityDescription("跨公司支持");
        support.setValidFrom(LocalDate.of(2020, 6, 1));
        staffing.getSupports().add(support);
        projectService.saveDetails(projectId, staffing);

        SourceDocumentForm document = new SourceDocumentForm();
        document.setDocumentType("预算批复");
        document.setDocumentNumber("YS-WP05");
        document.setStatus("有效");
        long documentId = sourceDocuments.save(document);
        ProjectDetailForm withDocument = projectService.detailForm(projectId);
        withDocument.getBudgets().get(0).setBasisDocumentId(documentId);
        projectService.saveDetails(projectId, withDocument);
        assertTrue(messageOf(() -> sourceDocuments.delete(documentId)).contains("仍被业务记录引用"));

        ProjectForm statusOnly = projectService.form(projectId);
        statusOnly.setStatus("进行中");
        statusOnly.setActualStartAt(LocalDate.now());
        projectService.save(statusOnly);
        assertEquals(1, projectVersions.findByProjectIdOrderByVersionNumberAsc(projectId).size());

        ProjectVersion open = projectVersions.findByProjectIdOrderByVersionNumberAsc(projectId).stream()
                .filter(version -> version.validTo == null)
                .findFirst()
                .orElseThrow();
        open.validFrom = LocalDate.now().minusDays(1);
        projectVersions.save(open);
        ProjectForm renamed = projectService.form(projectId);
        renamed.setName("聚义厅扩建");
        projectService.save(renamed);
        List<ProjectVersion> history = projectVersions.findByProjectIdOrderByVersionNumberAsc(projectId);
        assertEquals(2, history.size());
        assertEquals(LocalDate.now(), history.get(0).validTo);
        assertEquals("聚义厅扩建", history.get(1).name);
        assertEquals("聚义厅扩建", projectRepository.findById(projectId).orElseThrow().name);

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("聚义厅扩建")));
        mvc.perform(get("/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("跨公司支持")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("当前批准预算")));
    }

    @Test
    void contractRulesAmendmentsAndSupplierLock() throws Exception {
        long buyer = companyForOrg("CT-BUYER", "合同买方公司");
        long otherCompany = companyForOrg("CT-OTHER", "合同对照公司");
        long sellerPerson = person("合同卖方");
        long otherPerson = person("错配卖方");

        ContractForm twoBuyers = contract("CT-TWO", "两个买方", "销售", buyer, sellerPerson);
        twoBuyers.setBuyerPersonId(otherPerson);
        assertTrue(messageOf(() -> contractService.save(twoBuyers)).contains("恰好"));

        ContractForm noParties = contract("CT-NONE", "没有主体", "销售", buyer, sellerPerson);
        noParties.setBuyerCompanyId(null);
        noParties.setSellerPersonId(null);
        assertTrue(messageOf(() -> contractService.save(noParties)).contains("恰好"));

        ContractForm day = contract("CT-DAY", "当日总价", "销售", buyer, sellerPerson);
        day.setContractGrossAmount(new BigDecimal("50.00"));
        long dayId = contractService.save(day);
        Contract storedDay = contractRepository.findById(dayId).orElseThrow();
        assertNull(storedDay.contractNetAmount);
        assertNull(storedDay.contractTaxAmount);
        assertEquals(0, new BigDecimal("50.00").compareTo(storedDay.contractGrossAmount));
        assertEquals(1, contractTerms.findByContractIdOrderByVersionNumberAsc(dayId).size());
        assertNull(contractTerms.findByContractIdOrderByVersionNumberAsc(dayId).get(0).amendmentId);

        ContractForm badMoney = contractService.form(dayId);
        badMoney.setContractNetAmount(new BigDecimal("10.00"));
        badMoney.setContractTaxAmount(new BigDecimal("1.00"));
        badMoney.setContractGrossAmount(new BigDecimal("12.00"));
        assertTrue(messageOf(() -> contractService.save(badMoney)).contains("含税价须等于不含税价加税额"));

        ContractForm sameDay = contractService.form(dayId);
        sameDay.setContractGrossAmount(new BigDecimal("60.00"));
        contractService.save(sameDay);
        List<ContractTermVersion> dayTerms = contractTerms.findByContractIdOrderByVersionNumberAsc(dayId);
        assertEquals(1, dayTerms.size());
        assertEquals(0, new BigDecimal("60.00").compareTo(dayTerms.get(0).contractGrossAmount));
        assertNull(dayTerms.get(0).contractNetAmount);

        SupplierForm archive = supplier("SP-CT", "寨外供货", buyer);
        archive.setPersonId(sellerPerson);
        long supplierId = supplierService.save(archive);
        ContractForm salesWithSupplier = contract("CT-SALE-SUP", "销售误填档案", "销售", buyer, sellerPerson);
        salesWithSupplier.setSupplierId(supplierId);
        assertTrue(messageOf(() -> contractService.save(salesWithSupplier)).contains("非采购合同"));

        ContractForm mismatch = contract("CT-BUY-BAD", "错配采购", "采购", buyer, otherPerson);
        mismatch.setProcurementCompanyId(buyer);
        mismatch.setSupplierId(supplierId);
        assertTrue(messageOf(() -> contractService.save(mismatch)).contains("供货对象"));

        ContractForm purchase = contract("CT-BUY", "寨外供货合同", "采购", buyer, sellerPerson);
        purchase.setProcurementCompanyId(buyer);
        purchase.setSupplierId(supplierId);
        long purchaseId = contractService.save(purchase);
        assertEquals(supplierId, contractRepository.findById(purchaseId).orElseThrow().supplierId);
        assertTrue(SupplierUsageProbe.REFERENCED.isEmpty());

        SupplierForm changeBuyer = supplierService.form(supplierId);
        changeBuyer.setPurchasingCompanyId(otherCompany);
        assertTrue(messageOf(() -> supplierService.save(changeBuyer)).contains("不能更换"));
        SupplierForm changeObject = supplierService.form(supplierId);
        changeObject.setPersonId(null);
        changeObject.setCompanyId(otherCompany);
        assertTrue(messageOf(() -> supplierService.save(changeObject)).contains("不能更换"));
        assertTrue(messageOf(() -> supplierService.delete(supplierId)).contains("不能删除"));

        ContractForm old = contract("CT-OLD", "寨内扩建施工合同", "销售", buyer, sellerPerson);
        old.setValidFrom(LocalDate.now().minusDays(1));
        old.setBuyerNameSnapshot("原买方");
        old.setContractGrossAmount(new BigDecimal("100.00"));
        long oldId = contractService.save(old);
        ContractForm crossDay = contractService.form(oldId);
        crossDay.setContractGrossAmount(new BigDecimal("110.00"));
        assertTrue(messageOf(() -> contractService.save(crossDay)).contains("跨日变更条款"));
        assertEquals(0, new BigDecimal("100.00").compareTo(
                contractTerms.findByContractIdOrderByVersionNumberAsc(oldId).get(0).contractGrossAmount));

        ContractDetailForm duplicateStages = new ContractDetailForm();
        duplicateStages.getStages().add(stage("S1", 1));
        duplicateStages.getStages().add(stage("S1", 2));
        assertTrue(messageOf(() -> contractService.saveDetails(oldId, duplicateStages)).contains("阶段编号"));

        ContractDetailForm details = new ContractDetailForm();
        LineForm line = new LineForm();
        line.setLineNumber(1);
        line.setItemName("寨墙");
        line.setUnit("项");
        line.setLineGrossAmount(new BigDecimal("80.00"));
        details.getLines().add(line);
        StageForm stageRow = stage("S1", 1);
        stageRow.setStageGrossAmount(new BigDecimal("80.00"));
        details.getStages().add(stageRow);
        DeliverableForm deliverable = new DeliverableForm();
        deliverable.setStageCode("S1");
        deliverable.setLineNumber(1);
        deliverable.setDeliverableCode("DL-1");
        deliverable.setDeliverableName("寨墙完工");
        deliverable.setUnit("项");
        deliverable.setPlannedQuantity(BigDecimal.ONE);
        deliverable.setAcceptanceCriteria("可通行");
        deliverable.setPlannedDeliveryDate(LocalDate.now().plusMonths(1));
        details.getDeliverables().add(deliverable);
        contractService.saveDetails(oldId, details);

        SourceDocumentForm amendmentDocument = new SourceDocumentForm();
        amendmentDocument.setDocumentType("合同变更");
        amendmentDocument.setDocumentNumber("BG-WP06");
        amendmentDocument.setStatus("有效");
        long amendmentDocumentId = sourceDocuments.save(amendmentDocument);
        ContractAmendmentForm amendment = new ContractAmendmentForm();
        amendment.setAmendmentNumber("BG-1");
        amendment.setSignedAt(LocalDate.now());
        amendment.setEffectiveAt(LocalDate.now());
        amendment.setSourceDocumentId(amendmentDocumentId);
        amendment.setSummary("上调含税总价并改买方名称");
        ContractForm amendedTerms = contractService.form(oldId);
        amendedTerms.setBuyerNameSnapshot("新买方");
        amendedTerms.setContractGrossAmount(new BigDecimal("180.00"));
        amendment.setTerms(amendedTerms);
        contractService.registerAmendment(oldId, amendment);

        List<ContractTermVersion> history = contractTerms.findByContractIdOrderByVersionNumberAsc(oldId);
        assertEquals(2, history.size());
        assertNull(history.get(0).amendmentId);
        assertNotNull(history.get(1).amendmentId);
        assertEquals("原买方", history.get(0).buyerNameSnapshot);
        assertEquals("新买方", history.get(1).buyerNameSnapshot);
        assertEquals(buyer, history.get(0).buyerCompanyId);
        assertEquals(sellerPerson, history.get(0).sellerPersonId);
        assertEquals(0, new BigDecimal("100.00").compareTo(history.get(0).contractGrossAmount));
        assertEquals(0, new BigDecimal("180.00").compareTo(history.get(1).contractGrossAmount));
        assertEquals(LocalDate.now(), history.get(0).validTo);
        assertEquals(LocalDate.now(), history.get(1).validFrom);
        assertNull(history.get(0).contractNetAmount);
        assertNull(history.get(0).contractTaxAmount);

        List<ContractLine> lines = contractLines.findByContractIdOrderByLineNumberAsc(oldId);
        assertEquals(1, lines.size());
        List<ContractLineTermVersion> lineHistory = contractLineTerms.findByContractLineId(lines.get(0).id);
        assertEquals(2, lineHistory.size());
        assertTrue(lineHistory.stream().anyMatch(row -> row.validTo != null
                && new BigDecimal("80.00").compareTo(row.lineGrossAmount) == 0));
        assertTrue(lineHistory.stream().anyMatch(row -> row.validTo == null
                && new BigDecimal("80.00").compareTo(row.lineGrossAmount) == 0));
        assertFalse(lineHistory.get(0).id.equals(lineHistory.get(1).id));

        var stages = contractStages.findByContractIdOrderBySequenceNoAsc(oldId);
        assertEquals(1, stages.size());
        List<ContractStageTermVersion> stageHistory = contractStageTerms.findByContractStageIdOrderByVersionNumberAsc(stages.get(0).id);
        assertEquals(2, stageHistory.size());
        assertNull(stageHistory.get(0).amendmentId);
        assertNotNull(stageHistory.get(1).amendmentId);
        List<ContractStageDeliverable> originalDeliverables =
                contractDeliverables.findByStageTermVersionIdOrderByIdAsc(stageHistory.get(0).id);
        List<ContractStageDeliverable> copiedDeliverables =
                contractDeliverables.findByStageTermVersionIdOrderByIdAsc(stageHistory.get(1).id);
        assertEquals("DL-1", originalDeliverables.get(0).deliverableCode);
        assertEquals("DL-1", copiedDeliverables.get(0).deliverableCode);
        assertFalse(originalDeliverables.get(0).id.equals(copiedDeliverables.get(0).id));

        long projectId = projectService.save(project("CT-PJ", "寨内工程", buyer));
        contractService.linkProject(projectId, oldId, "施工主合同");
        mvc.perform(get("/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("寨内扩建施工合同")));
        mvc.perform(get("/contracts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CT-OLD")));
        mvc.perform(get("/contracts/" + oldId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("新买方")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DL-1")));
    }

    @Test
    void billingPaymentAndAcceptanceCumulatives() throws Exception {
        long buyer = companyForOrg("FF-BUY", "履约买方公司");
        long seller = person("履约卖方");
        long contractId = contractService.save(contract("CT-FF", "寨墙交付合同", "销售", buyer, seller));
        ContractDetailForm details = new ContractDetailForm();
        LineForm line = new LineForm();
        line.setLineNumber(1);
        line.setItemName("寨墙");
        line.setUnit("项");
        details.getLines().add(line);
        details.getStages().add(stage("S1", 1));
        DeliverableForm deliverable = new DeliverableForm();
        deliverable.setStageCode("S1");
        deliverable.setLineNumber(1);
        deliverable.setDeliverableCode("DL-1");
        deliverable.setDeliverableName("寨墙完工");
        deliverable.setUnit("项");
        deliverable.setPlannedQuantity(new BigDecimal("10"));
        deliverable.setAcceptanceCriteria("可通行");
        deliverable.setPlannedDeliveryDate(LocalDate.now().plusMonths(1));
        details.getDeliverables().add(deliverable);
        contractService.saveDetails(contractId, details);

        long projectId = projectService.save(project("FF-PJ", "寨墙工程", buyer));
        contractService.linkProject(projectId, contractId, "施工");
        long termId = contractTerms.findByContractIdOrderByVersionNumberAsc(contractId).get(0).id;
        long stageId = contractStages.findByContractIdOrderBySequenceNoAsc(contractId).get(0).id;
        long deliverableId = contractDeliverables.findByStageTermVersionIdOrderByIdAsc(
                contractStageTerms.findByContractStageIdOrderByVersionNumberAsc(stageId).get(0).id).get(0).id;

        ContractPlanForm missingDate = new ContractPlanForm();
        missingDate.getBillings().add(billing(1, termId, null, "指定日期", "80.00"));
        assertTrue(messageOf(() -> fulfillment.savePlans(contractId, missingDate)).contains("计划日期"));

        ContractPlanForm missingStage = new ContractPlanForm();
        ContractPlanForm.BillingRow stageTrigger = billing(1, termId, null, "阶段验收", "80.00");
        stageTrigger.setDaysAfterTrigger(7);
        missingStage.getBillings().add(stageTrigger);
        assertTrue(messageOf(() -> fulfillment.savePlans(contractId, missingStage)).contains("阶段"));

        ContractPlanForm missingTerm = new ContractPlanForm();
        ContractPlanForm.BillingRow fakeTerm = billing(1, 9_999_999L, stageId, "阶段验收", "80.00");
        fakeTerm.setDaysAfterTrigger(7);
        missingTerm.getBillings().add(fakeTerm);
        assertTrue(messageOf(() -> fulfillment.savePlans(contractId, missingTerm)).contains("条款版本"));

        ContractPlanForm plans = new ContractPlanForm();
        ContractPlanForm.BillingRow invoice = billing(1, termId, stageId, "阶段验收", "80.00");
        invoice.setDaysAfterTrigger(15);
        plans.getBillings().add(invoice);
        ContractPlanForm.PaymentRow installment = payment(1, termId, stageId, "进度款", "阶段验收", "80.00");
        installment.setDaysAfterTrigger(30);
        plans.getPayments().add(installment);
        fulfillment.savePlans(contractId, plans);

        long deliveryId = fulfillment.recordDelivery(contractId, delivery(projectId, seller, "DV-1", deliverableId, "10"));
        long lineId = deliveryLines.findByDeliveryEventIdOrderByIdAsc(deliveryId).get(0).id;
        LocalDateTime firstAt = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        long firstAcceptanceId = fulfillment.recordAcceptance(contractId,
                acceptance(deliveryId, buyer, "AC-1", firstAt, lineId, "10", "7", "3", "缝隙未齐"));

        FulfillmentService.ProgressRow midway = progressOf(contractId);
        assertEquals(0, new BigDecimal("10").compareTo(midway.deliveredQuantity()));
        assertEquals(0, new BigDecimal("7").compareTo(midway.acceptedQuantity()));
        assertEquals(0, new BigDecimal("3").compareTo(midway.rejectedQuantity()));
        AcceptanceLine firstLine = acceptanceLines.findByAcceptanceEventIdOrderByIdAsc(firstAcceptanceId).get(0);
        assertEquals(0, new BigDecimal("7").compareTo(firstLine.acceptedQuantity));
        assertEquals("部分通过", acceptanceEvents.findById(firstAcceptanceId).orElseThrow().acceptanceResult);

        assertTrue(messageOf(() -> fulfillment.recordAcceptance(contractId,
                acceptance(deliveryId, buyer, "AC-OVER", firstAt.plusMinutes(1), lineId, "10", "10", "0", null)))
                .contains("不能超过已交付"));
        assertTrue(messageOf(() -> fulfillment.recordDelivery(contractId,
                resubmit(projectId, seller, deliveryId, "DV-2", deliverableId, "10")))
                .contains("重提"));

        long secondAcceptanceId = fulfillment.recordAcceptance(contractId,
                acceptance(deliveryId, buyer, "AC-2", firstAt.plusMinutes(2), lineId, "3", "3", "0", null));
        FulfillmentService.ProgressRow done = progressOf(contractId);
        assertEquals(0, new BigDecimal("10").compareTo(done.deliveredQuantity()));
        assertEquals(0, new BigDecimal("10").compareTo(done.acceptedQuantity()));
        assertEquals(0, new BigDecimal("0").compareTo(done.rejectedQuantity()));
        assertEquals(0, new BigDecimal("7").compareTo(
                acceptanceLines.findByAcceptanceEventIdOrderByIdAsc(firstAcceptanceId).get(0).acceptedQuantity));
        assertEquals(0, new BigDecimal("3").compareTo(
                acceptanceLines.findByAcceptanceEventIdOrderByIdAsc(secondAcceptanceId).get(0).acceptedQuantity));
        assertEquals("通过", acceptanceEvents.findById(secondAcceptanceId).orElseThrow().acceptanceResult);
        assertEquals(2, acceptanceEvents.findByDeliveryEventIdOrderByAcceptedAtAscIdAsc(deliveryId).size());
        assertTrue(messageOf(() -> fulfillment.recordAcceptance(contractId,
                acceptance(deliveryId, buyer, "AC-3", firstAt.plusMinutes(3), lineId, "1", "1", "0", null)))
                .contains("不能超过已交付"));
        assertTrue(messageOf(() -> contractService.delete(contractId)).contains("不能删除"));

        mvc.perform(get("/contracts/" + contractId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("履约进度")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("累计通过")));
        mvc.perform(get("/contracts/" + contractId + "/performance"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("累计交付")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("累计不通过")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DL-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AC-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("部分通过")));
        mvc.perform(get("/contracts/" + contractId + "/plans"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("开票计划")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("收付款计划")));
    }

    @Test
    void workLogApprovalAndRateSnapshot() throws Exception {
        long companyId = companyForOrg("WL-CO", "工时公司");
        long departmentId = department(companyId, "WL-FIN", "工时财务");
        long workerId = employee("时迁", "E-WL-1");
        long leaderId = employee("吴用", "E-WL-2");
        long outsiderId = employee("白胜", "E-WL-3");
        long employmentId = employ(workerId, companyId, departmentId, "GH-WL");
        long assignmentId = organizations.form(workerId).getAssignments().get(0).getId();
        long outsiderEmploymentId = employ(outsiderId, companyId, departmentId, "GH-OUT");
        long outsiderAssignmentId = organizations.form(outsiderId).getAssignments().get(0).getId();
        long projectId = projectService.save(project("WL-PJ", "工时工程", companyId));
        ProjectDetailForm staffing = new ProjectDetailForm();
        staffing.getMembers().add(member(workerId, employmentId, assignmentId, LocalDate.now().minusDays(10)));
        projectService.saveDetails(projectId, staffing);

        long rateId = laborCostRates.save(rate(employmentId, "200", 1, LocalDate.now().minusDays(30), leaderId));
        long logId = workLogs.save(hours(workerId, employmentId, assignmentId, projectId, LocalDate.now(), "8", "寨墙施工", true));

        assertTrue(messageOf(() -> workLogs.approve(logId, decision(workerId, null))).contains("自己"));
        workLogs.approve(logId, decision(leaderId, "同意"));

        WorkLog approved = workLogRepository.findById(logId).orElseThrow();
        assertEquals(0, new BigDecimal("1600.00").compareTo(approved.laborCostAmount));
        assertEquals(0, new BigDecimal("200").compareTo(approved.hourlyCostAmountSnapshot));
        assertEquals(rateId, approved.laborCostRateId);
        assertEquals("CNY", approved.laborCostCurrency);
        assertEquals("通过", approved.approvalStatus);
        assertEquals("有效", approved.recordStatus);
        assertEquals(leaderId, approved.approvedByEmployeeId);
        assertNotNull(approved.approvedAt);

        mvc.perform(get("/work-logs"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1600")));
        mvc.perform(get("/work-logs/" + logId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1600")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("成本快照")));
        mvc.perform(get("/labor-cost-rates"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("200")));
        mvc.perform(get("/work-logs/new")).andExpect(status().isOk());
        mvc.perform(get("/labor-cost-rates/new")).andExpect(status().isOk());

        LaborCostRate changed = laborCostRateRepository.findById(rateId).orElseThrow();
        changed.hourlyCostAmount = new BigDecimal("300");
        laborCostRateRepository.save(changed);
        WorkLog afterPriceEdit = workLogRepository.findById(logId).orElseThrow();
        assertEquals(0, new BigDecimal("1600.00").compareTo(afterPriceEdit.laborCostAmount));
        assertEquals(0, new BigDecimal("200").compareTo(afterPriceEdit.hourlyCostAmountSnapshot));

        laborCostRates.save(rate(employmentId, "300", 2, LocalDate.now().plusDays(1), leaderId));
        WorkLog afterNewVersion = workLogRepository.findById(logId).orElseThrow();
        assertEquals(0, new BigDecimal("1600.00").compareTo(afterNewVersion.laborCostAmount));
        assertEquals(0, new BigDecimal("200").compareTo(afterNewVersion.hourlyCostAmountSnapshot));
        assertEquals(rateId, afterNewVersion.laborCostRateId);

        assertTrue(messageOf(() -> workLogs.save(hours(
                outsiderId, outsiderEmploymentId, outsiderAssignmentId, projectId, LocalDate.now(), "4", "编外", true)))
                .contains("不是该项目成员"));
        assertTrue(messageOf(() -> workLogs.save(hours(
                workerId, employmentId, assignmentId, projectId, LocalDate.now().minusDays(30), "1", "过早", true)))
                .contains("有效期"));

        long rejectedId = workLogs.save(hours(
                workerId, employmentId, assignmentId, projectId, LocalDate.now(), "1", "待驳回", true));
        assertTrue(messageOf(() -> workLogs.reject(rejectedId, decision(leaderId, null))).contains("驳回"));
        workLogs.reject(rejectedId, decision(leaderId, "工时不实"));
        WorkLog rejected = workLogRepository.findById(rejectedId).orElseThrow();
        assertEquals("驳回", rejected.approvalStatus);
        assertNull(rejected.laborCostAmount);
        assertNull(rejected.hourlyCostAmountSnapshot);
        assertEquals(leaderId, rejected.approvedByEmployeeId);
        assertNotNull(rejected.approvedAt);
        assertEquals("工时不实", rejected.approvalComment);
        mvc.perform(get("/work-logs/approvals")).andExpect(status().isOk());
    }

    private LaborCostRateForm rate(long employmentId, String amount, int version, LocalDate from, long approverId) {
        LaborCostRateForm form = new LaborCostRateForm();
        form.setEmploymentId(employmentId);
        form.setCostBasis("标准成本");
        form.setHourlyCostAmount(new BigDecimal(amount));
        form.setCurrency("CNY");
        form.setRateVersionNumber(version);
        form.setValidFrom(from);
        form.setApprovalStatus("已批准");
        form.setApprovedByEmployeeId(approverId);
        form.setApprovedAt(LocalDateTime.now().minusDays(1));
        return form;
    }

    private WorkLogForm hours(
            long employeeId,
            long employmentId,
            long assignmentId,
            long projectId,
            LocalDate workDate,
            String amount,
            String description,
            boolean submit) {
        WorkLogForm form = new WorkLogForm();
        form.setEmployeeId(employeeId);
        form.setEmploymentId(employmentId);
        form.setDepartmentAssignmentId(assignmentId);
        form.setProjectId(projectId);
        form.setWorkDate(workDate);
        form.setHours(new BigDecimal(amount));
        form.setWorkDescription(description);
        form.setBillable(true);
        form.setSubmitForApproval(submit);
        return form;
    }

    private WorkLogDecisionForm decision(long approverId, String comment) {
        WorkLogDecisionForm form = new WorkLogDecisionForm();
        form.setApprovedByEmployeeId(approverId);
        form.setApprovedAt(LocalDateTime.now());
        form.setApprovalComment(comment);
        return form;
    }

    private FulfillmentService.ProgressRow progressOf(long contractId) {
        return fulfillment.progress(contractId).stream()
                .filter(row -> "DL-1".equals(row.deliverableCode()))
                .findFirst()
                .orElseThrow();
    }

    private ContractPlanForm.BillingRow billing(int number, Long termId, Long stageId, String trigger, String amount) {
        ContractPlanForm.BillingRow row = new ContractPlanForm.BillingRow();
        row.setScheduleNumber(number);
        row.setContractTermVersionId(termId);
        row.setContractStageId(stageId);
        row.setBillingTrigger(trigger);
        row.setPlannedInvoiceGrossAmount(new BigDecimal(amount));
        row.setCurrency("CNY");
        row.setValidFrom(LocalDate.now());
        return row;
    }

    private ContractPlanForm.PaymentRow payment(
            int number, Long termId, Long stageId, String purpose, String trigger, String amount) {
        ContractPlanForm.PaymentRow row = new ContractPlanForm.PaymentRow();
        row.setScheduleNumber(number);
        row.setContractTermVersionId(termId);
        row.setContractStageId(stageId);
        row.setInstallmentName("第" + number + "期");
        row.setPaymentPurpose(purpose);
        row.setPaymentTrigger(trigger);
        row.setPlannedPaymentAmount(new BigDecimal(amount));
        row.setCurrency("CNY");
        row.setValidFrom(LocalDate.now());
        return row;
    }

    private DeliveryEventForm delivery(long projectId, long personId, String number, long deliverableId, String quantity) {
        DeliveryEventForm form = new DeliveryEventForm();
        form.setProjectId(projectId);
        form.setSubmitterPersonId(personId);
        form.setDeliveryNumber(number);
        form.setSubmittedAt(LocalDateTime.now());
        form.setDeliverableDescription("寨墙完工提交");
        DeliveryEventForm.Line line = new DeliveryEventForm.Line();
        line.setStageDeliverableId(deliverableId);
        line.setSubmittedQuantity(new BigDecimal(quantity));
        form.getLines().add(line);
        return form;
    }

    private DeliveryEventForm resubmit(
            long projectId, long personId, long ancestorId, String number, long deliverableId, String quantity) {
        DeliveryEventForm form = delivery(projectId, personId, number, deliverableId, quantity);
        form.setResubmissionOfDeliveryEventId(ancestorId);
        return form;
    }

    private AcceptanceEventForm acceptance(
            long deliveryId,
            long companyId,
            String number,
            LocalDateTime acceptedAt,
            long lineId,
            String reviewed,
            String accepted,
            String rejected,
            String reason) {
        AcceptanceEventForm form = new AcceptanceEventForm();
        form.setDeliveryEventId(deliveryId);
        form.setAcceptorCompanyId(companyId);
        form.setAcceptanceNumber(number);
        form.setAcceptedAt(acceptedAt);
        AcceptanceEventForm.Line line = new AcceptanceEventForm.Line();
        line.setDeliveryLineId(lineId);
        line.setReviewedQuantity(new BigDecimal(reviewed));
        line.setAcceptedQuantity(new BigDecimal(accepted));
        line.setRejectedQuantity(new BigDecimal(rejected));
        line.setRejectionReason(reason);
        form.getLines().add(line);
        return form;
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

    private long employ(long employeeId, long companyId, long departmentId, String number) {
        EmployeeOrgForm form = new EmployeeOrgForm();
        form.setEmployeeId(employeeId);
        EmploymentForm job = employment(number, companyId, null);
        form.getEmployments().add(job);
        form.getAssignments().add(assignment(job.getRowKey(), departmentId, LocalDate.of(2020, 1, 1), null));
        organizations.save(employeeId, form);
        return organizations.form(employeeId).getEmployments().get(0).getId();
    }

    private ContractForm contract(String number, String title, String type, long buyerCompanyId, long sellerPersonId) {
        ContractForm form = new ContractForm();
        form.setContractNumber(number);
        form.setContractTitle(title);
        form.setContractType(type);
        form.setPricingMethod("固定总价");
        form.setCurrency("CNY");
        form.setBuyerCompanyId(buyerCompanyId);
        form.setSellerPersonId(sellerPersonId);
        form.setBuyerNameSnapshot("买方");
        form.setSellerNameSnapshot("卖方");
        form.setValidFrom(LocalDate.now());
        form.setApprovalStatus("草稿");
        form.setStatus("未生效");
        return form;
    }

    private StageForm stage(String code, int sequence) {
        StageForm row = new StageForm();
        row.setStageCode(code);
        row.setSequenceNo(sequence);
        row.setStageName(code + "阶段");
        row.setStatus("未开始");
        row.setDeliverableDescription("按图施工");
        row.setAcceptanceCriteria("验收通过");
        row.setPlannedStartAt(LocalDate.now());
        row.setPlannedDueAt(LocalDate.now().plusMonths(1));
        return row;
    }

    private ProjectForm project(String code, String name, long ownerCompanyId) {
        ProjectForm form = new ProjectForm();
        form.setProjectCode(code);
        form.setName(name);
        form.setProjectType("客户交付");
        form.setOwnerCompanyId(ownerCompanyId);
        form.setPlannedStartAt(LocalDate.now());
        form.setPlannedEndAt(LocalDate.now().plusMonths(6));
        form.setStatus("筹备");
        return form;
    }

    private MemberForm member(long employeeId, long employmentId, Long assignmentId, LocalDate from) {
        MemberForm row = new MemberForm();
        row.setEmployeeId(employeeId);
        row.setEmploymentId(employmentId);
        row.setDepartmentAssignmentId(assignmentId);
        row.setProjectRole("实施");
        row.setValidFrom(from);
        return row;
    }

    private BudgetForm budget(int version, BigDecimal labor, BigDecimal nonlabor) {
        BudgetForm row = new BudgetForm();
        row.setVersionNumber(version);
        row.setBudgetCurrency("CNY");
        row.setLaborCostBudgetAmount(labor);
        row.setNonlaborCostBudgetAmount(nonlabor);
        row.setApprovalStatus("草稿");
        row.setValidFrom(LocalDate.now().minusDays(1));
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
