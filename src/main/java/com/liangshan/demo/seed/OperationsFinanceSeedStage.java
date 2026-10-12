package com.liangshan.demo.seed;

import com.liangshan.demo.domain.AccountingAccount;
import com.liangshan.demo.domain.AccountingPeriod;
import com.liangshan.demo.domain.AcceptanceEvent;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.ContractStageDeliverable;
import com.liangshan.demo.domain.ContractStageTermVersion;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.DeliveryEvent;
import com.liangshan.demo.domain.DeliveryLine;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.ExpenseClaim;
import com.liangshan.demo.domain.JournalEntry;
import com.liangshan.demo.domain.LaborCostRate;
import com.liangshan.demo.domain.Ledger;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.Payment;
import com.liangshan.demo.domain.PaymentAllocation;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ReceivableItem;
import com.liangshan.demo.domain.RevenueRecognition;
import com.liangshan.demo.domain.SourceDocument;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.domain.WorkLog;
import com.liangshan.demo.repository.AccountingAccountRepository;
import com.liangshan.demo.repository.AccountingPeriodRepository;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageDeliverableRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractStageTermVersionRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.DeliveryEventRepository;
import com.liangshan.demo.repository.DeliveryLineRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.ExpenseClaimRepository;
import com.liangshan.demo.repository.InvoiceRepository;
import com.liangshan.demo.repository.JournalEntryRepository;
import com.liangshan.demo.repository.LaborCostRateRepository;
import com.liangshan.demo.repository.LedgerRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.PaymentAllocationRepository;
import com.liangshan.demo.repository.PaymentRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.repository.RevenueRecognitionRepository;
import com.liangshan.demo.repository.SourceDocumentRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.repository.WorkLogRepository;
import com.liangshan.demo.service.CostOccurrenceService;
import com.liangshan.demo.service.ExpenseClaimService;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.FulfillmentService;
import com.liangshan.demo.service.FundsService;
import com.liangshan.demo.service.InvoiceService;
import com.liangshan.demo.service.JournalService;
import com.liangshan.demo.service.LaborCostRateService;
import com.liangshan.demo.service.LedgerService;
import com.liangshan.demo.service.RevenueRecognitionService;
import com.liangshan.demo.service.SourceDocumentService;
import com.liangshan.demo.service.WorkLogService;
import com.liangshan.demo.web.form.AcceptanceEventForm;
import com.liangshan.demo.web.form.AccountForm;
import com.liangshan.demo.web.form.CostForm;
import com.liangshan.demo.web.form.DeliveryEventForm;
import com.liangshan.demo.web.form.ExpenseClaimForm;
import com.liangshan.demo.web.form.InvoiceForm;
import com.liangshan.demo.web.form.JournalForm;
import com.liangshan.demo.web.form.LaborCostRateForm;
import com.liangshan.demo.web.form.LedgerForm;
import com.liangshan.demo.web.form.PayableForm;
import com.liangshan.demo.web.form.PaymentForm;
import com.liangshan.demo.web.form.PeriodForm;
import com.liangshan.demo.web.form.ReceivableForm;
import com.liangshan.demo.web.form.RevenueForm;
import com.liangshan.demo.web.form.SourceDocumentForm;
import com.liangshan.demo.web.form.WorkLogDecisionForm;
import com.liangshan.demo.web.form.WorkLogForm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OperationsFinanceSeedStage implements SeedStage {

    static final int YEAR = 2024;
    static final int FIRST_MONTH = 4;
    static final int SECOND_MONTH = 5;

    private static final LocalDate RATE_FROM = LocalDate.of(2024, 1, 1);
    private static final BigDecimal HOURLY = new BigDecimal("200.00");
    private static final String HQ = OrgHrSeedStage.GROUP_CODE;
    private static final String POLICY = "WP14";

    private final CompanyRepository companies;
    private final EmployeeRepository employees;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final PersonRepository persons;
    private final ProjectRepository projects;
    private final ContractRepository contracts;
    private final ContractStageRepository stages;
    private final ContractStageTermVersionRepository stageTerms;
    private final ContractStageDeliverableRepository deliverables;
    private final ContractTermVersionRepository terms;
    private final SupplierRepository suppliers;
    private final SourceDocumentRepository documents;
    private final SourceDocumentService sourceDocuments;
    private final WorkLogRepository workLogs;
    private final WorkLogService workLogService;
    private final LaborCostRateRepository rates;
    private final LaborCostRateService rateService;
    private final CostOccurrenceRepository costs;
    private final CostOccurrenceService costService;
    private final ExpenseClaimRepository claims;
    private final ExpenseClaimService claimService;
    private final DeliveryEventRepository deliveries;
    private final DeliveryLineRepository deliveryLines;
    private final AcceptanceEventRepository acceptances;
    private final FulfillmentService fulfillment;
    private final LedgerRepository ledgers;
    private final AccountingAccountRepository accounts;
    private final AccountingPeriodRepository periods;
    private final LedgerService ledgerService;
    private final ReceivableItemRepository receivables;
    private final PayableItemRepository payables;
    private final PaymentRepository payments;
    private final PaymentAllocationRepository allocations;
    private final FundsService funds;
    private final InvoiceRepository invoiceRecords;
    private final InvoiceService invoices;
    private final RevenueRecognitionRepository recognitions;
    private final RevenueRecognitionService revenueService;
    private final JournalEntryRepository journals;
    private final JournalService journalService;

    private final Map<String, Long> documentIds = new HashMap<>();
    private final Map<String, WorkLog> logsByDescription = new HashMap<>();

    public OperationsFinanceSeedStage(
            CompanyRepository companies,
            EmployeeRepository employees,
            EmployeeEmploymentRepository employments,
            EmployeeDepartmentAssignmentRepository assignments,
            PersonRepository persons,
            ProjectRepository projects,
            ContractRepository contracts,
            ContractStageRepository stages,
            ContractStageTermVersionRepository stageTerms,
            ContractStageDeliverableRepository deliverables,
            ContractTermVersionRepository terms,
            SupplierRepository suppliers,
            SourceDocumentRepository documents,
            SourceDocumentService sourceDocuments,
            WorkLogRepository workLogs,
            WorkLogService workLogService,
            LaborCostRateRepository rates,
            LaborCostRateService rateService,
            CostOccurrenceRepository costs,
            CostOccurrenceService costService,
            ExpenseClaimRepository claims,
            ExpenseClaimService claimService,
            DeliveryEventRepository deliveries,
            DeliveryLineRepository deliveryLines,
            AcceptanceEventRepository acceptances,
            FulfillmentService fulfillment,
            LedgerRepository ledgers,
            AccountingAccountRepository accounts,
            AccountingPeriodRepository periods,
            LedgerService ledgerService,
            ReceivableItemRepository receivables,
            PayableItemRepository payables,
            PaymentRepository payments,
            PaymentAllocationRepository allocations,
            FundsService funds,
            InvoiceRepository invoiceRecords,
            InvoiceService invoices,
            RevenueRecognitionRepository recognitions,
            RevenueRecognitionService revenueService,
            JournalEntryRepository journals,
            JournalService journalService) {
        this.companies = companies;
        this.employees = employees;
        this.employments = employments;
        this.assignments = assignments;
        this.persons = persons;
        this.projects = projects;
        this.contracts = contracts;
        this.stages = stages;
        this.stageTerms = stageTerms;
        this.deliverables = deliverables;
        this.terms = terms;
        this.suppliers = suppliers;
        this.documents = documents;
        this.sourceDocuments = sourceDocuments;
        this.workLogs = workLogs;
        this.workLogService = workLogService;
        this.rates = rates;
        this.rateService = rateService;
        this.costs = costs;
        this.costService = costService;
        this.claims = claims;
        this.claimService = claimService;
        this.deliveries = deliveries;
        this.deliveryLines = deliveryLines;
        this.acceptances = acceptances;
        this.fulfillment = fulfillment;
        this.ledgers = ledgers;
        this.accounts = accounts;
        this.periods = periods;
        this.ledgerService = ledgerService;
        this.receivables = receivables;
        this.payables = payables;
        this.payments = payments;
        this.allocations = allocations;
        this.funds = funds;
        this.invoiceRecords = invoiceRecords;
        this.invoices = invoices;
        this.recognitions = recognitions;
        this.revenueService = revenueService;
        this.journals = journals;
        this.journalService = journalService;
    }

    @Override
    public String code() {
        return "wp14";
    }

    @Override
    public int order() {
        return 140;
    }

    @Override
    public String label() {
        return "经营与财务";
    }

    @Override
    public void run() {
        if (projects.findByProjectCode("PJ-W13-01").isEmpty()
                || employees.findByEmployeeCode(LiangshanRoster.employeeCode(1)).isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "请先载入组织与人事以及项目与采购种子"));
        }
        documentIds.clear();
        for (SourceDocument document : documents.findAll()) {
            if (document.documentNumber != null) {
                documentIds.put(document.documentNumber, document.id);
            }
        }
        logsByDescription.clear();
        for (WorkLog log : workLogs.findAll()) {
            if (log.workDescription != null) {
                logsByDescription.put(log.workDescription, log);
            }
        }
        long rateDocumentId = document("DOC-W14-RATE", "成本率", HQ, "标准人工成本率");
        long spendDocumentId = document("DOC-W14-SPEND", "费用单", HQ, "项目费用与体检垫付");
        long claimDocumentId = document("DOC-W14-CLAIM", "报销单", HQ, "报销审批依据");
        long payDocumentId = document("DOC-W14-PAY", "收付款", HQ, "收付款依据");
        long agencyDocumentId = document("DOC-W14-AGENCY", "代付说明", "WP13-BJC", "北京大兴府代东京留守司付款");

        for (Unit unit : units()) {
            ensureRate(unit, rateDocumentId);
            ensureLogs(unit);
            ensureClaims(unit, spendDocumentId, claimDocumentId);
        }
        ensurePartialAcceptance();
        for (Unit unit : units()) {
            long ledgerId = ensureLedger(unit);
            ensureAccounts(ledgerId);
            long april = ensurePeriod(ledgerId, FIRST_MONTH);
            long may = ensurePeriod(ledgerId, SECOND_MONTH);
            ensureReceivable(unit, FIRST_MONTH, payDocumentId);
            ensureReceivable(unit, SECOND_MONTH, payDocumentId);
            ensureSupplierPayable(unit, FIRST_MONTH, payDocumentId);
            ensureSupplierPayable(unit, SECOND_MONTH, payDocumentId);
            long aprilRevenue = ensureRevenue(unit, FIRST_MONTH, april, payDocumentId);
            long mayRevenue = ensureRevenue(unit, SECOND_MONTH, may, payDocumentId);
            ensureJournal(unit, ledgerId, april, FIRST_MONTH, aprilRevenue);
            ensureJournal(unit, ledgerId, may, SECOND_MONTH, mayRevenue);
        }
        ensureFundsStory(payDocumentId, agencyDocumentId);
        ensureInvoices(payDocumentId);
        JournalEntry original = requiredJournal(HQ, FIRST_MONTH);
        if (journals.findByReversalOfEntryId(original.id).stream().noneMatch(row -> Codes.JOURNAL_POSTED.equals(row.status))) {
            journalService.reverse(original.id, staff(1).employeeId(), "四月收入重分类，冲销原凭证");
        }
    }

    private void ensureRate(Unit unit, long documentId) {
        Staff filer = staff(unit.filerSeat());
        for (LaborCostRate rate : rates.findByEmploymentIdAndCostBasisOrderByRateVersionNumberAsc(
                filer.employmentId(), Codes.LABOR_BASIS_STANDARD)) {
            if (Codes.RATE_APPROVED.equals(rate.approvalStatus) && rate.validTo == null) {
                return;
            }
        }
        LaborCostRateForm form = new LaborCostRateForm();
        form.setEmploymentId(filer.employmentId());
        form.setCostBasis(Codes.LABOR_BASIS_STANDARD);
        form.setHourlyCostAmount(HOURLY);
        form.setCurrency("CNY");
        form.setValidFrom(RATE_FROM);
        form.setApprovalStatus(Codes.RATE_APPROVED);
        form.setApprovedByEmployeeId(staff(1).employeeId());
        form.setApprovedAt(LocalDateTime.of(2024, 1, 5, 9, 0));
        form.setBasisDocumentId(documentId);
        rateService.save(form);
    }

    private void ensureLogs(Unit unit) {
        for (int month : new int[] {FIRST_MONTH, SECOND_MONTH}) {
            for (LocalDate day : weekdays(YEAR, month)) {
                ensureLog(unit, day, "8", "WP14|" + unit.code() + "|" + day, true, null);
            }
            LocalDate saturday = firstSaturday(YEAR, month);
            ensureLog(unit, saturday, "2", "WP14-REJ|" + unit.code() + "|" + saturday, false, "工时说明与当日任务不符");
        }
    }

    private void ensureLog(Unit unit, LocalDate day, String hours, String description, boolean approve, String reason) {
        Staff filer = staff(unit.filerSeat());
        WorkLog existing = logsByDescription.get(description);
        if (existing == null) {
            WorkLogForm form = new WorkLogForm();
            form.setEmployeeId(filer.employeeId());
            form.setEmploymentId(filer.employmentId());
            form.setDepartmentAssignmentId(filer.assignmentId());
            form.setProjectId(requiredProject(unit.laborProject()).id);
            form.setWorkDate(day);
            form.setHours(new BigDecimal(hours));
            form.setWorkDescription(description);
            form.setBillable(approve);
            form.setSubmitForApproval(true);
            long id = workLogService.save(form);
            existing = workLogs.findById(id).orElseThrow();
            logsByDescription.put(description, existing);
        }
        WorkLogDecisionForm decision = decision(staff(unit.managerSeat()).employeeId(), day.atTime(18, 0), reason);
        if (approve) {
            if (Codes.WORK_APPROVED.equals(existing.approvalStatus)) {
                return;
            }
            if (Codes.WORK_DRAFT.equals(existing.approvalStatus)) {
                WorkLogForm again = new WorkLogForm();
                again.setId(existing.id);
                again.setEmployeeId(filer.employeeId());
                again.setEmploymentId(filer.employmentId());
                again.setDepartmentAssignmentId(filer.assignmentId());
                again.setProjectId(requiredProject(unit.laborProject()).id);
                again.setWorkDate(day);
                again.setHours(new BigDecimal(hours));
                again.setWorkDescription(description);
                again.setSubmitForApproval(true);
                workLogService.save(again);
            }
            if (!Codes.WORK_REJECTED.equals(existing.approvalStatus)) {
                workLogService.approve(existing.id, decision);
            }
            return;
        }
        if (Codes.WORK_REJECTED.equals(existing.approvalStatus) || Codes.WORK_APPROVED.equals(existing.approvalStatus)) {
            return;
        }
        workLogService.reject(existing.id, decision);
    }

    private void ensureClaims(Unit unit, long spendDocumentId, long claimDocumentId) {
        long projectCost = ensureAdvancedCost(unit, "COST-W14-PJ-" + unit.code(), "差旅", "1200.00",
                LocalDate.of(YEAR, FIRST_MONTH, 8), spendDocumentId, unit.laborProject());
        settleClaim("CL-W14-PJ-" + unit.code(), Codes.CLAIM_PROJECT, unit, projectCost, "1200.00",
                claimDocumentId, LocalDate.of(YEAR, FIRST_MONTH, 12), unit.payProjectClaim(), false, null);
        long examCost = ensureAdvancedCost(unit, "COST-W14-EXAM-" + unit.code(), "体检", "500.00",
                LocalDate.of(YEAR, SECOND_MONTH, 8), spendDocumentId, unit.laborProject());
        settleClaim("CL-W14-EXAM-" + unit.code(), Codes.CLAIM_EXAM, unit, examCost, "500.00",
                claimDocumentId, LocalDate.of(YEAR, SECOND_MONTH, 12), unit.payExamClaim(), false, null);
        if (HQ.equals(unit.code())) {
            long rejectedCost = ensureAdvancedCost(unit, "COST-W14-REJ-HQ", "差旅", "300.00",
                    LocalDate.of(YEAR, FIRST_MONTH, 9), spendDocumentId, unit.laborProject());
            settleClaim("CL-W14-REJ-HQ", Codes.CLAIM_PROJECT, unit, rejectedCost, "300.00",
                    claimDocumentId, LocalDate.of(YEAR, FIRST_MONTH, 13), false, true, "票据与行程不符");
        }
    }

    private long ensureAdvancedCost(
            Unit unit, String number, String type, String amount, LocalDate day, long documentId, String projectCode) {
        List<CostOccurrence> found = costs.findByCostNumber(number);
        if (!found.isEmpty()) {
            return found.get(0).id;
        }
        Staff filer = staff(unit.filerSeat());
        CostForm form = new CostForm();
        form.setBearingCompanyId(requiredCompany(unit.code()).id);
        form.setIncurredByEmployeeId(filer.employeeId());
        form.setCostNumber(number);
        form.setCostType(type);
        form.setIncurredAt(day.atTime(10, 0));
        form.setCostAmount(new BigDecimal(amount));
        form.setEmployeeAdvancedAmount(new BigDecimal(amount));
        form.setCurrency("CNY");
        form.setCostBasis(Codes.COST_BASIS_ACTUAL);
        form.setDescription(number);
        form.setApprovalStatus(Codes.RATE_APPROVED);
        form.setApprovedByEmployeeId(staff(unit.managerSeat()).employeeId());
        form.setApprovedAt(day.atTime(16, 0));
        form.setStatus(Codes.COST_EFFECTIVE);
        form.setSourceDocumentId(documentId);
        CostForm.AllocationRow allocation = new CostForm.AllocationRow();
        allocation.setProjectId(requiredProject(projectCode).id);
        allocation.setAllocatedAmount(new BigDecimal(amount));
        allocation.setCurrency("CNY");
        allocation.setAllocationPeriod(day.toString().substring(0, 7));
        allocation.setAllocationMethod("直接指定");
        allocation.setAllocatedByEmployeeId(staff(unit.managerSeat()).employeeId());
        allocation.setAllocatedAt(day.atTime(16, 0));
        allocation.setStatus(Codes.ALLOCATION_EFFECTIVE);
        form.getAllocations().add(allocation);
        return costService.save(form);
    }

    private void settleClaim(
            String number, String type, Unit unit, long costId, String amount, long documentId, LocalDate day,
            boolean pay, boolean reject, String reason) {
        ExpenseClaim claim = claims.findByClaimNumber(number).stream().findFirst().orElse(null);
        if (claim == null) {
            ExpenseClaimForm form = new ExpenseClaimForm();
            form.setClaimNumber(number);
            form.setClaimType(type);
            form.setClaimantEmployeeId(staff(unit.filerSeat()).employeeId());
            form.setReimbursementCompanyId(requiredCompany(unit.code()).id);
            form.setCurrency("CNY");
            form.setSourceDocumentId(documentId);
            form.setSubmitForApproval(true);
            ExpenseClaimForm.LineRow line = new ExpenseClaimForm.LineRow();
            line.setLineNumber(1);
            line.setCostOccurrenceId(costId);
            line.setClaimedReimbursementAmount(new BigDecimal(amount));
            form.getLines().add(line);
            long id = claimService.save(form);
            claim = claims.findById(id).orElseThrow();
        }
        WorkLogDecisionForm decision = decision(staff(unit.managerSeat()).employeeId(), day.atTime(17, 0), reason);
        if (reject) {
            if (Codes.CLAIM_PENDING.equals(claim.approvalStatus)) {
                claimService.reject(claim.id, decision);
            }
            return;
        }
        if (Codes.CLAIM_PENDING.equals(claim.approvalStatus)) {
            claimService.approve(claim.id, decision);
            claim = claims.findById(claim.id).orElseThrow();
        }
        if (pay && Codes.CLAIM_APPROVED.equals(claim.approvalStatus) && payments.findByPaymentNumber("PAY-" + number).isEmpty()) {
            claimService.payApprovedClaim(claim.id, staff(1).employeeId());
        }
    }

    private void ensurePartialAcceptance() {
        partial(
                "CT-W13-SALE-TK", "PJ-W13-06", "S2", "LS-JZ", null, 5,
                "WP13-TK", "suyuanjing@wp13.liangshan.test", null,
                "DV-W14-TK", "20",
                "AC-W14-TK-1", "20", "12", "8", "甲仗尺寸不符", LocalDate.of(YEAR, FIRST_MONTH, 25),
                "AC-W14-TK-2", "8", "8", "0", null, LocalDate.of(YEAR, SECOND_MONTH, 10));
        partial(
                "CT-W13-BUY-JZ", "PJ-W13-09", "S1", "WP13-ZHU", null, null,
                "LS-JZ", null, 5,
                "DV-W14-JZ", "100",
                "AC-W14-JZ-1", "100", "70", "30", "刀刃缺口", LocalDate.of(YEAR, FIRST_MONTH, 22),
                "AC-W14-JZ-2", "30", "30", "0", null, LocalDate.of(YEAR, SECOND_MONTH, 12));
    }

    private void partial(
            String contractNumber, String projectCode, String stageCode,
            String submitterCode, String submitterEmail, Integer submitterSeat,
            String acceptorCode, String acceptorEmail, Integer acceptorSeat,
            String deliveryNumber, String quantity,
            String firstNumber, String firstReviewed, String firstAccepted, String firstRejected, String firstReason, LocalDate firstDay,
            String secondNumber, String secondReviewed, String secondAccepted, String secondRejected, String secondReason, LocalDate secondDay) {
        Contract contract = requiredContract(contractNumber);
        DeliveryEvent delivery = deliveryByNumber(deliveryNumber);
        if (delivery == null) {
            ContractStageDeliverable item = openDeliverable(contract.id, stageCode);
            long documentId = document("DOC-W14-" + deliveryNumber, "交付单", submitterCode, deliveryNumber);
            DeliveryEventForm form = new DeliveryEventForm();
            form.setProjectId(requiredProject(projectCode).id);
            form.setDeliveryNumber(deliveryNumber);
            form.setSubmittedAt(firstDay.minusDays(5).atTime(9, 0));
            form.setDeliverableDescription(deliveryNumber);
            form.setSubmitterCompanyId(requiredCompany(submitterCode).id);
            if (submitterEmail != null) {
                form.setSubmitterPersonId(personByEmail(submitterEmail));
            }
            if (submitterSeat != null) {
                form.setSubmittedByEmployeeId(staff(submitterSeat).employeeId());
            }
            form.setSourceDocumentId(documentId);
            DeliveryEventForm.Line line = new DeliveryEventForm.Line();
            line.setStageDeliverableId(item.id);
            line.setSubmittedQuantity(new BigDecimal(quantity));
            line.setUnit(item.unit);
            form.getLines().add(line);
            long id = fulfillment.recordDelivery(contract.id, form);
            delivery = deliveries.findById(id).orElseThrow();
        }
        long lineId = deliveryLines.findByDeliveryEventIdOrderByIdAsc(delivery.id).get(0).id;
        recordAcceptance(contract.id, delivery.id, lineId, acceptorCode, acceptorEmail, acceptorSeat,
                firstNumber, firstReviewed, firstAccepted, firstRejected, firstReason, firstDay);
        recordAcceptance(contract.id, delivery.id, lineId, acceptorCode, acceptorEmail, acceptorSeat,
                secondNumber, secondReviewed, secondAccepted, secondRejected, secondReason, secondDay);
    }

    private void recordAcceptance(
            long contractId, long deliveryId, long lineId, String acceptorCode, String acceptorEmail, Integer acceptorSeat,
            String number, String reviewed, String accepted, String rejected, String reason, LocalDate day) {
        if (acceptanceByNumber(number) != null) {
            return;
        }
        long documentId = document("DOC-W14-" + number, "验收单", acceptorCode, number);
        AcceptanceEventForm form = new AcceptanceEventForm();
        form.setDeliveryEventId(deliveryId);
        form.setAcceptanceNumber(number);
        form.setAcceptedAt(day.atTime(15, 0));
        form.setAcceptorCompanyId(requiredCompany(acceptorCode).id);
        if (acceptorEmail != null) {
            form.setAcceptedByPersonId(personByEmail(acceptorEmail));
        }
        if (acceptorSeat != null) {
            form.setAcceptedByEmployeeId(staff(acceptorSeat).employeeId());
        }
        form.setAcceptanceComments(number);
        form.setSourceDocumentId(documentId);
        AcceptanceEventForm.Line line = new AcceptanceEventForm.Line();
        line.setDeliveryLineId(lineId);
        line.setReviewedQuantity(new BigDecimal(reviewed));
        line.setAcceptedQuantity(new BigDecimal(accepted));
        line.setRejectedQuantity(new BigDecimal(rejected));
        line.setRejectionReason(reason);
        form.getLines().add(line);
        fulfillment.recordAcceptance(contractId, form);
    }

    private long ensureLedger(Unit unit) {
        String code = "LDG-W14-" + unit.suffix();
        for (Ledger ledger : ledgers.findByLedgerCode(code)) {
            return ledger.id;
        }
        Company company = requiredCompany(unit.code());
        LedgerForm form = new LedgerForm();
        form.setLegalPersonId(requiredCompany(HQ).legalPersonId);
        form.setScopeCompanyId(company.id);
        form.setLedgerCode(code);
        form.setName(company.name + "账簿");
        form.setLedgerType("公司账簿");
        form.setBaseCurrency("CNY");
        form.setStatus(Codes.LEDGER_ACTIVE);
        return ledgerService.saveLedger(form);
    }

    private void ensureAccounts(long ledgerId) {
        account(ledgerId, "1122", "应收账款", Codes.ACCOUNT_ASSET, Codes.BALANCE_DEBIT);
        account(ledgerId, "2202", "应付账款", Codes.ACCOUNT_LIABILITY, Codes.BALANCE_CREDIT);
        account(ledgerId, "6001", "主营业务收入", Codes.ACCOUNT_INCOME, Codes.BALANCE_CREDIT);
        account(ledgerId, "6401", "主营业务成本", Codes.ACCOUNT_EXPENSE, Codes.BALANCE_DEBIT);
    }

    private void account(long ledgerId, String code, String name, String category, String direction) {
        for (AccountingAccount account : accounts.findByLedgerIdOrderByAccountCodeAsc(ledgerId)) {
            if (code.equals(account.accountCode)) {
                return;
            }
        }
        AccountForm form = new AccountForm();
        form.setLedgerId(ledgerId);
        form.setAccountCode(code);
        form.setAccountName(name);
        form.setAccountCategory(category);
        form.setNormalBalanceDirection(direction);
        form.setPostable(true);
        form.setValidFrom(RATE_FROM);
        form.setStatus(Codes.LEDGER_ACTIVE);
        ledgerService.saveAccount(form);
    }

    private long ensurePeriod(long ledgerId, int month) {
        for (AccountingPeriod period : periods.findByLedgerIdOrderByPeriodStartDateAsc(ledgerId)) {
            if (period.fiscalYear != null && period.fiscalYear == YEAR && period.periodNumber != null && period.periodNumber == month) {
                return period.id;
            }
        }
        YearMonth value = YearMonth.of(YEAR, month);
        PeriodForm form = new PeriodForm();
        form.setLedgerId(ledgerId);
        form.setFiscalYear(YEAR);
        form.setPeriodNumber(month);
        form.setPeriodStartDate(value.atDay(1));
        form.setPeriodEndDate(value.atEndOfMonth());
        form.setPeriodStatus(Codes.PERIOD_OPEN);
        return ledgerService.savePeriod(form);
    }

    private void ensureReceivable(Unit unit, int month, long documentId) {
        String number = "AR-W14-" + unit.suffix() + "-" + month;
        if (!receivables.findByReceivableNumber(number).isEmpty()) {
            return;
        }
        ReceivableForm form = new ReceivableForm();
        form.setReceivableNumber(number);
        form.setCreditorCompanyId(requiredCompany(unit.code()).id);
        form.setDebtorCompanyId(requiredCompany(unit.receivableDebtor()).id);
        form.setReceivablePurpose("阶段款");
        form.setReceivableAmount(money(unit.receivable(month)));
        form.setCurrency("CNY");
        form.setStatus(Codes.PAYABLE_CONFIRMED);
        form.setConfirmedByEmployeeId(staff(1).employeeId());
        form.setConfirmedAt(LocalDate.of(YEAR, month, 10).atTime(9, 0));
        form.setSourceDocumentId(documentId);
        funds.saveReceivable(form);
    }

    private void ensureSupplierPayable(Unit unit, int month, long documentId) {
        String number = "AP-W14-" + unit.suffix() + "-" + month;
        if (!payables.findByPayableNumber(number).isEmpty()) {
            return;
        }
        Company buyer = requiredCompany(unit.code());
        Supplier supplier = requiredSupplier(buyer.id, unit.supplierCode());
        PayableForm form = new PayableForm();
        form.setPayableNumber(number);
        form.setDebtorCompanyId(buyer.id);
        form.setCreditorCompanyId(supplier.companyId);
        form.setSupplierId(supplier.id);
        form.setPayablePurpose("采购款");
        form.setPayableAmount(money(month == FIRST_MONTH ? "10000.00" : "8000.00"));
        form.setCurrency("CNY");
        form.setStatus(Codes.PAYABLE_CONFIRMED);
        form.setConfirmedByEmployeeId(staff(1).employeeId());
        form.setConfirmedAt(LocalDate.of(YEAR, month, 16).atTime(9, 0));
        form.setSourceDocumentId(documentId);
        funds.savePayable(form);
    }

    private long ensureRevenue(Unit unit, int month, long periodId, long documentId) {
        LocalDate day = LocalDate.of(YEAR, month, 20);
        long ledgerId = requiredLedger(unit);
        for (RevenueRecognition row : recognitions.findAll()) {
            if (ledgerId == row.ledgerId && day.equals(row.recognitionDate) && POLICY.equals(row.revenuePolicyCode)) {
                return row.id;
            }
        }
        Contract contract = requiredContract(unit.revenueContract());
        RevenueForm form = new RevenueForm();
        form.setLedgerId(ledgerId);
        form.setContractId(contract.id);
        form.setContractTermVersionId(openTerm(contract.id).id);
        form.setProjectId(requiredProject(unit.revenueProject()).id);
        form.setContractStageId(stage(contract.id, unit.revenueStage()).id);
        form.setRecognitionDate(day);
        form.setAccountingPeriodId(periodId);
        form.setRecognizedRevenueAmount(money(unit.revenue(month)));
        form.setCurrency("CNY");
        form.setRevenuePolicyCode(POLICY);
        form.setRevenuePolicyVersion("1");
        form.setSourceDocumentId(documentId);
        form.setApprovedByEmployeeId(staff(1).employeeId());
        form.setApprovedAt(day.atTime(11, 0));
        form.setStatus(Codes.REVENUE_APPROVED);
        return revenueService.save(form);
    }

    private void ensureJournal(Unit unit, long ledgerId, long periodId, int month, long recognitionId) {
        String number = "JE-W14-" + unit.suffix() + "-" + month;
        JournalEntry existing = journals.findByLedgerIdAndAccountingPeriodIdAndVoucherNumber(ledgerId, periodId, number)
                .stream().findFirst().orElse(null);
        if (existing != null && Codes.JOURNAL_POSTED.equals(existing.status)) {
            return;
        }
        RevenueRecognition recognition = recognitions.findById(recognitionId).orElseThrow();
        long id;
        if (existing == null) {
            JournalForm form = new JournalForm();
            form.setLedgerId(ledgerId);
            form.setAccountingPeriodId(periodId);
            form.setVoucherNumber(number);
            form.setVoucherDate(LocalDate.of(YEAR, month, 20));
            form.setEntryDescription(unit.code() + " " + month + "月确认收入");
            form.setPreparedByEmployeeId(staff(10).employeeId());
            form.getLines().add(line(1, accountId(ledgerId, "1122"), recognition.recognizedRevenueAmount, null,
                    requiredCompany(unit.receivableDebtor()).id));
            form.getLines().add(line(2, accountId(ledgerId, "6001"), null, recognition.recognizedRevenueAmount, null));
            JournalForm.SourceRow source = new JournalForm.SourceRow();
            source.setLineNumber(2);
            source.setRevenueRecognitionId(recognitionId);
            source.setSourceContributionAmount(recognition.recognizedRevenueAmount);
            source.setCurrency("CNY");
            form.getSources().add(source);
            id = journalService.save(form);
        } else {
            id = existing.id;
        }
        journalService.post(id, staff(1).employeeId());
    }

    private void ensureFundsStory(long payDocumentId, long agencyDocumentId) {
        ReceivableItem receivable = receivables.findByReceivableNumber("AR-W14-HQ-4").get(0);
        long song = staff(1).employeeId();
        Company headquarters = requiredCompany(HQ);
        Company tokyo = requiredCompany("WP13-TK");
        Company daxing = requiredCompany("WP13-BJC");
        LocalDateTime april = LocalDate.of(YEAR, FIRST_MONTH, 18).atTime(10, 0);
        if (payments.findByPaymentNumber("PAY-W14-TK-30").isEmpty()) {
            funds.savePayment(receipt("PAY-W14-TK-30", tokyo.id, headquarters.id, null, null,
                    "15000.00", song, april, payDocumentId, receivable.id, null, null));
        }
        Payment receipt = payments.findByPaymentNumber("PAY-W14-TK-30").get(0);
        PaymentAllocation applied = allocations.findByPaymentIdOrderByIdAsc(receipt.id).get(0);
        if (payments.findByPaymentNumber("PAY-W14-TK-REF").isEmpty()) {
            PaymentForm refund = receipt("PAY-W14-TK-REF", headquarters.id, tokyo.id, null, null,
                    "5000.00", song, april.plusDays(2), payDocumentId, receivable.id, applied.id, Codes.SETTLEMENT_REVERSE);
            refund.setPaymentKind(Codes.PAYMENT_REFUND);
            refund.setRefundOfPaymentId(receipt.id);
            funds.savePayment(refund);
        }
        if (payments.findByPaymentNumber("PAY-W14-TK-3RD").isEmpty()) {
            PaymentForm third = receipt("PAY-W14-TK-3RD", daxing.id, headquarters.id, tokyo.id, agencyDocumentId,
                    "20000.00", song, april.plusDays(4), payDocumentId, receivable.id, null, null);
            funds.savePayment(third);
        }
        PayableItem jizhouPayable = payables.findByPayableNumber("AP-W14-JZ-4").get(0);
        if (payments.findByPaymentNumber("PAY-W14-JZ-40").isEmpty()) {
            Supplier supplier = requiredSupplier(requiredCompany("LS-JZ").id, "SUP-W13-ZHU-JZ");
            PaymentForm payment = receipt("PAY-W14-JZ-40", requiredCompany("LS-JZ").id, supplier.companyId, null, null,
                    "4000.00", song, april.plusDays(1), payDocumentId, null, null, null);
            payment.getAllocations().clear();
            PaymentForm.AllocationRow row = new PaymentForm.AllocationRow();
            row.setPayableItemId(jizhouPayable.id);
            row.setSettlementAmount(money("4000.00"));
            row.setCurrency("CNY");
            row.setSettlementAction(Codes.SETTLEMENT_APPLY);
            row.setAllocatedAt(april.plusDays(1));
            row.setAllocatedByEmployeeId(song);
            row.setStatus(Codes.PAYMENT_CONFIRMED);
            payment.getAllocations().add(row);
            funds.savePayment(payment);
        }
    }

    private PaymentForm receipt(
            String number, long payerId, long payeeId, Long paymentForId, Long agencyId, String amount, long clerk,
            LocalDateTime paidAt, long documentId, Long receivableId, Long reversesId, String action) {
        PaymentForm form = new PaymentForm();
        form.setPayerCompanyId(payerId);
        form.setPayeeCompanyId(payeeId);
        form.setPaymentForCompanyId(paymentForId);
        form.setAgencyBasisDocumentId(agencyId);
        form.setPaymentNumber(number);
        form.setPaymentKind(Codes.PAYMENT_NORMAL);
        form.setPaymentPurpose("合同款");
        form.setPaidAt(paidAt);
        form.setPaymentAmount(money(amount));
        form.setCurrency("CNY");
        form.setPaymentChannel("银行转账");
        form.setConfirmedByEmployeeId(clerk);
        form.setConfirmedAt(paidAt);
        form.setSourceDocumentId(documentId);
        form.setStatus(Codes.PAYMENT_CONFIRMED);
        if (receivableId != null) {
            PaymentForm.AllocationRow row = new PaymentForm.AllocationRow();
            row.setReceivableItemId(receivableId);
            row.setSettlementAmount(money(amount));
            row.setCurrency("CNY");
            row.setSettlementAction(action == null ? Codes.SETTLEMENT_APPLY : action);
            row.setReversesAllocationId(reversesId);
            row.setAllocatedAt(paidAt);
            row.setAllocatedByEmployeeId(clerk);
            row.setStatus(Codes.PAYMENT_CONFIRMED);
            form.getAllocations().add(row);
        }
        return form;
    }

    private void ensureInvoices(long documentId) {
        ReceivableItem hq = receivables.findByReceivableNumber("AR-W14-HQ-4").get(0);
        invoice("INV-W14-HQ-04", HQ, "WP13-TK", "20000.00", "2600.00", "22600.00", "0.13",
                LocalDate.of(YEAR, FIRST_MONTH, 21), documentId, hq.id, null);
        ReceivableItem bj = receivables.findByReceivableNumber("AR-W14-BJ-5").get(0);
        invoice("INV-W14-BJ-05", "LS-BJ", "WP13-BJC", "8000.00", "1040.00", "9040.00", "0.13",
                LocalDate.of(YEAR, SECOND_MONTH, 21), documentId, bj.id, null);
        PayableItem stone = payables.findByPayableNumber("AP-W14-HQ-4").get(0);
        invoice("INV-W14-ZHU-04", "WP13-ZHU", HQ, "5000.00", "0.00", "5000.00", null,
                LocalDate.of(YEAR, FIRST_MONTH, 17), documentId, null, stone.id);
    }

    private void invoice(
            String number, String issuerCode, String recipientCode, String net, String tax, String gross, String rate,
            LocalDate issuedAt, long documentId, Long receivableId, Long payableId) {
        if (!invoiceRecords.findByInvoiceNumber(number).isEmpty()) {
            return;
        }
        Company issuer = requiredCompany(issuerCode);
        Company recipient = requiredCompany(recipientCode);
        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber(number);
        form.setInvoiceType("增值税普通发票");
        form.setInvoiceMedium("电子");
        form.setInvoiceColor("蓝字");
        form.setInvoiceNetAmount(money(net));
        form.setInvoiceTaxAmount(money(tax));
        form.setInvoiceGrossAmount(money(gross));
        form.setCurrency("CNY");
        form.setStatus(Codes.INVOICE_ISSUED);
        form.setIssuedAt(issuedAt);
        form.setIssuerCompanyId(issuer.id);
        form.setRecipientCompanyId(recipient.id);
        form.setIssuerNameSnapshot(issuer.name);
        form.setIssuerTaxpayerIdSnapshot(uscc(issuerCode));
        form.setRecipientNameSnapshot(recipient.name);
        form.setRecipientTaxpayerIdSnapshot(uscc(recipientCode));
        form.setSourceDocumentId(documentId);
        InvoiceForm.LineRow line = new InvoiceForm.LineRow();
        line.setLineNumber(1);
        line.setItemName(number);
        line.setNetAmount(money(net));
        line.setTaxAmount(money(tax));
        line.setGrossAmount(money(gross));
        if (rate == null) {
            line.setTaxTreatment("免税");
        } else {
            line.setTaxTreatment("适用税率");
            line.setTaxRate(new BigDecimal(rate));
        }
        form.getLines().add(line);
        InvoiceForm.AllocationRow allocation = new InvoiceForm.AllocationRow();
        allocation.setLineNumber(1);
        allocation.setReceivableItemId(receivableId);
        allocation.setPayableItemId(payableId);
        allocation.setAllocatedAmount(money(gross));
        allocation.setCurrency("CNY");
        allocation.setAllocationBasis(Codes.ALLOCATION_GROSS);
        form.getAllocations().add(allocation);
        invoices.save(form);
    }

    private JournalForm.LineRow line(int number, long accountId, BigDecimal debit, BigDecimal credit, Long counterpartyId) {
        JournalForm.LineRow row = new JournalForm.LineRow();
        row.setLineNumber(number);
        row.setAccountingAccountId(accountId);
        row.setLineDescription("分录 " + number);
        row.setDebitAmount(debit == null ? BigDecimal.ZERO : debit);
        row.setCreditAmount(credit == null ? BigDecimal.ZERO : credit);
        row.setCurrency("CNY");
        row.setCounterpartyCompanyId(counterpartyId);
        return row;
    }

    private long accountId(long ledgerId, String code) {
        for (AccountingAccount account : accounts.findByLedgerIdOrderByAccountCodeAsc(ledgerId)) {
            if (code.equals(account.accountCode)) {
                return account.id;
            }
        }
        throw new FieldErrorsException(Map.of("*", "科目不存在：" + code));
    }

    private JournalEntry requiredJournal(String companyCode, int month) {
        Unit unit = units().stream().filter(row -> companyCode.equals(row.code())).findFirst().orElseThrow();
        long ledgerId = requiredLedger(unit);
        long periodId = ensurePeriod(ledgerId, month);
        return journals.findByLedgerIdAndAccountingPeriodIdAndVoucherNumber(ledgerId, periodId, "JE-W14-" + unit.suffix() + "-" + month)
                .get(0);
    }

    private long requiredLedger(Unit unit) {
        return ledgers.findByLedgerCode("LDG-W14-" + unit.suffix()).get(0).id;
    }

    private long document(String number, String type, String issuerCode, String summary) {
        Long existing = documentIds.get(number);
        if (existing != null) {
            return existing;
        }
        SourceDocumentForm form = new SourceDocumentForm();
        form.setDocumentType(type);
        form.setDocumentNumber(number);
        form.setIssuerCompanyId(requiredCompany(issuerCode).id);
        form.setPreparedByEmployeeId(staff(1).employeeId());
        form.setIssuedAt(LocalDate.of(YEAR, 1, 8));
        form.setContentSummary(summary);
        form.setStatus("有效");
        long id = sourceDocuments.save(form);
        documentIds.put(number, id);
        return id;
    }

    private WorkLogDecisionForm decision(long approverId, LocalDateTime at, String comment) {
        WorkLogDecisionForm form = new WorkLogDecisionForm();
        form.setApprovedByEmployeeId(approverId);
        form.setApprovedAt(at);
        form.setApprovalComment(comment);
        return form;
    }

    private ContractStageDeliverable openDeliverable(long contractId, String stageCode) {
        ContractStage stage = stage(contractId, stageCode);
        ContractStageTermVersion open = null;
        for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(stage.id)) {
            if (term.validTo == null) {
                open = term;
            }
        }
        if (open == null) {
            throw new FieldErrorsException(Map.of("*", "阶段没有当前条款"));
        }
        return deliverables.findByStageTermVersionIdOrderByIdAsc(open.id).get(0);
    }

    private ContractTermVersion openTerm(long contractId) {
        ContractTermVersion open = null;
        for (ContractTermVersion term : terms.findByContractIdOrderByVersionNumberAsc(contractId)) {
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
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            if (code.equals(stage.stageCode)) {
                return stage;
            }
        }
        throw new FieldErrorsException(Map.of("*", "合同阶段不存在：" + code));
    }

    private DeliveryEvent deliveryByNumber(String number) {
        for (DeliveryEvent event : deliveries.findAll()) {
            if (number.equals(event.deliveryNumber)) {
                return event;
            }
        }
        return null;
    }

    private AcceptanceEvent acceptanceByNumber(String number) {
        for (AcceptanceEvent event : acceptances.findAll()) {
            if (number.equals(event.acceptanceNumber)) {
                return event;
            }
        }
        return null;
    }

    private long personByEmail(String email) {
        for (Person person : persons.findAllByOrderByNameAsc()) {
            if (email.equals(person.email)) {
                return person.id;
            }
        }
        throw new FieldErrorsException(Map.of("*", "外部联系人不存在：" + email));
    }

    private Staff staff(int seat) {
        Employee employee = employees.findByEmployeeCode(LiangshanRoster.employeeCode(seat)).stream().findFirst().orElse(null);
        if (employee == null) {
            throw new FieldErrorsException(Map.of("*", "请先载入组织与人事种子"));
        }
        List<EmployeeEmployment> hires = employments.findByEmployeeIdOrderByIdAsc(employee.id);
        List<EmployeeDepartmentAssignment> posts = assignments.findByEmployeeIdOrderByIdAsc(employee.id);
        if (hires.isEmpty() || posts.isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "员工尚未任职：" + employee.employeeCode));
        }
        return new Staff(employee.id, hires.get(0).id, posts.get(0).id);
    }

    private Company requiredCompany(String code) {
        return companies.findByCompanyCode(code).stream().findFirst()
                .orElseThrow(() -> new FieldErrorsException(Map.of("*", "公司不存在：" + code)));
    }

    private Project requiredProject(String code) {
        return projects.findByProjectCode(code).stream().findFirst()
                .orElseThrow(() -> new FieldErrorsException(Map.of("*", "项目不存在：" + code)));
    }

    private Contract requiredContract(String number) {
        return contracts.findByContractNumber(number).stream().findFirst()
                .orElseThrow(() -> new FieldErrorsException(Map.of("*", "合同不存在：" + number)));
    }

    private Supplier requiredSupplier(long purchasingCompanyId, String code) {
        for (Supplier supplier : suppliers.findByPurchasingCompanyId(purchasingCompanyId)) {
            if (code.equals(supplier.supplierCode)) {
                return supplier;
            }
        }
        throw new FieldErrorsException(Map.of("*", "供应商档案不存在：" + code));
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }

    private static String uscc(String companyCode) {
        return switch (companyCode) {
            case HQ -> "91370100MA3LS0001A";
            case "LS-JZ" -> "91370100MA3LS0002A";
            case "LS-DM" -> "91370100MA3LS0003A";
            case "LS-BJ" -> "91370100MA3LS0004A";
            case "LS-JS" -> "91370100MA3LS0005A";
            case "WP13-TK" -> "91370100MA3WP1301A";
            case "WP13-BJC" -> "91370100MA3WP1302A";
            case "WP13-ZHU" -> "91370100MA3WP1303A";
            default -> throw new FieldErrorsException(Map.of("*", "缺少税号：" + companyCode));
        };
    }

    private static List<LocalDate> weekdays(int year, int month) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate day = LocalDate.of(year, month, 1);
        LocalDate end = day.plusMonths(1);
        while (day.isBefore(end)) {
            DayOfWeek dow = day.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days.add(day);
            }
            day = day.plusDays(1);
        }
        return days;
    }

    private static LocalDate firstSaturday(int year, int month) {
        LocalDate day = LocalDate.of(year, month, 1);
        while (day.getDayOfWeek() != DayOfWeek.SATURDAY) {
            day = day.plusDays(1);
        }
        return day;
    }

    private static List<Unit> units() {
        return List.of(
                new Unit(HQ, "HQ", 2, 1, "PJ-W13-01", "CT-W13-SALE-TK", "PJ-W13-06", "S2",
                        "WP13-TK", "SUP-W13-ZHU-HQ", "20000.00", "19500.00", "30000.00", "12000.00", true, false),
                new Unit("LS-JZ", "JZ", 13, 5, "PJ-W13-09", "CT-W13-BUY-JZ", "PJ-W13-09", "S1",
                        HQ, "SUP-W13-ZHU-JZ", "8000.00", "7500.00", "8000.00", "7000.00", true, false),
                new Unit("LS-DM", "DM", 17, 6, "PJ-W13-03", "CT-W13-BUY-DM", "PJ-W13-03", "S1",
                        HQ, "SUP-W13-ZENG", "6000.00", "5500.00", "6000.00", "5000.00", false, true),
                new Unit("LS-BJ", "BJ", 15, 7, "PJ-W13-04", "CT-W13-SALE-BJ", "PJ-W13-07", "S2",
                        "WP13-BJC", "SUP-W13-LI", "15000.00", "14000.00", "9000.00", "8000.00", false, true),
                new Unit("LS-JS", "JS", 19, 8, "PJ-W13-05", "CT-W13-BUY-JS", "PJ-W13-10", "S1",
                        HQ, "SUP-W13-SHI", "5000.00", "4500.00", "4000.00", "3500.00", false, false));
    }

    private record Staff(long employeeId, long employmentId, long assignmentId) {
    }

    private record Unit(
            String code, String suffix, int filerSeat, int managerSeat, String laborProject,
            String revenueContract, String revenueProject, String revenueStage, String receivableDebtor,
            String supplierCode, String revenueApril, String revenueMay, String receivableApril, String receivableMay,
            boolean payProjectClaim, boolean payExamClaim) {

        private String revenue(int month) {
            return month == FIRST_MONTH ? revenueApril : revenueMay;
        }

        private String receivable(int month) {
            return month == FIRST_MONTH ? receivableApril : receivableMay;
        }
    }
}
