package com.liangshan.demo.seed;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractAmendment;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.SourceDocument;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractAmendmentRepository;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractLineRepository;
import com.liangshan.demo.repository.ContractPaymentScheduleRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectMemberRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.SourceDocumentRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.service.CompanyService;
import com.liangshan.demo.service.ContractService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.FulfillmentService;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.service.ProjectService;
import com.liangshan.demo.service.SourceDocumentService;
import com.liangshan.demo.service.SupplierService;
import com.liangshan.demo.web.form.CompanyForm;
import com.liangshan.demo.web.form.ContractAmendmentForm;
import com.liangshan.demo.web.form.ContractDetailForm;
import com.liangshan.demo.web.form.ContractDetailForm.DeliverableForm;
import com.liangshan.demo.web.form.ContractDetailForm.LineForm;
import com.liangshan.demo.web.form.ContractDetailForm.LinkForm;
import com.liangshan.demo.web.form.ContractDetailForm.StageForm;
import com.liangshan.demo.web.form.ContractForm;
import com.liangshan.demo.web.form.ContractPlanForm;
import com.liangshan.demo.web.form.ContractPlanForm.BillingRow;
import com.liangshan.demo.web.form.ContractPlanForm.PaymentRow;
import com.liangshan.demo.web.form.LegalPersonForm;
import com.liangshan.demo.web.form.PersonForm;
import com.liangshan.demo.web.form.ProjectDetailForm;
import com.liangshan.demo.web.form.ProjectDetailForm.BudgetForm;
import com.liangshan.demo.web.form.ProjectDetailForm.DeliveryForm;
import com.liangshan.demo.web.form.ProjectDetailForm.MemberForm;
import com.liangshan.demo.web.form.ProjectForm;
import com.liangshan.demo.web.form.SourceDocumentForm;
import com.liangshan.demo.web.form.SupplierDetailForm;
import com.liangshan.demo.web.form.SupplierDetailForm.CategoryForm;
import com.liangshan.demo.web.form.SupplierDetailForm.ContactForm;
import com.liangshan.demo.web.form.SupplierDetailForm.QualificationForm;
import com.liangshan.demo.web.form.SupplierForm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ProjectProcurementSeedStage implements SeedStage {

    private static final LocalDate FOUNDED = LocalDate.of(2018, 1, 1);
    private static final LocalDate PLAN_START = LocalDate.of(2024, 3, 1);
    private static final LocalDate PLAN_END = LocalDate.of(2026, 12, 31);
    private static final LocalDate CONTRACT_FROM = LocalDate.of(2024, 1, 15);
    private static final LocalDate CONTRACT_TO = LocalDate.of(2025, 12, 31);
    private static final LocalDate SIGNED = LocalDate.of(2024, 1, 10);
    private static final LocalDate AMEND_EFFECTIVE = LocalDate.of(2024, 6, 15);
    private static final LocalDateTime REVIEWED = LocalDateTime.of(2024, 1, 9, 9, 0);
    private static final BigDecimal RATE = new BigDecimal("0.13");
    private static final String HQ = OrgHrSeedStage.GROUP_CODE;
    private static final String JZ = "LS-JZ";
    private static final String DM = "LS-DM";
    private static final String BJ = "LS-BJ";
    private static final String JS = "LS-JS";

    private final LegalPersonService legalPersonService;
    private final CompanyService companyService;
    private final CompanyRepository companies;
    private final PersonService personService;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final DepartmentRepository departments;
    private final SourceDocumentService sourceDocuments;
    private final SourceDocumentRepository documents;
    private final SupplierService supplierService;
    private final SupplierRepository suppliers;
    private final ProjectService projectService;
    private final ProjectRepository projects;
    private final ProjectMemberRepository projectMembers;
    private final ContractService contractService;
    private final ContractRepository contracts;
    private final ContractLineRepository contractLines;
    private final ContractStageRepository contractStages;
    private final ContractTermVersionRepository contractTerms;
    private final ContractAmendmentRepository amendments;
    private final ContractBillingScheduleRepository billings;
    private final ContractPaymentScheduleRepository payments;
    private final FulfillmentService fulfillment;

    public ProjectProcurementSeedStage(
            LegalPersonService legalPersonService,
            CompanyService companyService,
            CompanyRepository companies,
            PersonService personService,
            PersonRepository persons,
            EmployeeRepository employees,
            EmployeeEmploymentRepository employments,
            EmployeeDepartmentAssignmentRepository assignments,
            DepartmentRepository departments,
            SourceDocumentService sourceDocuments,
            SourceDocumentRepository documents,
            SupplierService supplierService,
            SupplierRepository suppliers,
            ProjectService projectService,
            ProjectRepository projects,
            ProjectMemberRepository projectMembers,
            ContractService contractService,
            ContractRepository contracts,
            ContractLineRepository contractLines,
            ContractStageRepository contractStages,
            ContractTermVersionRepository contractTerms,
            ContractAmendmentRepository amendments,
            ContractBillingScheduleRepository billings,
            ContractPaymentScheduleRepository payments,
            FulfillmentService fulfillment) {
        this.legalPersonService = legalPersonService;
        this.companyService = companyService;
        this.companies = companies;
        this.personService = personService;
        this.persons = persons;
        this.employees = employees;
        this.employments = employments;
        this.assignments = assignments;
        this.departments = departments;
        this.sourceDocuments = sourceDocuments;
        this.documents = documents;
        this.supplierService = supplierService;
        this.suppliers = suppliers;
        this.projectService = projectService;
        this.projects = projects;
        this.projectMembers = projectMembers;
        this.contractService = contractService;
        this.contracts = contracts;
        this.contractLines = contractLines;
        this.contractStages = contractStages;
        this.contractTerms = contractTerms;
        this.amendments = amendments;
        this.billings = billings;
        this.payments = payments;
        this.fulfillment = fulfillment;
    }

    @Override
    public String code() {
        return "wp13";
    }

    @Override
    public int order() {
        return 130;
    }

    @Override
    public String label() {
        return "项目与采购";
    }

    @Override
    public void run() {
        requiredCompany(HQ);
        Map<String, Long> outside = outsideCompanies();
        long tokyoContact = person("宿元景", "suyuanjing@wp13.liangshan.test", "13800013001");
        long beijingContact = person("梁中书", "liangzhongshu@wp13.liangshan.test", "13800013002");
        long zhuBiao = person("祝彪", "zhubiao@wp13.liangshan.test", "13800013003");
        long zhuLong = person("祝龙", "zhulong@wp13.liangshan.test", "13800013004");
        long zengSheng = person("曾升", "zengsheng@wp13.liangshan.test", "13800013005");
        long luan = person("栾廷玉", "luantingyu@wp13.liangshan.test", "13800013006");
        long shi = person("史文恭", "shiwingong@wp13.liangshan.test", "13800013007");

        long zhuQual = document("DOC-W13-QUAL-ZHU", "资质证书", outside.get("WP13-ZHU"), "祝家庄兵器生产许可");
        long zengQual = document("DOC-W13-QUAL-ZENG", "资质证书", outside.get("WP13-ZENG"), "曾头市铁器生产许可");
        long liQual = document("DOC-W13-QUAL-LI", "资质证书", outside.get("WP13-LI"), "李家庄弓弩制作许可");
        long shiQual = document("DOC-W13-QUAL-SHI", "资质证书", outside.get("WP13-SHI"), "史家庄枪械制作许可");

        supplier("SUP-W13-ZHU-HQ", "祝家庄兵器坊（集团采购）", HQ, outside.get("WP13-ZHU"), 10, zhuQual,
                List.of(contact(zhuBiao, "商务"), contact(zhuLong, "交付")), "刀枪");
        supplier("SUP-W13-ZHU-JZ", "祝家庄兵器坊（济州采购）", JZ, outside.get("WP13-ZHU"), 5, zhuQual,
                List.of(contact(zhuBiao, "商务"), contact(zhuLong, "交付")), "刀枪");
        supplier("SUP-W13-ZENG", "曾头市铁铺", DM, outside.get("WP13-ZENG"), 6, zengQual,
                List.of(contact(zengSheng, "商务")), "甲仗");
        supplier("SUP-W13-LI", "李家庄弓弩铺", BJ, outside.get("WP13-LI"), 7, liQual,
                List.of(contact(luan, "商务")), "弓弩");
        supplier("SUP-W13-SHI", "史家庄枪坊", JS, outside.get("WP13-SHI"), 8, shiQual,
                List.of(contact(shi, "商务")), "长枪");

        for (ProjectSpec spec : projects()) {
            ensureProject(spec);
        }

        sales("CT-W13-SALE-TK", "东京甲仗销售合同", HQ, outside.get("WP13-TK"), "东京留守司",
                tokyoContact, 1, 2, "PJ-W13-06", "集团签约，济州交付",
                line(1, "甲仗", "套", "20", "5000.00"));
        sales("CT-W13-SALE-BJ", "大兴府弓弩销售合同", BJ, outside.get("WP13-BJC"), "北京大兴府",
                beijingContact, 7, 7, "PJ-W13-07", "北京分公司签约并交付",
                line(1, "弓弩", "张", "40", "2500.00"));

        purchase("CT-W13-BUY-HQ", "祝家庄刀枪采购合同", "SUP-W13-ZHU-HQ", HQ, 10, zhuBiao,
                "PJ-W13-08", "集团签约采购，大名负责项目交付",
                line(1, "朴刀", "把", "100", "1000.00"), true);
        purchase("CT-W13-BUY-JZ", "济州祝家庄刀枪采购合同", "SUP-W13-ZHU-JZ", JZ, 5, zhuBiao,
                "PJ-W13-09", "济州另行建档采购同一供货公司",
                line(1, "朴刀", "把", "100", "1000.00"), false);
        purchase("CT-W13-BUY-DM", "大名曾头市甲仗采购合同", "SUP-W13-ZENG", DM, 6, zengSheng,
                "PJ-W13-03", "大名军械库采购",
                line(1, "铁甲", "副", "50", "2000.00"), false);
        purchase("CT-W13-BUY-BJ", "北京李家庄弓弩采购合同", "SUP-W13-LI", BJ, 7, luan,
                "PJ-W13-04", "北京演武场采购",
                line(1, "弓", "张", "125", "800.00"), false);
        purchase("CT-W13-BUY-JS", "江州史家庄长枪采购合同", "SUP-W13-SHI", JS, 8, shi,
                "PJ-W13-10", "江州签约，项目交付在北京",
                line(1, "长枪", "支", "200", "500.00"), false);
    }

    private Map<String, Long> outsideCompanies() {
        company("WP13-TK", "东京留守司", "91370100MA3WP1301A", "河南省开封市东京御街 1 号");
        company("WP13-BJC", "北京大兴府", "91370100MA3WP1302A", "北京市大兴区府前街 2 号");
        company("WP13-ZHU", "祝家庄兵器坊", "91370100MA3WP1303A", "山东省济宁市祝家庄兵器巷 3 号");
        company("WP13-ZENG", "曾头市铁铺", "91370100MA3WP1304A", "山东省聊城市曾头市铁市街 4 号");
        company("WP13-LI", "李家庄弓弩铺", "91370100MA3WP1305A", "山东省济宁市李家庄弓弦路 5 号");
        company("WP13-SHI", "史家庄枪坊", "91370100MA3WP1306A", "山东省济宁市史家庄长枪路 6 号");
        return Map.of(
                "WP13-TK", requiredCompany("WP13-TK").id,
                "WP13-BJC", requiredCompany("WP13-BJC").id,
                "WP13-ZHU", requiredCompany("WP13-ZHU").id,
                "WP13-ZENG", requiredCompany("WP13-ZENG").id,
                "WP13-LI", requiredCompany("WP13-LI").id,
                "WP13-SHI", requiredCompany("WP13-SHI").id);
    }

    private List<ProjectSpec> projects() {
        return List.of(
                new ProjectSpec("PJ-W13-01", "水泊营寨整备", "内部建设", HQ, "D-OPS", HQ, "D-OPS", null, 2, new int[]{2, 10}),
                new ProjectSpec("PJ-W13-02", "济州城防加固", "内部建设", JZ, "D-MGR", JZ, "D-MGR", null, 5, new int[]{5, 13}),
                new ProjectSpec("PJ-W13-03", "大名军械库", "内部建设", DM, "D-MGR", DM, "D-MGR", null, 6, new int[]{6, 17}),
                new ProjectSpec("PJ-W13-04", "北京演武场", "内部建设", BJ, "D-MGR", BJ, "D-MGR", null, 7, new int[]{7, 15}),
                new ProjectSpec("PJ-W13-05", "江州水师船坞", "内部建设", JS, "D-MGR", JS, "D-MGR", null, 8, new int[]{8, 19}),
                new ProjectSpec("PJ-W13-06", "东京甲仗交付", "客户交付", HQ, "D-OPS", JZ, "D-MGR", "WP13-TK", 2, new int[]{2, 5}),
                new ProjectSpec("PJ-W13-07", "大兴府弓弩交付", "客户交付", BJ, "D-MGR", BJ, "D-MGR", "WP13-BJC", 7, new int[]{7, 16}),
                new ProjectSpec("PJ-W13-08", "集团兵器集采", "内部建设", HQ, "D-FIN", DM, "D-MGR", null, 10, new int[]{10, 6}),
                new ProjectSpec("PJ-W13-09", "济州兵器补给", "内部建设", JZ, "D-BIZ", JZ, "D-BIZ", null, 13, new int[]{13, 14}),
                new ProjectSpec("PJ-W13-10", "江州枪械补给", "内部建设", JS, "D-MGR", BJ, "D-MGR", null, 8, new int[]{8, 7}));
    }

    private void company(String code, String name, String uscc, String address) {
        if (companyOrNull(code) != null) {
            return;
        }
        LegalPersonForm legal = new LegalPersonForm();
        legal.setLegalName(name);
        legal.setLegalPersonType("企业法人");
        legal.setEstablishedAt(FOUNDED);
        legal.setStatus("存续");
        long legalPersonId = legalPersonService.save(legal);

        CompanyForm form = new CompanyForm();
        form.setCompanyCode(code);
        form.setName(name);
        form.setCompanyType(Codes.COMPANY);
        form.setLegalPersonId(legalPersonId);
        form.setCompanyForm("有限责任公司");
        form.setBusinessAddress(address);
        form.setEstablishedAt(FOUNDED);
        form.setStatus("存续");
        CompanyForm.RegistrationForm registration = new CompanyForm.RegistrationForm();
        registration.setVersionNumber(1);
        registration.setRegistrationType("设立登记");
        registration.setUnifiedSocialCreditCode(uscc);
        registration.setTaxpayerIdentificationNumber(uscc);
        registration.setRegistrationAuthority("当地市场监督管理局");
        registration.setRegistrationJurisdiction("中国");
        registration.setRegisteredName(name);
        registration.setRegisteredAddress(address);
        registration.setRegistrationStatus("存续");
        registration.setRegisteredAt(FOUNDED);
        registration.setValidFrom(FOUNDED);
        registration.setBusinessScope("兵器制造与甲仗贸易");
        registration.setBusinessTermStartDate(FOUNDED);
        registration.setBusinessTermIndefinite(true);
        registration.setRegisteredCapitalAmount(new BigDecimal("5000000.00"));
        registration.setRegisteredCapitalCurrency("CNY");
        form.getRegistrations().add(registration);
        companyService.save(form);
    }

    private long person(String name, String email, String phone) {
        for (Person person : persons.findAllByOrderByNameAsc()) {
            if (email.equals(person.email)) {
                return person.id;
            }
        }
        PersonForm form = new PersonForm();
        form.setName(name);
        form.setEmail(email);
        form.setMobilePhone(phone);
        form.setStatus("有效");
        return personService.save(form);
    }

    private long document(String number, String type, long issuerCompanyId, String summary) {
        for (SourceDocument document : documents.findAll()) {
            if (number.equals(document.documentNumber)) {
                return document.id;
            }
        }
        SourceDocumentForm form = new SourceDocumentForm();
        form.setDocumentType(type);
        form.setDocumentNumber(number);
        form.setIssuerCompanyId(issuerCompanyId);
        form.setPreparedByEmployeeId(staff(1).employeeId());
        form.setIssuedAt(SIGNED);
        form.setContentSummary(summary);
        form.setStatus("有效");
        return sourceDocuments.save(form);
    }

    private void supplier(
            String code, String name, String purchasingCode, long supplyingCompanyId, int ownerSeat,
            long qualificationDocumentId, List<ContactForm> contacts, String categoryName) {
        long purchasingCompanyId = requiredCompany(purchasingCode).id;
        Supplier existing = supplier(purchasingCompanyId, code);
        if (existing != null) {
            return;
        }
        SupplierForm form = new SupplierForm();
        form.setSupplierCode(code);
        form.setSupplierName(name);
        form.setPurchasingCompanyId(purchasingCompanyId);
        form.setCompanyId(supplyingCompanyId);
        form.setProcurementOwnerEmployeeId(staff(ownerSeat).employeeId());
        form.setAdmissionStatus("通过");
        form.setAdmissionSubmittedAt(LocalDateTime.of(2024, 1, 8, 9, 0));
        form.setAdmissionReviewedByEmployeeId(staff(3).employeeId());
        form.setAdmissionReviewedAt(REVIEWED);
        form.setCooperationStatus("合作中");
        form.setCooperationStartedOn(LocalDate.of(2024, 1, 1));
        form.setCooperationStatusEffectiveAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        form.setDefaultSettlementCurrency("CNY");
        form.setDefaultPaymentDueBasis("发票确认日");
        form.setDefaultPaymentTermDays(30);
        long supplierId = supplierService.save(form);

        SupplierDetailForm details = new SupplierDetailForm();
        details.getContacts().addAll(contacts);
        CategoryForm category = new CategoryForm();
        category.setSupplyCategoryCode("BINGQI");
        category.setSupplyCategoryName("兵器");
        category.setSupplyScopeDescription(categoryName);
        category.setValidFrom(LocalDate.of(2024, 1, 1));
        category.setStatus("有效");
        details.getCategories().add(category);
        QualificationForm qualification = new QualificationForm();
        qualification.setQualificationName("兵器生产许可");
        qualification.setCertificateNumber(code + "-LIC");
        qualification.setIssuingAuthority("当地市场监督管理局");
        qualification.setIssuedOn(LocalDate.of(2023, 1, 1));
        qualification.setExpiresOn(LocalDate.of(2028, 1, 1));
        qualification.setNoExpiry(false);
        qualification.setQualificationDocumentId(qualificationDocumentId);
        qualification.setReviewStatus("通过");
        qualification.setReviewedByEmployeeId(staff(3).employeeId());
        qualification.setReviewedAt(REVIEWED);
        qualification.setStatus("有效");
        details.getQualifications().add(qualification);
        supplierService.saveDetails(supplierId, details);
    }

    private ContactForm contact(long personId, String role) {
        ContactForm row = new ContactForm();
        row.setPersonId(personId);
        row.setContactRole(role);
        row.setBusinessPhone("13800013000");
        row.setBusinessEmail("contact@wp13.liangshan.test");
        row.setPrimary(true);
        row.setValidFrom(LocalDate.of(2024, 1, 1));
        row.setStatus("有效");
        return row;
    }

    private void ensureProject(ProjectSpec spec) {
        if (project(spec.code()) != null && !projectMembers.findByProjectIdOrderByIdAsc(project(spec.code()).id).isEmpty()) {
            return;
        }
        Project existing = project(spec.code());
        long projectId = existing == null ? projectService.save(header(spec)) : existing.id;
        if (!projectMembers.findByProjectIdOrderByIdAsc(projectId).isEmpty()) {
            return;
        }
        ProjectDetailForm details = new ProjectDetailForm();
        boolean first = true;
        for (int seat : spec.memberSeats()) {
            Staff member = staff(seat);
            MemberForm row = new MemberForm();
            row.setEmployeeId(member.employeeId());
            row.setEmploymentId(member.employmentId());
            row.setDepartmentAssignmentId(member.assignmentId());
            row.setProjectRole(first ? "交付负责人" : "实施");
            row.setPlannedHours(new BigDecimal("160"));
            row.setValidFrom(PLAN_START);
            details.getMembers().add(row);
            first = false;
        }
        BudgetForm budget = new BudgetForm();
        budget.setVersionNumber(1);
        budget.setBudgetCurrency("CNY");
        budget.setPlannedLaborHours(new BigDecimal("200"));
        budget.setLaborCostBudgetAmount(new BigDecimal("50000.00"));
        budget.setNonlaborCostBudgetAmount(new BigDecimal("80000.00"));
        budget.setBudgetCostBasis("标准人工");
        budget.setApprovalStatus("已批准");
        budget.setSubmittedAt(LocalDateTime.of(2024, 2, 20, 9, 0));
        budget.setApprovedByEmployeeId(staff(1).employeeId());
        budget.setApprovedAt(LocalDateTime.of(2024, 2, 21, 9, 0));
        budget.setValidFrom(PLAN_START);
        details.getBudgets().add(budget);
        DeliveryForm delivery = new DeliveryForm();
        delivery.setDeliveryCompanyId(requiredCompany(spec.deliveryCode()).id);
        delivery.setLeadDepartmentId(department(spec.deliveryCode(), spec.deliveryDept()).id);
        delivery.setResponsibilityDescription(spec.deliveryCode().equals(spec.ownerCode())
                ? "本单位交付"
                : "交付公司与项目管理公司不同");
        delivery.setValidFrom(PLAN_START);
        details.getDeliveries().add(delivery);
        projectService.saveDetails(projectId, details);
    }

    private ProjectForm header(ProjectSpec spec) {
        ProjectForm form = new ProjectForm();
        form.setProjectCode(spec.code());
        form.setName(spec.name());
        form.setProjectType(spec.type());
        form.setProjectScopeDescription(spec.name());
        form.setOwnerCompanyId(requiredCompany(spec.ownerCode()).id);
        form.setLeadDepartmentId(department(spec.ownerCode(), spec.ownerDept()).id);
        form.setProjectManagerEmployeeId(staff(spec.managerSeat()).employeeId());
        if (spec.customerCode() != null) {
            form.setCustomerCompanyId(requiredCompany(spec.customerCode()).id);
        }
        form.setPlannedStartAt(PLAN_START);
        form.setPlannedEndAt(PLAN_END);
        form.setActualStartAt(PLAN_START);
        form.setStatus("进行中");
        return form;
    }

    private void sales(
            String number, String title, String sellerCode, long buyerCompanyId, String buyerName,
            long buyerPersonId, int sellerSignatorySeat, int ownerSeat, String projectCode, String note, LineForm line) {
        long sellerCompanyId = requiredCompany(sellerCode).id;
        long sellerPersonId = staff(sellerSignatorySeat).personId();
        long contractId = ensureSalesContract(number, title, sellerCompanyId, sellerCode, buyerCompanyId, buyerName,
                buyerPersonId, sellerPersonId, ownerSeat);
        ensureSalesBody(contractId, projectCode, note, line, staff(ownerSeat).employeeId());
        ensureSalePlans(contractId);
    }

    private long ensureSalesContract(
            String number, String title, long sellerCompanyId, String sellerCode, long buyerCompanyId, String buyerName,
            long buyerPersonId, long sellerPersonId, int ownerSeat) {
        Contract existing = contract(number);
        if (existing != null) {
            return existing.id;
        }
        ContractForm form = commercial(number, title, "销售", buyerCompanyId, sellerCompanyId, buyerName,
                requiredCompany(sellerCode).name, buyerPersonId, sellerPersonId, ownerSeat, sellerCompanyId);
        form.setSourceDocumentId(document("DOC-W13-" + number, "合同", sellerCompanyId, title));
        money(form, "100000.00", "13000.00", "113000.00");
        return contractService.save(form);
    }

    private void ensureSalesBody(long contractId, String projectCode, String note, LineForm line, long responsibleEmployeeId) {
        if (!contractLines.findByContractIdOrderByLineNumberAsc(contractId).isEmpty()) {
            return;
        }
        ContractDetailForm details = new ContractDetailForm();
        details.getLines().add(line);
        details.getStages().add(stage("S1", 1, "备货", responsibleEmployeeId));
        details.getStages().add(stage("S2", 2, "验收", responsibleEmployeeId));
        details.getDeliverables().add(deliverable("S2", line));
        LinkForm link = new LinkForm();
        link.setProjectId(requiredProject(projectCode).id);
        link.setRelationshipNote(note);
        details.getLinks().add(link);
        contractService.saveDetails(contractId, details);
    }

    private void ensureSalePlans(long contractId) {
        if (!billings.findByContractIdOrderByScheduleNumberAsc(contractId).isEmpty()) {
            return;
        }
        ContractTermVersion term = openTerm(contractId);
        ContractStage acceptance = stage(contractId, "S2");
        ContractPlanForm form = new ContractPlanForm();
        form.getBillings().add(billing(1, term.id, null, "指定日期", "56500.00", LocalDate.of(2024, 4, 1), null));
        form.getBillings().add(billing(2, term.id, acceptance.id, "阶段验收", "56500.00", null, 15));
        form.getPayments().add(payment(1, term.id, null, "预付款", "预付款", "指定日期", "33900.00",
                LocalDate.of(2024, 2, 1), null));
        form.getPayments().add(payment(2, term.id, acceptance.id, "进度款", "进度款", "开票", "79100.00", null, 30));
        fulfillment.savePlans(contractId, form);
    }

    private void purchase(
            String number, String title, String supplierCode, String buyerCode, int buyerSeat, long sellerPersonId,
            String projectCode, String note, LineForm line, boolean amend) {
        long buyerCompanyId = requiredCompany(buyerCode).id;
        Supplier supplier = requiredSupplier(buyerCompanyId, supplierCode);
        Contract existing = contract(number);
        long contractId = existing == null
                ? savePurchase(number, title, buyerCompanyId, buyerCode, supplier, buyerSeat, sellerPersonId)
                : existing.id;
        if (contractLines.findByContractIdOrderByLineNumberAsc(contractId).isEmpty()) {
            ContractDetailForm details = new ContractDetailForm();
            details.getLines().add(line);
            details.getStages().add(stage("S1", 1, "到货", staff(buyerSeat).employeeId()));
            details.getDeliverables().add(deliverable("S1", line));
            LinkForm link = new LinkForm();
            link.setProjectId(requiredProject(projectCode).id);
            link.setRelationshipNote(note);
            details.getLinks().add(link);
            contractService.saveDetails(contractId, details);
        }
        if (amend) {
            ensureAmendment(contractId, buyerCompanyId);
        }
        if (payments.findByContractIdOrderByScheduleNumberAsc(contractId).isEmpty()) {
            ContractTermVersion term = openTerm(contractId);
            ContractStage arrival = stage(contractId, "S1");
            ContractPlanForm form = new ContractPlanForm();
            BigDecimal gross = openTerm(contractId).contractGrossAmount;
            BigDecimal advance = gross.multiply(new BigDecimal("0.30")).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal rest = gross.subtract(advance);
            form.getPayments().add(payment(1, term.id, null, "预付款", "预付款", "指定日期", advance.toPlainString(),
                    LocalDate.of(2024, 2, 1), null));
            form.getPayments().add(payment(2, term.id, arrival.id, "尾款", "尾款", "阶段验收", rest.toPlainString(),
                    null, 15));
            fulfillment.savePlans(contractId, form);
        }
    }

    private long savePurchase(
            String number, String title, long buyerCompanyId, String buyerCode, Supplier supplier, int buyerSeat,
            long sellerPersonId) {
        ContractForm form = commercial(number, title, "采购", buyerCompanyId, supplier.companyId,
                requiredCompany(buyerCode).name, supplierName(supplier), staff(buyerSeat).personId(), sellerPersonId,
                buyerSeat, buyerCompanyId);
        form.setProcurementCompanyId(buyerCompanyId);
        form.setSupplierId(supplier.id);
        form.setSourceDocumentId(document("DOC-W13-" + number, "合同", buyerCompanyId, title));
        money(form, "100000.00", "13000.00", "113000.00");
        return contractService.save(form);
    }

    private void ensureAmendment(long contractId, long buyerCompanyId) {
        for (ContractAmendment existing : amendments.findByContractIdOrderByIdAsc(contractId)) {
            if ("BG-W13-1".equals(existing.amendmentNumber)) {
                return;
            }
        }
        ContractAmendmentForm amendment = new ContractAmendmentForm();
        amendment.setAmendmentNumber("BG-W13-1");
        amendment.setSignedAt(LocalDate.of(2024, 6, 1));
        amendment.setEffectiveAt(AMEND_EFFECTIVE);
        amendment.setSourceDocumentId(document("DOC-W13-AMD-BUY-HQ", "合同变更", buyerCompanyId, "增购朴刀，上调含税总价"));
        amendment.setSummary("增购朴刀，上调含税总价，原条款版本保留");
        ContractForm terms = contractService.form(contractId);
        money(terms, "120000.00", "15600.00", "135600.00");
        amendment.setTerms(terms);
        contractService.registerAmendment(contractId, amendment);
    }

    private ContractForm commercial(
            String number, String title, String type, long buyerCompanyId, long sellerCompanyId, String buyerName,
            String sellerName, long buyerPersonId, long sellerPersonId, int ownerSeat, long managingCompanyId) {
        ContractForm form = new ContractForm();
        form.setContractNumber(number);
        form.setContractTitle(title);
        form.setContractType(type);
        form.setManagingCompanyId(managingCompanyId);
        form.setContractOwnerEmployeeId(staff(ownerSeat).employeeId());
        form.setBuyerCompanyId(buyerCompanyId);
        form.setSellerCompanyId(sellerCompanyId);
        form.setBuyerNameSnapshot(buyerName);
        form.setSellerNameSnapshot(sellerName);
        form.setBuyerSignatoryPersonId(buyerPersonId);
        form.setSellerSignatoryPersonId(sellerPersonId);
        form.setBuyerContactPersonId(buyerPersonId);
        form.setSellerContactPersonId(sellerPersonId);
        form.setSignedAt(SIGNED);
        form.setValidFrom(CONTRACT_FROM);
        form.setValidTo(CONTRACT_TO);
        form.setCurrency("CNY");
        form.setPricingMethod("固定总价");
        form.setApprovalStatus("已批准");
        form.setContractApprovedAt(LocalDateTime.of(2024, 1, 12, 10, 0));
        form.setContractApprovedByEmployeeId(staff(1).employeeId());
        form.setStatus("生效");
        return form;
    }

    private void money(ContractForm form, String net, String tax, String gross) {
        form.setContractNetAmount(new BigDecimal(net));
        form.setContractTaxAmount(new BigDecimal(tax));
        form.setContractGrossAmount(new BigDecimal(gross));
    }

    private LineForm line(int number, String name, String unit, String quantity, String unitPrice) {
        BigDecimal qty = new BigDecimal(quantity);
        BigDecimal price = new BigDecimal(unitPrice);
        BigDecimal net = qty.multiply(price).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal tax = net.multiply(RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        LineForm row = new LineForm();
        row.setLineNumber(number);
        row.setItemName(name);
        row.setUnit(unit);
        row.setContractedQuantity(qty);
        row.setUnitNetPrice(price);
        row.setTaxRate(RATE);
        row.setLineNetAmount(net);
        row.setLineTaxAmount(tax);
        row.setLineGrossAmount(net.add(tax));
        return row;
    }

    private StageForm stage(String code, int sequence, String name, long responsibleEmployeeId) {
        StageForm row = new StageForm();
        row.setStageCode(code);
        row.setSequenceNo(sequence);
        row.setStageName(name);
        row.setResponsibleEmployeeId(responsibleEmployeeId);
        row.setStatus("未开始");
        row.setDeliverableDescription(name);
        row.setAcceptanceCriteria("数量和质量符合合同");
        row.setPlannedStartAt(PLAN_START);
        row.setPlannedDueAt(LocalDate.of(2024, 9, 30));
        row.setCurrency("CNY");
        return row;
    }

    private DeliverableForm deliverable(String stageCode, LineForm line) {
        DeliverableForm row = new DeliverableForm();
        row.setStageCode(stageCode);
        row.setLineNumber(line.getLineNumber());
        row.setDeliverableCode("DL-1");
        row.setDeliverableName(line.getItemName());
        row.setUnit(line.getUnit());
        row.setPlannedQuantity(line.getContractedQuantity());
        row.setAcceptanceCriteria("点收合格");
        row.setPlannedDeliveryDate(LocalDate.of(2024, 6, 1));
        return row;
    }

    private BillingRow billing(
            int number, long termId, Long stageId, String trigger, String amount, LocalDate plannedDate, Integer days) {
        BillingRow row = new BillingRow();
        row.setScheduleNumber(number);
        row.setContractTermVersionId(termId);
        row.setContractStageId(stageId);
        row.setBillingTrigger(trigger);
        row.setPlannedInvoiceGrossAmount(new BigDecimal(amount));
        row.setCurrency("CNY");
        row.setPlannedInvoiceDate(plannedDate);
        row.setDaysAfterTrigger(days);
        row.setValidFrom(CONTRACT_FROM);
        return row;
    }

    private PaymentRow payment(
            int number, long termId, Long stageId, String installment, String purpose, String trigger, String amount,
            LocalDate plannedDate, Integer days) {
        PaymentRow row = new PaymentRow();
        row.setScheduleNumber(number);
        row.setContractTermVersionId(termId);
        row.setContractStageId(stageId);
        row.setInstallmentName(installment);
        row.setPaymentPurpose(purpose);
        row.setPaymentTrigger(trigger);
        row.setPlannedPaymentAmount(new BigDecimal(amount));
        row.setCurrency("CNY");
        row.setPlannedDueDate(plannedDate);
        row.setDaysAfterTrigger(days);
        row.setValidFrom(CONTRACT_FROM);
        return row;
    }

    private ContractTermVersion openTerm(long contractId) {
        ContractTermVersion open = null;
        for (ContractTermVersion term : contractTerms.findByContractIdOrderByVersionNumberAsc(contractId)) {
            if (term.validTo == null) {
                open = term;
            }
        }
        if (open == null) {
            throw new FieldErrorsException(Map.of("*", "合同没有当前条款"));
        }
        return open;
    }

    private ContractStage stage(long contractId, String code) {
        for (ContractStage stage : contractStages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            if (code.equals(stage.stageCode)) {
                return stage;
            }
        }
        throw new FieldErrorsException(Map.of("*", "合同阶段不存在：" + code));
    }

    private String supplierName(Supplier supplier) {
        Company company = companies.findById(supplier.companyId).orElse(null);
        return company == null ? supplier.supplierName : company.name;
    }

    private Staff staff(int seat) {
        Employee employee = requiredEmployee(seat);
        List<EmployeeEmployment> hires = employments.findByEmployeeIdOrderByIdAsc(employee.id);
        List<EmployeeDepartmentAssignment> posts = assignments.findByEmployeeIdOrderByIdAsc(employee.id);
        if (hires.isEmpty() || posts.isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "员工尚未任职：" + LiangshanRoster.employeeCode(seat)));
        }
        return new Staff(employee.id, employee.personId, hires.get(0).id, posts.get(0).id);
    }

    private Employee requiredEmployee(int seat) {
        List<Employee> found = employees.findByEmployeeCode(LiangshanRoster.employeeCode(seat));
        if (found.isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "请先载入组织与人事种子"));
        }
        return found.get(0);
    }

    private Company companyOrNull(String code) {
        List<Company> found = companies.findByCompanyCode(code);
        return found.isEmpty() ? null : found.get(0);
    }

    private Company requiredCompany(String code) {
        Company found = companyOrNull(code);
        if (found == null) {
            throw new FieldErrorsException(Map.of("*", "公司不存在：" + code));
        }
        return found;
    }

    private Department department(String companyCode, String departmentCode) {
        Department found = departments.findByCompanyIdAndDepartmentCode(requiredCompany(companyCode).id, departmentCode)
                .stream().findFirst().orElse(null);
        if (found == null) {
            throw new FieldErrorsException(Map.of("*", "部门不存在：" + companyCode + " " + departmentCode));
        }
        return found;
    }

    private Supplier supplier(long purchasingCompanyId, String code) {
        for (Supplier supplier : suppliers.findByPurchasingCompanyId(purchasingCompanyId)) {
            if (code.equals(supplier.supplierCode)) {
                return supplier;
            }
        }
        return null;
    }

    private Supplier requiredSupplier(long purchasingCompanyId, String code) {
        Supplier found = supplier(purchasingCompanyId, code);
        if (found == null) {
            throw new FieldErrorsException(Map.of("*", "供应商档案不存在：" + code));
        }
        return found;
    }

    private Project project(String code) {
        List<Project> found = projects.findByProjectCode(code);
        return found.isEmpty() ? null : found.get(0);
    }

    private Project requiredProject(String code) {
        Project found = project(code);
        if (found == null) {
            throw new FieldErrorsException(Map.of("*", "项目不存在：" + code));
        }
        return found;
    }

    private Contract contract(String number) {
        List<Contract> found = contracts.findByContractNumber(number);
        return found.isEmpty() ? null : found.get(0);
    }

    private record Staff(long employeeId, long personId, long employmentId, long assignmentId) {
    }

    private record ProjectSpec(
            String code, String name, String type, String ownerCode, String ownerDept, String deliveryCode,
            String deliveryDept, String customerCode, int managerSeat, int[] memberSeats) {
    }
}
