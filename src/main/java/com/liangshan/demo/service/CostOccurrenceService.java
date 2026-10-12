package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.CostAllocation;
import com.liangshan.demo.domain.CostInvoiceLine;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.LaborCostRate;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ProjectContract;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.domain.WorkLog;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.CostAllocationRepository;
import com.liangshan.demo.repository.CostInvoiceLineRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.ExpenseClaimLineRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.repository.WorkLogRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.CostForm;
import com.liangshan.demo.web.form.CostForm.AllocationRow;
import com.liangshan.demo.web.form.CostForm.InvoiceRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CostOccurrenceService {

    private final CostOccurrenceRepository costs;
    private final CostAllocationRepository allocations;
    private final CostInvoiceLineRepository invoiceLines;
    private final ExpenseClaimLineRepository claimLines;
    private final PayableItemRepository payables;
    private final WorkLogRepository workLogs;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeRepository employees;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final SupplierRepository suppliers;
    private final ContractRepository contracts;
    private final ContractStageRepository stages;
    private final ProjectRepository projects;
    private final ProjectContractRepository projectContracts;
    private final SourceDocumentService documents;

    public record Row(
            Long id,
            String costNumber,
            String costType,
            String costBasis,
            BigDecimal costAmount,
            String currency,
            BigDecimal employeeAdvancedAmount,
            BigDecimal allocatedAmount,
            String approvalStatus,
            String status) {
    }

    public List<Row> list() {
        return costs.findAllByOrderByIncurredAtDescIdDesc().stream().map(this::row).toList();
    }

    public CostOccurrence get(Long id) {
        return costs.findById(id).orElseThrow(NotFoundException::new);
    }

    public CostOccurrence findEffectiveLabor(Long workLogId) {
        if (workLogId == null) {
            return null;
        }
        CostOccurrence chosen = null;
        for (CostOccurrence cost : costs.findByWorkLogIdOrderByIdAsc(workLogId)) {
            if (Codes.COST_EFFECTIVE.equals(cost.status)) {
                chosen = cost;
            }
        }
        return chosen;
    }

    public CostForm form(Long id) {
        CostOccurrence cost = get(id);
        CostForm form = new CostForm();
        form.setId(cost.id);
        form.setBearingCompanyId(cost.bearingCompanyId);
        form.setBearingLegalPersonId(cost.bearingLegalPersonId);
        form.setIncurredByEmployeeId(cost.incurredByEmployeeId);
        form.setSupplierCompanyId(cost.supplierCompanyId);
        form.setSupplierLegalPersonId(cost.supplierLegalPersonId);
        form.setSupplierPersonId(cost.supplierPersonId);
        form.setSupplierId(cost.supplierId);
        form.setCostNumber(cost.costNumber);
        form.setPurchaseContractId(cost.purchaseContractId);
        form.setExpenseStartDate(cost.expenseStartDate);
        form.setExpenseEndDate(cost.expenseEndDate);
        form.setExpenseNetAmount(cost.expenseNetAmount);
        form.setExpenseTaxAmount(cost.expenseTaxAmount);
        form.setExpenseGrossAmount(cost.expenseGrossAmount);
        form.setEmployeeAdvancedAmount(cost.employeeAdvancedAmount);
        form.setCostPolicyCode(cost.costPolicyCode);
        form.setCostPolicyVersion(cost.costPolicyVersion);
        form.setCostType(cost.costType);
        form.setIncurredAt(cost.incurredAt);
        form.setCostAmount(cost.costAmount);
        form.setCurrency(cost.currency);
        form.setCostBasis(cost.costBasis);
        form.setDescription(cost.description);
        form.setApprovalStatus(cost.approvalStatus);
        form.setApprovedByEmployeeId(cost.approvedByEmployeeId);
        form.setApprovedAt(cost.approvedAt);
        form.setStatus(cost.status);
        form.setSourceDocumentId(cost.sourceDocumentId);
        for (CostAllocation allocation : allocations.findByCostOccurrenceIdOrderByIdAsc(id)) {
            AllocationRow row = new AllocationRow();
            row.setId(allocation.id);
            row.setProjectId(allocation.projectId);
            row.setContractStageId(allocation.contractStageId);
            row.setAllocatedAmount(allocation.allocatedAmount);
            row.setCurrency(allocation.currency);
            row.setAllocationPeriod(allocation.allocationPeriod);
            row.setAllocationMethod(allocation.allocationMethod);
            row.setAllocationBasisHours(allocation.allocationBasisHours);
            row.setAllocationRatio(allocation.allocationRatio);
            row.setAllocatedByEmployeeId(allocation.allocatedByEmployeeId);
            row.setAllocatedAt(allocation.allocatedAt);
            row.setBasisDocumentId(allocation.basisDocumentId);
            row.setStatus(allocation.status);
            form.getAllocations().add(row);
        }
        for (CostInvoiceLine line : invoiceLines.findByCostOccurrenceIdOrderByIdAsc(id)) {
            InvoiceRow row = new InvoiceRow();
            row.setId(line.id);
            row.setInvoiceLineId(line.invoiceLineId);
            row.setMatchedExpenseGrossAmount(line.matchedExpenseGrossAmount);
            row.setCurrency(line.currency);
            form.getInvoiceLines().add(row);
        }
        return form;
    }

    public boolean laborGenerated(Long id) {
        CostOccurrence cost = costs.findById(id).orElse(null);
        return cost != null && cost.workLogId != null;
    }

    @Transactional
    public long save(CostForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        CostOccurrence stored = null;
        if (form.getId() != null) {
            stored = costs.findById(form.getId()).orElse(null);
            if (stored == null) {
                errors.add("id", "费用不存在");
            }
        }
        validate(form, stored, errors);
        errors.throwIfAny();
        CostOccurrence cost = stored == null ? new CostOccurrence() : stored;
        apply(cost, form);
        if (stored != null && stored.workLogId != null) {
            copyLaborIdentity(cost, stored);
        }
        cost = costs.save(cost);
        syncAllocations(cost, form.getAllocations());
        syncInvoices(cost, form.getInvoiceLines());
        return cost.id;
    }

    @Transactional
    public CostOccurrence recordApprovedLabor(WorkLog log, LaborCostRate rate) {
        if (log.supersedesWorkLogId != null) {
            for (CostOccurrence prior : costs.findByWorkLogIdOrderByIdAsc(log.supersedesWorkLogId)) {
                if (!Codes.COST_EFFECTIVE.equals(prior.status)) {
                    continue;
                }
                prior.status = Codes.COST_SUPERSEDED;
                costs.save(prior);
                for (CostAllocation allocation : allocations.findByCostOccurrenceIdOrderByIdAsc(prior.id)) {
                    if (Codes.ALLOCATION_EFFECTIVE.equals(allocation.status)) {
                        allocation.status = "已替代";
                        allocations.save(allocation);
                    }
                }
            }
        }
        BigDecimal already = BigDecimal.ZERO;
        for (CostOccurrence existing : costs.findByWorkLogIdOrderByIdAsc(log.id)) {
            if (Codes.COST_EFFECTIVE.equals(existing.status) && existing.costedHours != null) {
                already = already.add(existing.costedHours);
            }
        }
        if (already.add(log.hours).compareTo(log.hours) > 0) {
            throw new FieldErrorsException(java.util.Map.of("hours", "累计计入小时数不能超过有效已批工时"));
        }
        EmployeeEmployment employment = employments.findById(log.employmentId).orElseThrow();
        BigDecimal amount = LaborCostRateService.plain(
                log.hours.multiply(rate.hourlyCostAmount).setScale(2, RoundingMode.HALF_UP));
        CostOccurrence cost = new CostOccurrence();
        cost.bearingCompanyId = employment.employerCompanyId;
        cost.bearingLegalPersonId = employment.employerLegalPersonId;
        cost.incurredByEmployeeId = log.employeeId;
        cost.costNumber = "LC-" + log.id;
        cost.costType = "人工成本";
        cost.incurredAt = log.approvedAt == null ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES) : log.approvedAt;
        cost.costAmount = amount;
        cost.currency = rate.currency;
        cost.costBasis = Codes.COST_BASIS_STANDARD_LABOR;
        cost.workLogId = log.id;
        cost.laborCostRateId = rate.id;
        cost.costedHours = LaborCostRateService.plain(log.hours);
        cost.hourlyCostAmountSnapshot = LaborCostRateService.plain(rate.hourlyCostAmount);
        cost.costPolicyCode = rate.costBasis;
        cost.costPolicyVersion = rate.rateVersionNumber == null ? null : rate.rateVersionNumber.toString();
        cost.approvalStatus = Codes.RATE_APPROVED;
        cost.approvedByEmployeeId = log.approvedByEmployeeId;
        cost.approvedAt = log.approvedAt;
        cost.description = log.workDescription;
        cost.status = Codes.COST_EFFECTIVE;
        if (log.supersedesWorkLogId != null) {
            List<CostOccurrence> priors = costs.findByWorkLogIdOrderByIdAsc(log.supersedesWorkLogId);
            if (!priors.isEmpty()) {
                cost.supersedesCostOccurrenceId = priors.get(priors.size() - 1).id;
            }
        }
        cost = costs.save(cost);
        CostAllocation allocation = new CostAllocation();
        allocation.costOccurrenceId = cost.id;
        allocation.projectId = log.projectId;
        allocation.contractStageId = log.contractStageId;
        allocation.allocatedAmount = amount;
        allocation.currency = rate.currency;
        allocation.allocationPeriod = log.workDate.toString().substring(0, 7);
        allocation.allocationMethod = "按工时";
        allocation.allocationBasisHours = LaborCostRateService.plain(log.hours);
        allocation.allocatedByEmployeeId = log.approvedByEmployeeId;
        allocation.allocatedAt = cost.incurredAt;
        allocation.status = Codes.ALLOCATION_EFFECTIVE;
        allocations.save(allocation);
        return cost;
    }

    @Transactional
    public void delete(Long id) {
        CostOccurrence cost = get(id);
        if (cost.workLogId != null) {
            throw new FieldErrorsException(java.util.Map.of("*", "工时成本由批准生成，不能删除"));
        }
        if (!claimLines.findByCostOccurrenceId(id).isEmpty() || payables.existsByCostOccurrenceId(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该费用仍被报销或应付引用，不能删除"));
        }
        allocations.findByCostOccurrenceIdOrderByIdAsc(id).forEach(allocations::delete);
        invoiceLines.findByCostOccurrenceIdOrderByIdAsc(id).forEach(invoiceLines::delete);
        costs.delete(cost);
    }

    private void validate(CostForm form, CostOccurrence stored, FieldErrors errors) {
        int bearers = (form.getBearingCompanyId() == null ? 0 : 1) + (form.getBearingLegalPersonId() == null ? 0 : 1);
        if (bearers != 1) {
            errors.add("bearingCompanyId", "成本承担对象在公司和法人中恰好选一个");
        } else if (form.getBearingCompanyId() != null && companies.findById(form.getBearingCompanyId()).isEmpty()) {
            errors.add("bearingCompanyId", "承担公司不存在");
        } else if (form.getBearingLegalPersonId() != null && legalPersons.findById(form.getBearingLegalPersonId()).isEmpty()) {
            errors.add("bearingLegalPersonId", "承担法人不存在");
        }
        if (form.getIncurredByEmployeeId() != null && employees.findById(form.getIncurredByEmployeeId()).isEmpty()) {
            errors.add("incurredByEmployeeId", "经办员工不存在");
        }
        alignSupplier(form, errors);
        if (form.getCostNumber() == null) {
            errors.add("costNumber", "请填写费用编号");
        } else {
            for (CostOccurrence other : costs.findByCostNumber(form.getCostNumber())) {
                if (!other.id.equals(form.getId()) && !Codes.COST_VOID.equals(other.status)) {
                    errors.add("costNumber", "费用编号已存在");
                }
            }
        }
        if (form.getPurchaseContractId() != null && contracts.findById(form.getPurchaseContractId()).isEmpty()) {
            errors.add("purchaseContractId", "采购合同不存在");
        }
        errors.add("expenseEndDate", Periods.orderError(form.getExpenseStartDate(), form.getExpenseEndDate()));
        if (form.getExpenseNetAmount() != null && form.getExpenseTaxAmount() != null && form.getExpenseGrossAmount() != null
                && form.getExpenseGrossAmount().compareTo(form.getExpenseNetAmount().add(form.getExpenseTaxAmount())) != 0) {
            errors.add("expenseGrossAmount", "含税支出须等于不含税加税额");
        }
        if (form.getEmployeeAdvancedAmount() != null && form.getEmployeeAdvancedAmount().signum() < 0) {
            errors.add("employeeAdvancedAmount", "垫付金额不能为负");
        }
        if (form.getCostType() == null || !Codes.COST_TYPES.contains(form.getCostType())) {
            errors.add("costType", "请选择费用类型");
        }
        if (form.getIncurredAt() == null) {
            errors.add("incurredAt", "请填写发生时间");
        }
        if (form.getCostAmount() == null) {
            errors.add("costAmount", "请填写成本金额");
        } else if (form.getCostAmount().signum() <= 0) {
            errors.add("costAmount", "成本金额须大于 0");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        }
        if (form.getCostBasis() == null || !Codes.COST_BASIS.contains(form.getCostBasis())) {
            errors.add("costBasis", "请选择成本口径");
        }
        if (form.getApprovalStatus() == null || !Codes.COST_APPROVAL_STATUS.contains(form.getApprovalStatus())) {
            errors.add("approvalStatus", "请选择审批状态");
        } else if (Codes.RATE_APPROVED.equals(form.getApprovalStatus())) {
            if (form.getApprovedByEmployeeId() == null || employees.findById(form.getApprovedByEmployeeId()).isEmpty()) {
                errors.add("approvedByEmployeeId", "已批准须填写审批人");
            }
            if (form.getApprovedAt() == null) {
                errors.add("approvedAt", "已批准须填写审批时间");
            }
        } else if (form.getApprovedByEmployeeId() != null || form.getApprovedAt() != null) {
            errors.add("approvedByEmployeeId", "未批准时审批人和时间留空");
        }
        if (form.getStatus() == null || !Codes.COST_RECORD_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择记录状态");
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        if (stored != null && stored.workLogId != null && form.getCostAmount() != null
                && !Cmp.eq(stored.costAmount, form.getCostAmount())) {
            errors.add("costAmount", "工时成本金额来自批准时的单价，不能改");
        }
        if (stored != null && stored.workLogId != null && form.getCurrency() != null && !form.getCurrency().equals(stored.currency)) {
            errors.add("currency", "工时成本金额来自批准时的单价，不能改");
        }
        validatePayroll(form, errors);
        validateAllocations(form, stored, errors);
        validateInvoices(form, errors);
    }

    private void alignSupplier(CostForm form, FieldErrors errors) {
        if (form.getSupplierId() != null) {
            Supplier supplier = suppliers.findById(form.getSupplierId()).orElse(null);
            if (supplier == null) {
                errors.add("supplierId", "供应商档案不存在");
                return;
            }
            boolean blank = form.getSupplierCompanyId() == null && form.getSupplierLegalPersonId() == null
                    && form.getSupplierPersonId() == null;
            if (blank) {
                form.setSupplierCompanyId(supplier.companyId);
                form.setSupplierLegalPersonId(supplier.legalPersonId);
                form.setSupplierPersonId(supplier.personId);
            } else if (!Cmp.eq(form.getSupplierCompanyId(), supplier.companyId)
                    || !Cmp.eq(form.getSupplierLegalPersonId(), supplier.legalPersonId)
                    || !Cmp.eq(form.getSupplierPersonId(), supplier.personId)) {
                errors.add("supplierId", "供货对象须与供应商档案一致");
            }
        }
        int objects = (form.getSupplierCompanyId() == null ? 0 : 1)
                + (form.getSupplierLegalPersonId() == null ? 0 : 1)
                + (form.getSupplierPersonId() == null ? 0 : 1);
        if (objects > 1) {
            errors.add("supplierCompanyId", "供货对象最多选一个");
        }
    }

    private void validatePayroll(CostForm form, FieldErrors errors) {
        boolean any = form.getId() != null && costs.findById(form.getId()).map(cost ->
                cost.payrollEmployeeId != null || cost.payrollPeriod != null).orElse(false);
        if (!any) {
            return;
        }
        CostOccurrence stored = costs.findById(form.getId()).orElse(null);
        if (stored == null || stored.payrollEmployeeId == null) {
            return;
        }
        for (CostOccurrence other : costs.findByPayrollEmployeeIdAndPayrollEmploymentIdAndPayrollPeriodAndPayrollBatchNumberAndPayrollLineNumber(
                stored.payrollEmployeeId, stored.payrollEmploymentId, stored.payrollPeriod,
                stored.payrollBatchNumber, stored.payrollLineNumber)) {
            if (!other.id.equals(stored.id) && !Codes.COST_VOID.equals(other.status)) {
                errors.add("costNumber", "该工资来源已经导入");
            }
        }
    }

    private void validateAllocations(CostForm form, CostOccurrence stored, FieldErrors errors) {
        WorkLog labor = stored == null || stored.workLogId == null ? null : workLogs.findById(stored.workLogId).orElse(null);
        BigDecimal sum = BigDecimal.ZERO;
        int active = 0;
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < form.getAllocations().size(); i++) {
            AllocationRow row = form.getAllocations().get(i);
            if (row.isRemoved() || blankAllocation(row)) {
                continue;
            }
            String prefix = "allocations[" + i + "]";
            if (row.getProjectId() == null || projects.findById(row.getProjectId()).isEmpty()) {
                errors.add(prefix + ".projectId", "请选择分配项目");
            }
            if (row.getAllocatedAmount() == null || row.getAllocatedAmount().signum() <= 0) {
                errors.add(prefix + ".allocatedAmount", "分配金额须大于 0");
            }
            String currency = row.getCurrency() == null ? form.getCurrency() : row.getCurrency();
            if (form.getCurrency() != null && currency != null && !form.getCurrency().equals(currency)) {
                errors.add(prefix + ".currency", "分配币种须与成本一致");
            }
            if (row.getAllocationMethod() == null || !Codes.ALLOCATION_METHODS.contains(row.getAllocationMethod())) {
                errors.add(prefix + ".allocationMethod", "请选择分配方法");
            }
            if (row.getStatus() == null || !Codes.ALLOCATION_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择分配状态");
            }
            if (row.getContractStageId() != null) {
                if (!stageOnProject(row.getProjectId(), row.getContractStageId())) {
                    errors.add(prefix + ".contractStageId", "阶段的合同不在该项目中");
                }
            }
            if (labor != null && Codes.ALLOCATION_EFFECTIVE.equals(row.getStatus())) {
                if (!labor.projectId.equals(row.getProjectId())
                        || (labor.contractStageId == null && row.getContractStageId() != null)
                        || (labor.contractStageId != null && !labor.contractStageId.equals(row.getContractStageId()))) {
                    errors.add(prefix + ".projectId", "工时成本只能归集到该工时所在项目及阶段");
                }
            }
            documents.require(row.getBasisDocumentId(), errors, prefix + ".basisDocumentId");
            if (row.getId() != null && !seen.add(row.getId())) {
                errors.add(prefix, "分配记录重复");
            }
            if (Codes.ALLOCATION_EFFECTIVE.equals(row.getStatus()) && row.getAllocatedAmount() != null) {
                active++;
                sum = sum.add(row.getAllocatedAmount());
            }
        }
        if (active > 0 && form.getCostAmount() != null && sum.compareTo(form.getCostAmount()) != 0) {
            errors.add("allocations", "分配合计须等于成本金额");
        }
    }

    private void validateInvoices(CostForm form, FieldErrors errors) {
        Set<Long> lines = new HashSet<>();
        for (int i = 0; i < form.getInvoiceLines().size(); i++) {
            InvoiceRow row = form.getInvoiceLines().get(i);
            if (row.isRemoved() || (row.getId() == null && row.getInvoiceLineId() == null && row.getMatchedExpenseGrossAmount() == null)) {
                continue;
            }
            String prefix = "invoiceLines[" + i + "]";
            if (row.getInvoiceLineId() == null) {
                errors.add(prefix + ".invoiceLineId", "请填写发票行");
            } else if (!lines.add(row.getInvoiceLineId())) {
                errors.add(prefix + ".invoiceLineId", "同一费用不能重复匹配同一发票行");
            }
            if (row.getMatchedExpenseGrossAmount() == null || row.getMatchedExpenseGrossAmount().signum() <= 0) {
                errors.add(prefix + ".matchedExpenseGrossAmount", "匹配含税金额须大于 0");
            }
            String currency = row.getCurrency() == null ? form.getCurrency() : row.getCurrency();
            if (form.getCurrency() != null && currency != null && !form.getCurrency().equals(currency)) {
                errors.add(prefix + ".currency", "发票匹配币种须与成本一致");
            }
        }
    }

    private boolean stageOnProject(Long projectId, Long stageId) {
        if (projectId == null) {
            return false;
        }
        ContractStage stage = stages.findById(stageId).orElse(null);
        if (stage == null) {
            return false;
        }
        Contract contract = contracts.findById(stage.contractId).orElse(null);
        if (contract == null) {
            return false;
        }
        for (ProjectContract link : projectContracts.findByProjectIdOrderByIdAsc(projectId)) {
            if (contract.id.equals(link.contractId)) {
                return true;
            }
        }
        return false;
    }

    private void apply(CostOccurrence cost, CostForm form) {
        cost.bearingCompanyId = form.getBearingCompanyId();
        cost.bearingLegalPersonId = form.getBearingLegalPersonId();
        cost.incurredByEmployeeId = form.getIncurredByEmployeeId();
        cost.supplierCompanyId = form.getSupplierCompanyId();
        cost.supplierLegalPersonId = form.getSupplierLegalPersonId();
        cost.supplierPersonId = form.getSupplierPersonId();
        cost.supplierId = form.getSupplierId();
        cost.costNumber = form.getCostNumber();
        cost.purchaseContractId = form.getPurchaseContractId();
        cost.expenseStartDate = form.getExpenseStartDate();
        cost.expenseEndDate = form.getExpenseEndDate();
        cost.expenseNetAmount = form.getExpenseNetAmount();
        cost.expenseTaxAmount = form.getExpenseTaxAmount();
        cost.expenseGrossAmount = form.getExpenseGrossAmount();
        cost.employeeAdvancedAmount = form.getEmployeeAdvancedAmount();
        cost.costPolicyCode = form.getCostPolicyCode();
        cost.costPolicyVersion = form.getCostPolicyVersion();
        cost.costType = form.getCostType();
        cost.incurredAt = form.getIncurredAt().truncatedTo(ChronoUnit.MINUTES);
        cost.costAmount = form.getCostAmount();
        cost.currency = form.getCurrency();
        cost.costBasis = form.getCostBasis();
        cost.description = form.getDescription();
        cost.approvalStatus = form.getApprovalStatus();
        cost.approvedByEmployeeId = form.getApprovedByEmployeeId();
        cost.approvedAt = form.getApprovedAt() == null ? null : form.getApprovedAt().truncatedTo(ChronoUnit.MINUTES);
        cost.status = form.getStatus();
        cost.sourceDocumentId = form.getSourceDocumentId();
    }

    private void copyLaborIdentity(CostOccurrence cost, CostOccurrence stored) {
        cost.workLogId = stored.workLogId;
        cost.laborCostRateId = stored.laborCostRateId;
        cost.costedHours = stored.costedHours;
        cost.hourlyCostAmountSnapshot = stored.hourlyCostAmountSnapshot;
        cost.costAmount = stored.costAmount;
        cost.currency = stored.currency;
        cost.costBasis = stored.costBasis;
        cost.costType = stored.costType;
        cost.supersedesCostOccurrenceId = stored.supersedesCostOccurrenceId;
    }

    private void syncAllocations(CostOccurrence cost, List<AllocationRow> rows) {
        Set<Long> keep = new HashSet<>();
        for (AllocationRow row : rows) {
            if (row.isRemoved() || blankAllocation(row)) {
                continue;
            }
            CostAllocation allocation = row.getId() == null
                    ? new CostAllocation()
                    : allocations.findById(row.getId()).orElseGet(CostAllocation::new);
            allocation.costOccurrenceId = cost.id;
            allocation.projectId = row.getProjectId();
            allocation.contractStageId = row.getContractStageId();
            allocation.allocatedAmount = row.getAllocatedAmount();
            allocation.currency = row.getCurrency() == null ? cost.currency : row.getCurrency();
            allocation.allocationPeriod = row.getAllocationPeriod() == null
                    ? cost.incurredAt.toLocalDate().toString().substring(0, 7)
                    : row.getAllocationPeriod();
            allocation.allocationMethod = row.getAllocationMethod();
            allocation.allocationBasisHours = row.getAllocationBasisHours();
            allocation.allocationRatio = row.getAllocationRatio();
            allocation.allocatedByEmployeeId = row.getAllocatedByEmployeeId();
            allocation.allocatedAt = row.getAllocatedAt() == null
                    ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                    : row.getAllocatedAt().truncatedTo(ChronoUnit.MINUTES);
            allocation.basisDocumentId = row.getBasisDocumentId();
            allocation.status = row.getStatus();
            keep.add(allocations.save(allocation).id);
        }
        for (CostAllocation existing : allocations.findByCostOccurrenceIdOrderByIdAsc(cost.id)) {
            if (!keep.contains(existing.id)) {
                allocations.delete(existing);
            }
        }
    }

    private void syncInvoices(CostOccurrence cost, List<InvoiceRow> rows) {
        Set<Long> keep = new HashSet<>();
        for (InvoiceRow row : rows) {
            if (row.isRemoved() || (row.getId() == null && row.getInvoiceLineId() == null)) {
                continue;
            }
            CostInvoiceLine line = row.getId() == null
                    ? new CostInvoiceLine()
                    : invoiceLines.findById(row.getId()).orElseGet(CostInvoiceLine::new);
            line.costOccurrenceId = cost.id;
            line.invoiceLineId = row.getInvoiceLineId();
            line.matchedExpenseGrossAmount = row.getMatchedExpenseGrossAmount();
            line.currency = row.getCurrency() == null ? cost.currency : row.getCurrency();
            keep.add(invoiceLines.save(line).id);
        }
        for (CostInvoiceLine existing : invoiceLines.findByCostOccurrenceIdOrderByIdAsc(cost.id)) {
            if (!keep.contains(existing.id)) {
                invoiceLines.delete(existing);
            }
        }
    }

    private void normalize(CostForm form) {
        form.setCostNumber(Text.blankToNull(form.getCostNumber()));
        form.setCostPolicyCode(Text.blankToNull(form.getCostPolicyCode()));
        form.setCostPolicyVersion(Text.blankToNull(form.getCostPolicyVersion()));
        form.setCostType(Text.blankToNull(form.getCostType()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setCostBasis(Text.blankToNull(form.getCostBasis()));
        form.setDescription(Text.blankToNull(form.getDescription()));
        form.setApprovalStatus(Text.blankToNull(form.getApprovalStatus()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        if (form.getAllocations() == null) {
            form.setAllocations(List.of());
        }
        if (form.getInvoiceLines() == null) {
            form.setInvoiceLines(List.of());
        }
        for (AllocationRow row : form.getAllocations()) {
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            row.setAllocationPeriod(Text.blankToNull(row.getAllocationPeriod()));
            row.setAllocationMethod(Text.blankToNull(row.getAllocationMethod()));
            row.setStatus(Text.blankToNull(row.getStatus()));
        }
        for (InvoiceRow row : form.getInvoiceLines()) {
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
        }
    }

    private boolean blankAllocation(AllocationRow row) {
        return row.getId() == null && row.getProjectId() == null && row.getAllocatedAmount() == null;
    }

    private Row row(CostOccurrence cost) {
        BigDecimal allocated = BigDecimal.ZERO;
        for (CostAllocation allocation : allocations.findByCostOccurrenceIdOrderByIdAsc(cost.id)) {
            if (Codes.ALLOCATION_EFFECTIVE.equals(allocation.status)) {
                allocated = allocated.add(allocation.allocatedAmount);
            }
        }
        return new Row(
                cost.id,
                cost.costNumber,
                cost.costType,
                cost.costBasis,
                LaborCostRateService.plain(cost.costAmount),
                cost.currency,
                LaborCostRateService.plain(cost.employeeAdvancedAmount),
                LaborCostRateService.plain(allocated),
                cost.approvalStatus,
                cost.status);
    }

    public String projectLabel(Long projectId) {
        Project project = projects.findById(projectId).orElse(null);
        return project == null ? "项目 #" + projectId : project.projectCode + " " + project.name;
    }
}
