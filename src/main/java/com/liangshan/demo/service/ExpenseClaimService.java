package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeDepartmentAssignment;
import com.liangshan.demo.domain.EmployeeReportingLine;
import com.liangshan.demo.domain.ExpenseClaim;
import com.liangshan.demo.domain.ExpenseClaimLine;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.Payment;
import com.liangshan.demo.domain.PaymentAllocation;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.EmployeeDepartmentAssignmentRepository;
import com.liangshan.demo.repository.EmployeeReportingLineRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.ExpenseClaimLineRepository;
import com.liangshan.demo.repository.ExpenseClaimRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.PaymentAllocationRepository;
import com.liangshan.demo.repository.PaymentRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.ExpenseClaimForm;
import com.liangshan.demo.web.form.ExpenseClaimForm.LineRow;
import com.liangshan.demo.web.form.WorkLogDecisionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExpenseClaimService {

    private final ExpenseClaimRepository claims;
    private final ExpenseClaimLineRepository lines;
    private final CostOccurrenceRepository costs;
    private final PayableItemRepository payables;
    private final PaymentRepository payments;
    private final PaymentAllocationRepository paymentAllocations;
    private final EmployeeRepository employees;
    private final EmployeeReportingLineRepository reportingLines;
    private final EmployeeDepartmentAssignmentRepository assignments;
    private final CompanyRepository companies;
    private final SourceDocumentService documents;

    public record Row(
            Long id,
            String claimNumber,
            String claimType,
            String claimantLabel,
            String currency,
            BigDecimal claimedAmount,
            String approvalStatus,
            String payableNumber) {
    }

    public List<Row> list() {
        return claims.findAllByOrderByIdDesc().stream().map(this::row).toList();
    }

    public ExpenseClaim get(Long id) {
        return claims.findById(id).orElseThrow(NotFoundException::new);
    }

    public PayableItem payableOf(Long claimId) {
        List<PayableItem> found = payables.findByExpenseClaimId(claimId);
        return found.isEmpty() ? null : found.get(0);
    }

    public ExpenseClaimForm form(Long id) {
        ExpenseClaim claim = get(id);
        ExpenseClaimForm form = new ExpenseClaimForm();
        form.setId(claim.id);
        form.setClaimNumber(claim.claimNumber);
        form.setClaimType(claim.claimType);
        form.setClaimantEmployeeId(claim.claimantEmployeeId);
        form.setReimbursementCompanyId(claim.reimbursementCompanyId);
        form.setCurrency(claim.currency);
        form.setSourceDocumentId(claim.sourceDocumentId);
        form.setSubmitForApproval(Codes.CLAIM_PENDING.equals(claim.approvalStatus));
        for (ExpenseClaimLine line : lines.findByExpenseClaimIdOrderByLineNumberAscIdAsc(id)) {
            LineRow row = new LineRow();
            row.setId(line.id);
            row.setLineNumber(line.lineNumber);
            row.setCostOccurrenceId(line.costOccurrenceId);
            row.setClaimedReimbursementAmount(line.claimedReimbursementAmount);
            row.setRejectionReason(line.rejectionReason);
            form.getLines().add(row);
        }
        return form;
    }

    @Transactional
    public long save(ExpenseClaimForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        ExpenseClaim stored = null;
        if (form.getId() != null) {
            stored = claims.findById(form.getId()).orElse(null);
            if (stored == null) {
                errors.add("id", "报销单不存在");
            } else if (Codes.CLAIM_APPROVED.equals(stored.approvalStatus)) {
                errors.add("claimNumber", "已通过的报销不能修改");
            } else if (Codes.CLAIM_PENDING.equals(stored.approvalStatus)) {
                errors.add("claimNumber", "待审报销不能修改");
            }
        }
        validate(form, errors);
        errors.throwIfAny();
        ExpenseClaim claim = stored == null ? new ExpenseClaim() : stored;
        claim.claimNumber = form.getClaimNumber();
        claim.claimType = form.getClaimType();
        claim.claimantEmployeeId = form.getClaimantEmployeeId();
        claim.reimbursementCompanyId = form.getReimbursementCompanyId();
        claim.currency = form.getCurrency();
        claim.sourceDocumentId = form.getSourceDocumentId();
        claim.approvedByEmployeeId = null;
        claim.approvedAt = null;
        claim.approvalComment = null;
        if (form.isSubmitForApproval()) {
            claim.approvalStatus = Codes.CLAIM_PENDING;
            claim.submittedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        } else {
            claim.approvalStatus = Codes.CLAIM_DRAFT;
            claim.submittedAt = null;
        }
        claim = claims.save(claim);
        syncLines(claim, form.getLines());
        return claim.id;
    }

    @Transactional
    public void approve(Long id, WorkLogDecisionForm decision) {
        ExpenseClaim claim = get(id);
        FieldErrors errors = new FieldErrors();
        if (!Codes.CLAIM_PENDING.equals(claim.approvalStatus)) {
            errors.add("approvalStatus", "只有待审报销可以批准");
        }
        requireApprover(claim, decision, errors, false);
        errors.throwIfAny();
        claim.approvalStatus = Codes.CLAIM_APPROVED;
        claim.approvedByEmployeeId = decision.getApprovedByEmployeeId();
        claim.approvedAt = decision.getApprovedAt().truncatedTo(ChronoUnit.MINUTES);
        claim.approvalComment = Text.blankToNull(decision.getApprovalComment());
        claims.save(claim);
        BigDecimal approvedTotal = BigDecimal.ZERO;
        Long onlyCost = null;
        int approvedLines = 0;
        for (ExpenseClaimLine line : lines.findByExpenseClaimIdOrderByLineNumberAscIdAsc(claim.id)) {
            line.approvedReimbursementAmount = line.claimedReimbursementAmount;
            line.rejectionReason = null;
            lines.save(line);
            approvedTotal = approvedTotal.add(line.claimedReimbursementAmount);
            onlyCost = line.costOccurrenceId;
            approvedLines++;
        }
        PayableItem payable = new PayableItem();
        payable.payableNumber = "AP-" + claim.claimNumber;
        payable.debtorCompanyId = claim.reimbursementCompanyId;
        Employee claimant = employees.findById(claim.claimantEmployeeId).orElseThrow();
        payable.creditorPersonId = claimant.personId;
        payable.expenseClaimId = claim.id;
        payable.costOccurrenceId = approvedLines == 1 ? onlyCost : null;
        payable.payablePurpose = Codes.PAYABLE_REIMBURSEMENT;
        payable.payableAmount = LaborCostRateService.plain(approvedTotal);
        payable.currency = claim.currency;
        payable.confirmedAt = claim.approvedAt;
        payable.confirmedByEmployeeId = claim.approvedByEmployeeId;
        payable.sourceDocumentId = claim.sourceDocumentId;
        payable.status = Codes.PAYABLE_CONFIRMED;
        payables.save(payable);
    }

    @Transactional
    public void reject(Long id, WorkLogDecisionForm decision) {
        ExpenseClaim claim = get(id);
        FieldErrors errors = new FieldErrors();
        if (!Codes.CLAIM_PENDING.equals(claim.approvalStatus)) {
            errors.add("approvalStatus", "只有待审报销可以驳回");
        }
        requireApprover(claim, decision, errors, true);
        errors.throwIfAny();
        claim.approvalStatus = Codes.CLAIM_REJECTED;
        claim.approvedByEmployeeId = decision.getApprovedByEmployeeId();
        claim.approvedAt = decision.getApprovedAt().truncatedTo(ChronoUnit.MINUTES);
        claim.approvalComment = Text.blankToNull(decision.getApprovalComment());
        claims.save(claim);
    }

    /**
     * 按已批报销付给员工本人。只核销应付，不另记成本。
     */
    @Transactional
    public long payApprovedClaim(Long claimId, Long confirmedByEmployeeId) {
        ExpenseClaim claim = get(claimId);
        if (!Codes.CLAIM_APPROVED.equals(claim.approvalStatus)) {
            throw new FieldErrorsException(java.util.Map.of("approvalStatus", "只有已通过的报销可以付款"));
        }
        PayableItem payable = payableOf(claimId);
        if (payable == null || !Codes.PAYABLE_CONFIRMED.equals(payable.status)) {
            throw new FieldErrorsException(java.util.Map.of("*", "报销还没有已确认的应付"));
        }
        for (PaymentAllocation existing : paymentAllocations.findByPayableItemId(payable.id)) {
            if ("已确认".equals(existing.status) && "apply".equals(existing.settlementAction)) {
                throw new FieldErrorsException(java.util.Map.of("*", "该报销已经付款"));
            }
        }
        if (confirmedByEmployeeId == null || employees.findById(confirmedByEmployeeId).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("confirmedByEmployeeId", "请选择付款确认人"));
        }
        Employee claimant = employees.findById(claim.claimantEmployeeId).orElseThrow();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Payment payment = new Payment();
        payment.payerCompanyId = claim.reimbursementCompanyId;
        payment.payeePersonId = claimant.personId;
        payment.paymentNumber = "PAY-" + claim.claimNumber;
        payment.paymentKind = "普通收付";
        payment.paymentPurpose = Codes.PAYABLE_REIMBURSEMENT;
        payment.relatedEmployeeId = claim.claimantEmployeeId;
        payment.paidAt = now;
        payment.paymentAmount = payable.payableAmount;
        payment.currency = payable.currency;
        payment.paymentChannel = "银行转账";
        payment.confirmedByEmployeeId = confirmedByEmployeeId;
        payment.confirmedAt = now;
        payment.status = "已确认";
        payment = payments.save(payment);
        PaymentAllocation allocation = new PaymentAllocation();
        allocation.paymentId = payment.id;
        allocation.payableItemId = payable.id;
        allocation.settlementAmount = payable.payableAmount;
        allocation.currency = payable.currency;
        allocation.settlementAction = "apply";
        allocation.allocatedAt = now;
        allocation.allocatedByEmployeeId = confirmedByEmployeeId;
        allocation.status = "已确认";
        paymentAllocations.save(allocation);
        return payment.id;
    }

    private void validate(ExpenseClaimForm form, FieldErrors errors) {
        if (form.getClaimNumber() == null) {
            errors.add("claimNumber", "请填写报销单号");
        } else {
            for (ExpenseClaim other : claims.findByClaimNumber(form.getClaimNumber())) {
                if (!other.id.equals(form.getId())) {
                    errors.add("claimNumber", "报销单号已存在");
                }
            }
        }
        if (form.getClaimType() == null || !Codes.CLAIM_TYPES.contains(form.getClaimType())) {
            errors.add("claimType", "请选择报销类型");
        }
        Employee claimant = form.getClaimantEmployeeId() == null
                ? null
                : employees.findById(form.getClaimantEmployeeId()).orElse(null);
        if (claimant == null) {
            errors.add("claimantEmployeeId", "请选择报销人");
        }
        if (form.getReimbursementCompanyId() == null || companies.findById(form.getReimbursementCompanyId()).isEmpty()) {
            errors.add("reimbursementCompanyId", "请选择承担报销的公司");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        int kept = 0;
        Set<Long> costsOnForm = new HashSet<>();
        for (int i = 0; i < form.getLines().size(); i++) {
            LineRow row = form.getLines().get(i);
            if (row.isRemoved() || (row.getId() == null && row.getCostOccurrenceId() == null && row.getClaimedReimbursementAmount() == null)) {
                continue;
            }
            kept++;
            String prefix = "lines[" + i + "]";
            CostOccurrence cost = row.getCostOccurrenceId() == null ? null : costs.findById(row.getCostOccurrenceId()).orElse(null);
            if (cost == null) {
                errors.add(prefix + ".costOccurrenceId", "请选择费用");
                continue;
            }
            if (!costsOnForm.add(cost.id)) {
                errors.add(prefix + ".costOccurrenceId", "同一报销单不能重复引用同一费用");
            }
            if (!Codes.COST_EFFECTIVE.equals(cost.status) || !Codes.RATE_APPROVED.equals(cost.approvalStatus)) {
                errors.add(prefix + ".costOccurrenceId", "只有已批准的有效费用可以报销");
            }
            if (cost.employeeAdvancedAmount == null) {
                errors.add(prefix + ".costOccurrenceId", "该费用没有员工垫付，不能报销");
            }
            if (claimant != null && !claimant.id.equals(cost.incurredByEmployeeId)) {
                errors.add(prefix + ".costOccurrenceId", "垫付员工须是报销人");
            }
            if (form.getCurrency() != null && !form.getCurrency().equals(cost.currency)) {
                errors.add(prefix + ".costOccurrenceId", "报销币种须与费用一致");
            }
            if (row.getClaimedReimbursementAmount() == null || row.getClaimedReimbursementAmount().signum() <= 0) {
                errors.add(prefix + ".claimedReimbursementAmount", "申请金额须大于 0");
            } else if (cost.employeeAdvancedAmount != null
                    && row.getClaimedReimbursementAmount().compareTo(cost.employeeAdvancedAmount) > 0) {
                errors.add(prefix + ".claimedReimbursementAmount", "申请金额不能超过员工垫付");
            }
            if (occupied(cost.id, form.getId())) {
                errors.add(prefix + ".costOccurrenceId", "同一垫付不能重复报销");
            }
        }
        if (kept == 0) {
            errors.add("lines", "请至少填写一条报销明细");
        }
    }

    private boolean occupied(Long costId, Long claimId) {
        for (ExpenseClaimLine line : lines.findByCostOccurrenceId(costId)) {
            if (line.expenseClaimId.equals(claimId)) {
                continue;
            }
            ExpenseClaim other = claims.findById(line.expenseClaimId).orElse(null);
            if (other == null || Codes.CLAIM_REJECTED.equals(other.approvalStatus)) {
                continue;
            }
            if (Codes.CLAIM_APPROVED.equals(other.approvalStatus) && line.rejectionReason != null) {
                continue;
            }
            return true;
        }
        return false;
    }

    private void requireApprover(ExpenseClaim claim, WorkLogDecisionForm decision, FieldErrors errors, boolean reasonRequired) {
        if (decision == null || decision.getApprovedByEmployeeId() == null
                || employees.findById(decision.getApprovedByEmployeeId()).isEmpty()) {
            errors.add("approvedByEmployeeId", "请选择审批人");
        } else if (decision.getApprovedByEmployeeId().equals(claim.claimantEmployeeId)) {
            errors.add("approvedByEmployeeId", "不能审批自己的报销");
        } else if (!administrativeManager(claim, decision.getApprovedByEmployeeId())) {
            errors.add("approvedByEmployeeId", "审批人不是该员工在提交日的行政上级");
        }
        if (decision == null || decision.getApprovedAt() == null) {
            errors.add("approvedAt", "请填写审批时间");
        }
        if (reasonRequired && (decision == null || Text.blankToNull(decision.getApprovalComment()) == null)) {
            errors.add("approvalComment", "驳回须填写原因");
        }
    }

    private boolean administrativeManager(ExpenseClaim claim, Long approverEmployeeId) {
        if (claim.submittedAt == null) {
            return false;
        }
        LocalDate day = claim.submittedAt.toLocalDate();
        for (EmployeeReportingLine line : reportingLines.findByEmployeeIdOrderByIdAsc(claim.claimantEmployeeId)) {
            if (!Codes.REPORTING_ADMIN.equals(line.reportingType)) {
                continue;
            }
            if (!Periods.contains(line.validFrom, line.validTo, day, day.plusDays(1))) {
                continue;
            }
            EmployeeDepartmentAssignment manager = assignments.findById(line.managerAssignmentId).orElse(null);
            if (manager == null || !approverEmployeeId.equals(manager.employeeId)) {
                continue;
            }
            if (Periods.contains(manager.validFrom, manager.validTo, day, day.plusDays(1))) {
                return true;
            }
        }
        return false;
    }

    private void syncLines(ExpenseClaim claim, List<LineRow> rows) {
        Set<Long> keep = new HashSet<>();
        int number = 1;
        for (LineRow row : rows) {
            if (row.isRemoved() || row.getCostOccurrenceId() == null) {
                continue;
            }
            ExpenseClaimLine line = row.getId() == null
                    ? new ExpenseClaimLine()
                    : lines.findById(row.getId()).orElseGet(ExpenseClaimLine::new);
            line.expenseClaimId = claim.id;
            line.lineNumber = row.getLineNumber() == null ? number : row.getLineNumber();
            line.costOccurrenceId = row.getCostOccurrenceId();
            line.claimedReimbursementAmount = row.getClaimedReimbursementAmount();
            line.approvedReimbursementAmount = null;
            line.rejectionReason = null;
            keep.add(lines.save(line).id);
            number++;
        }
        for (ExpenseClaimLine existing : lines.findByExpenseClaimIdOrderByLineNumberAscIdAsc(claim.id)) {
            if (!keep.contains(existing.id)) {
                lines.delete(existing);
            }
        }
    }

    private void normalize(ExpenseClaimForm form) {
        form.setClaimNumber(Text.blankToNull(form.getClaimNumber()));
        form.setClaimType(Text.blankToNull(form.getClaimType()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        if (form.getLines() == null) {
            form.setLines(List.of());
        }
        for (LineRow row : form.getLines()) {
            row.setRejectionReason(Text.blankToNull(row.getRejectionReason()));
        }
    }

    private Row row(ExpenseClaim claim) {
        BigDecimal claimed = BigDecimal.ZERO;
        Employee employee = employees.findById(claim.claimantEmployeeId).orElse(null);
        String who = employee == null ? "员工 #" + claim.claimantEmployeeId : employee.employeeCode;
        for (ExpenseClaimLine line : lines.findByExpenseClaimIdOrderByLineNumberAscIdAsc(claim.id)) {
            claimed = claimed.add(line.claimedReimbursementAmount);
        }
        PayableItem payable = payableOf(claim.id);
        return new Row(
                claim.id,
                claim.claimNumber,
                claim.claimType,
                who,
                claim.currency,
                LaborCostRateService.plain(claimed),
                claim.approvalStatus,
                payable == null ? null : payable.payableNumber);
    }
}
