package com.liangshan.demo.seed;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeReportingLineRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.service.CompanyService;
import com.liangshan.demo.service.DepartmentService;
import com.liangshan.demo.service.EmployeeOrgService;
import com.liangshan.demo.service.EmployeeService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.LegalPersonService;
import com.liangshan.demo.service.PersonService;
import com.liangshan.demo.web.form.CompanyForm;
import com.liangshan.demo.web.form.DepartmentForm;
import com.liangshan.demo.web.form.EmployeeForm;
import com.liangshan.demo.web.form.EmployeeOrgForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.AssignmentForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.EmploymentForm;
import com.liangshan.demo.web.form.EmployeeOrgForm.ReportingForm;
import com.liangshan.demo.web.form.LegalPersonForm;
import com.liangshan.demo.web.form.PersonForm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OrgHrSeedStage implements SeedStage {

    static final String GROUP_CODE = "LS-GROUP";
    static final String LEGAL_NAME = "梁山集团有限公司";
    private static final LocalDate FOUNDED = LocalDate.of(2000, 1, 1);
    private static final LocalDate JOINED = LocalDate.of(2020, 1, 1);
    private static final String BANK = "梁山泊银行";

    private final LegalPersonService legalPersonService;
    private final LegalPersonRepository legalPersons;
    private final CompanyService companyService;
    private final CompanyRepository companies;
    private final PersonService personService;
    private final EmployeeService employeeService;
    private final EmployeeRepository employees;
    private final DepartmentService departmentService;
    private final DepartmentRepository departments;
    private final EmployeeOrgService organizations;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final EmployeeReportingLineRepository reportingLines;

    public OrgHrSeedStage(
            LegalPersonService legalPersonService,
            LegalPersonRepository legalPersons,
            CompanyService companyService,
            CompanyRepository companies,
            PersonService personService,
            EmployeeService employeeService,
            EmployeeRepository employees,
            DepartmentService departmentService,
            DepartmentRepository departments,
            EmployeeOrgService organizations,
            EmployeeEmploymentRepository employments,
            EmployeeDepartmentAssignmentRepository assignments,
            EmployeeReportingLineRepository reportingLines) {
        this.legalPersonService = legalPersonService;
        this.legalPersons = legalPersons;
        this.companyService = companyService;
        this.companies = companies;
        this.personService = personService;
        this.employeeService = employeeService;
        this.employees = employees;
        this.departmentService = departmentService;
        this.departments = departments;
        this.organizations = organizations;
        this.employments = employments;
        this.assignments = assignments;
        this.reportingLines = reportingLines;
    }

    @Override
    public String code() {
        return "wp12";
    }

    @Override
    public int order() {
        return 120;
    }

    @Override
    public String label() {
        return "组织与人事";
    }

    @Override
    public void run() {
        for (int seat = 1; seat <= LiangshanRoster.NAMES.size(); seat++) {
            ensureEmployee(seat);
        }
        long legalPersonId = ensureLegalPerson();
        ensureGroup(legalPersonId);
        for (BranchSpec branch : branches()) {
            ensureBranch(branch);
        }
        for (DeptSpec dept : departments()) {
            ensureDepartment(dept);
        }
        for (Placement placement : placements()) {
            ensureOrganization(placement);
        }
    }

    private void ensureEmployee(int seat) {
        if (!employees.findByEmployeeCode(LiangshanRoster.employeeCode(seat)).isEmpty()) {
            return;
        }
        PersonForm person = new PersonForm();
        person.setName(LiangshanRoster.name(seat));
        person.setStatus("有效");
        long personId = personService.save(person);
        EmployeeForm employee = new EmployeeForm();
        employee.setPersonId(personId);
        employee.setEmployeeCode(LiangshanRoster.employeeCode(seat));
        employee.setStatus("有效");
        employeeService.save(employee);
    }

    private long ensureLegalPerson() {
        Company group = company(GROUP_CODE);
        if (group != null && group.legalPersonId != null) {
            return group.legalPersonId;
        }
        for (LegalPerson legalPerson : legalPersons.findAllByOrderByLegalNameAsc()) {
            if (LEGAL_NAME.equals(legalPerson.legalName) && companies.findByLegalPersonId(legalPerson.id).isEmpty()) {
                return legalPerson.id;
            }
        }
        LegalPersonForm form = new LegalPersonForm();
        form.setLegalName(LEGAL_NAME);
        form.setLegalPersonType("企业法人");
        form.setEstablishedAt(FOUNDED);
        form.setStatus("存续");
        return legalPersonService.save(form);
    }

    private void ensureGroup(long legalPersonId) {
        if (company(GROUP_CODE) != null) {
            return;
        }
        CompanyForm form = companyForm(GROUP_CODE, "梁山集团", Codes.COMPANY);
        form.setLegalPersonId(legalPersonId);
        form.setCompanyForm("有限责任公司");
        form.setBusinessAddress("山东省济宁市梁山县水泊路 1 号");
        form.getRegistrations().add(registration(
                "91370100MA3LS0001A",
                "梁山集团有限公司",
                "山东省济宁市梁山县水泊路 1 号",
                "济南市市场监督管理局",
                null,
                new BigDecimal("100000000.00")));
        form.getBankAccounts().add(bank("梁山集团有限公司", "622200000000000001", "水泊支行", true, true));
        form.getBankAccounts().add(bank("梁山集团有限公司", "622200000000000002", "忠义支行", false, true));
        companyService.save(form);
    }

    private void ensureBranch(BranchSpec branch) {
        if (company(branch.code()) != null) {
            return;
        }
        Company head = company(GROUP_CODE);
        if (head == null) {
            throw new FieldErrorsException(Map.of("*", "梁山集团尚未建立"));
        }
        CompanyForm form = companyForm(branch.code(), branch.name(), Codes.BRANCH);
        form.setHeadCompanyId(head.id);
        form.setBusinessAddress(branch.address());
        CompanyForm.RegistrationForm registration = registration(
                branch.uscc(), branch.name(), branch.address(), branch.authority(), null, null);
        registration.setBranchManagerPersonId(requiredEmployee(branch.managerSeat()).personId);
        form.getRegistrations().add(registration);
        form.getBankAccounts().add(bank(branch.name(), branch.account(), branch.bankBranch(), true, true));
        companyService.save(form);
    }

    private void ensureDepartment(DeptSpec spec) {
        Company company = requiredCompany(spec.companyCode());
        Long parentId = null;
        if (spec.parentCode() != null) {
            parentId = requiredDepartment(spec.companyCode(), spec.parentCode()).id;
        }
        long headId = requiredEmployee(spec.headSeat()).id;
        List<Department> found = departments.findByCompanyIdAndDepartmentCode(company.id, spec.code());
        if (!found.isEmpty()) {
            Department existing = found.get(0);
            if (existing.departmentHeadEmployeeId == null) {
                DepartmentForm form = departmentService.form(existing.id);
                form.setDepartmentHeadEmployeeId(headId);
                if (form.getParentDepartmentId() == null) {
                    form.setParentDepartmentId(parentId);
                }
                departmentService.save(form);
            }
            return;
        }
        DepartmentForm form = new DepartmentForm();
        form.setCompanyId(company.id);
        form.setParentDepartmentId(parentId);
        form.setDepartmentCode(spec.code());
        form.setName(spec.name());
        form.setDepartmentType(spec.type());
        form.setDepartmentHeadEmployeeId(headId);
        form.setValidFrom(JOINED);
        form.setStatus("有效");
        departmentService.save(form);
    }

    private void ensureOrganization(Placement placement) {
        Employee self = requiredEmployee(placement.seat());
        Long managerAssignmentId = managerAssignmentId(placement.managerSeat());
        List<EmployeeEmployment> hires = employments.findByEmployeeIdOrderByIdAsc(self.id);
        List<EmployeeDepartmentAssignment> posts = assignments.findByEmployeeIdOrderByIdAsc(self.id);
        boolean hasAdmin = reportingLines.findByEmployeeIdOrderByIdAsc(self.id).stream()
                .anyMatch(line -> Codes.REPORTING_ADMIN.equals(line.reportingType));
        if (!hires.isEmpty() && !posts.isEmpty() && (managerAssignmentId == null || hasAdmin)) {
            return;
        }
        Department department = requiredDepartment(placement.companyCode(), placement.departmentCode());
        Company company = requiredCompany(placement.companyCode());
        if (hires.isEmpty()) {
            EmployeeOrgForm form = new EmployeeOrgForm();
            EmploymentForm hire = new EmploymentForm();
            hire.setRowKey("hire");
            hire.setEmployerCompanyId(company.id);
            hire.setEmployeeNumber(self.employeeCode);
            hire.setEmploymentType("全职");
            hire.setEmploymentStartDate(JOINED);
            hire.setStatus("在职");
            form.getEmployments().add(hire);
            AssignmentForm post = assignment("post", "hire", department.id, placement.title());
            form.getAssignments().add(post);
            if (managerAssignmentId != null) {
                form.getReportingLines().add(adminLine("post", managerAssignmentId));
            }
            organizations.save(self.id, form);
            return;
        }
        EmployeeOrgForm form = organizations.form(self.id);
        String assignmentKey = posts.isEmpty() ? "post" : "id:" + posts.get(0).id;
        if (posts.isEmpty()) {
            form.getAssignments().add(assignment("post", "id:" + hires.get(0).id, department.id, placement.title()));
        }
        if (managerAssignmentId != null && !hasAdmin) {
            form.getReportingLines().add(adminLine(assignmentKey, managerAssignmentId));
        }
        organizations.save(self.id, form);
    }

    private Long managerAssignmentId(Integer managerSeat) {
        if (managerSeat == null) {
            return null;
        }
        List<EmployeeDepartmentAssignment> posts =
                assignments.findByEmployeeIdOrderByIdAsc(requiredEmployee(managerSeat).id);
        if (posts.isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "上级尚未任职：" + LiangshanRoster.name(managerSeat)));
        }
        return posts.get(0).id;
    }

    private AssignmentForm assignment(String rowKey, String employmentKey, long departmentId, String title) {
        AssignmentForm post = new AssignmentForm();
        post.setRowKey(rowKey);
        post.setEmploymentKey(employmentKey);
        post.setDepartmentId(departmentId);
        post.setAssignmentType("主岗");
        post.setPositionTitle(title);
        post.setValidFrom(JOINED);
        post.setStatus("有效");
        return post;
    }

    private ReportingForm adminLine(String assignmentKey, long managerAssignmentId) {
        ReportingForm line = new ReportingForm();
        line.setAssignmentKey(assignmentKey);
        line.setManagerAssignmentKey("id:" + managerAssignmentId);
        line.setReportingType(Codes.REPORTING_ADMIN);
        line.setValidFrom(JOINED);
        return line;
    }

    private CompanyForm companyForm(String code, String name, String type) {
        CompanyForm form = new CompanyForm();
        form.setCompanyCode(code);
        form.setName(name);
        form.setCompanyType(type);
        form.setEstablishedAt(FOUNDED);
        form.setStatus("存续");
        return form;
    }

    private CompanyForm.RegistrationForm registration(
            String uscc, String registeredName, String address, String authority, Long branchManagerPersonId,
            BigDecimal capital) {
        CompanyForm.RegistrationForm row = new CompanyForm.RegistrationForm();
        row.setVersionNumber(1);
        row.setRegistrationType("设立登记");
        row.setUnifiedSocialCreditCode(uscc);
        row.setTaxpayerIdentificationNumber(uscc);
        row.setRegistrationAuthority(authority);
        row.setRegistrationJurisdiction("中国");
        row.setRegisteredName(registeredName);
        row.setRegisteredAddress(address);
        row.setRegistrationStatus("存续");
        row.setRegisteredAt(FOUNDED);
        row.setValidFrom(FOUNDED);
        row.setBusinessScope("企业管理、咨询与项目交付");
        row.setBusinessTermStartDate(FOUNDED);
        row.setBusinessTermIndefinite(true);
        row.setBranchManagerPersonId(branchManagerPersonId);
        if (capital != null) {
            row.setRegisteredCapitalAmount(capital);
            row.setRegisteredCapitalCurrency("CNY");
        }
        return row;
    }

    private CompanyForm.BankAccountForm bank(
            String accountName, String number, String branch, boolean receipt, boolean payment) {
        CompanyForm.BankAccountForm row = new CompanyForm.BankAccountForm();
        row.setBankAccountName(accountName);
        row.setBankAccountNumber(number);
        row.setBankName(BANK);
        row.setBankBranchName(branch);
        row.setAccountCurrency("CNY");
        row.setReceiptAccount(receipt);
        row.setPaymentAccount(payment);
        row.setValidFrom(FOUNDED);
        row.setStatus("有效");
        return row;
    }

    private List<Placement> placements() {
        List<DeptSpec> plan = departments();
        Map<Integer, Placement> bySeat = new HashMap<>();
        Set<Integer> heads = new HashSet<>();
        for (DeptSpec dept : plan) {
            heads.add(dept.headSeat());
            bySeat.put(dept.headSeat(), new Placement(
                    dept.headSeat(), dept.companyCode(), dept.code(), title(dept), managerSeat(dept, plan)));
        }
        List<Integer> staff = new ArrayList<>();
        for (int seat = 1; seat <= LiangshanRoster.NAMES.size(); seat++) {
            if (!heads.contains(seat)) {
                staff.add(seat);
            }
        }
        for (int index = 0; index < staff.size(); index++) {
            DeptSpec dept = plan.get(index % plan.size());
            int seat = staff.get(index);
            bySeat.put(seat, new Placement(seat, dept.companyCode(), dept.code(), "职员", dept.headSeat()));
        }
        if (bySeat.size() != LiangshanRoster.NAMES.size()) {
            throw new FieldErrorsException(Map.of("*", "座次没有全部落到部门"));
        }
        List<Placement> ordered = new ArrayList<>(bySeat.values());
        ordered.sort(Comparator.comparingInt((Placement row) -> depth(row.seat(), bySeat)).thenComparingInt(Placement::seat));
        return ordered;
    }

    private int depth(int seat, Map<Integer, Placement> bySeat) {
        int depth = 0;
        Integer manager = bySeat.get(seat).managerSeat();
        Set<Integer> seen = new HashSet<>();
        while (manager != null) {
            if (!seen.add(manager)) {
                throw new FieldErrorsException(Map.of("*", "汇报座次成环"));
            }
            depth++;
            manager = bySeat.get(manager).managerSeat();
        }
        return depth;
    }

    private Integer managerSeat(DeptSpec dept, List<DeptSpec> plan) {
        if (dept.headSeat() == 1) {
            return null;
        }
        if (GROUP_CODE.equals(dept.companyCode()) || "D-MGR".equals(dept.code())) {
            return 1;
        }
        for (DeptSpec item : plan) {
            if (item.companyCode().equals(dept.companyCode()) && "D-MGR".equals(item.code())) {
                return item.headSeat();
            }
        }
        throw new FieldErrorsException(Map.of("*", "分公司缺少经理室"));
    }

    private String title(DeptSpec dept) {
        if (dept.headSeat() == 1) {
            return "集团首席执行官";
        }
        if (dept.headSeat() == 3) {
            return "集团人事总监";
        }
        if (dept.headSeat() == 53) {
            return "信息技术经理";
        }
        if ("D-MGR".equals(dept.code())) {
            return "分公司经理";
        }
        if ("D-BIZ".equals(dept.code())) {
            return "业务部负责人";
        }
        if ("D-ADM".equals(dept.code())) {
            return "综合部负责人";
        }
        return dept.name() + "负责人";
    }

    private List<BranchSpec> branches() {
        return List.of(
                new BranchSpec("LS-JZ", "梁山集团济州分公司", "91370100MA3LS0002A",
                        "山东省济宁市梁山县济州大道 8 号", "济宁市梁山县市场监督管理局",
                        "622200000000000011", "济州支行", 5),
                new BranchSpec("LS-DM", "梁山集团大名分公司", "91370100MA3LS0003A",
                        "河北省邯郸市大名县梁山路 18 号", "邯郸市大名县市场监督管理局",
                        "622200000000000012", "大名支行", 6),
                new BranchSpec("LS-BJ", "梁山集团北京分公司", "91370100MA3LS0004A",
                        "北京市西城区梁山大街 28 号", "北京市西城区市场监督管理局",
                        "622200000000000013", "北京支行", 7),
                new BranchSpec("LS-JS", "梁山集团江州分公司", "91370100MA3LS0005A",
                        "江西省九江市浔阳区梁山路 38 号", "九江市浔阳区市场监督管理局",
                        "622200000000000014", "江州支行", 8));
    }

    private List<DeptSpec> departments() {
        return List.of(
                new DeptSpec(GROUP_CODE, "D-CEO", "总经办", "职能中心", 1, null),
                new DeptSpec(GROUP_CODE, "D-OPS", "运营部", "部门", 2, "D-CEO"),
                new DeptSpec(GROUP_CODE, "D-HR", "人事部", "职能中心", 3, "D-CEO"),
                new DeptSpec(GROUP_CODE, "D-IT", "信息技术部", "职能中心", 53, "D-CEO"),
                new DeptSpec(GROUP_CODE, "D-FIN", "财务管理部", "职能中心", 10, "D-CEO"),
                new DeptSpec("LS-JZ", "D-MGR", "经理室", "部门", 5, null),
                new DeptSpec("LS-JZ", "D-BIZ", "业务部", "部门", 13, "D-MGR"),
                new DeptSpec("LS-JZ", "D-ADM", "综合部", "部门", 14, "D-MGR"),
                new DeptSpec("LS-DM", "D-MGR", "经理室", "部门", 6, null),
                new DeptSpec("LS-DM", "D-BIZ", "业务部", "部门", 17, "D-MGR"),
                new DeptSpec("LS-DM", "D-ADM", "综合部", "部门", 18, "D-MGR"),
                new DeptSpec("LS-BJ", "D-MGR", "经理室", "部门", 7, null),
                new DeptSpec("LS-BJ", "D-BIZ", "业务部", "部门", 15, "D-MGR"),
                new DeptSpec("LS-BJ", "D-ADM", "综合部", "部门", 16, "D-MGR"),
                new DeptSpec("LS-JS", "D-MGR", "经理室", "部门", 8, null),
                new DeptSpec("LS-JS", "D-BIZ", "业务部", "部门", 19, "D-MGR"),
                new DeptSpec("LS-JS", "D-ADM", "综合部", "部门", 20, "D-MGR"));
    }

    private Company company(String code) {
        List<Company> found = companies.findByCompanyCode(code);
        return found.isEmpty() ? null : found.get(0);
    }

    private Company requiredCompany(String code) {
        Company found = company(code);
        if (found == null) {
            throw new FieldErrorsException(Map.of("*", "公司不存在：" + code));
        }
        return found;
    }

    private Employee requiredEmployee(int seat) {
        List<Employee> found = employees.findByEmployeeCode(LiangshanRoster.employeeCode(seat));
        if (found.isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "员工不存在：" + LiangshanRoster.employeeCode(seat)));
        }
        return found.get(0);
    }

    private Department requiredDepartment(String companyCode, String departmentCode) {
        Department found = departments.findByCompanyIdAndDepartmentCode(requiredCompany(companyCode).id, departmentCode)
                .stream().findFirst().orElse(null);
        if (found == null) {
            throw new FieldErrorsException(Map.of("*", "部门不存在：" + companyCode + " " + departmentCode));
        }
        return found;
    }

    private record BranchSpec(
            String code, String name, String uscc, String address, String authority,
            String account, String bankBranch, int managerSeat) {
    }

    private record DeptSpec(
            String companyCode, String code, String name, String type, int headSeat, String parentCode) {
    }

    private record Placement(
            int seat, String companyCode, String departmentCode, String title, Integer managerSeat) {
    }
}
