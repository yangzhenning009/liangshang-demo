package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.ContractBillingSchedule;
import com.liangshan.demo.domain.ContractPaymentSchedule;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.PayableAdjustment;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.Payment;
import com.liangshan.demo.domain.PaymentAllocation;
import com.liangshan.demo.domain.ReceivableAdjustment;
import com.liangshan.demo.domain.ReceivableItem;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractPaymentScheduleRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PayableAdjustmentRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.PaymentAllocationRepository;
import com.liangshan.demo.repository.PaymentRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ReceivableAdjustmentRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.PayableForm;
import com.liangshan.demo.web.form.PaymentForm;
import com.liangshan.demo.web.form.PaymentForm.AllocationRow;
import com.liangshan.demo.web.form.ReceivableForm;
import com.liangshan.demo.web.form.ReceivableForm.AdjustmentRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FundsService {

    private final ReceivableItemRepository receivables;
    private final ReceivableAdjustmentRepository receivableAdjustments;
    private final PayableItemRepository payables;
    private final PayableAdjustmentRepository payableAdjustments;
    private final PaymentRepository payments;
    private final PaymentAllocationRepository allocations;
    private final ContractPaymentScheduleRepository paymentSchedules;
    private final ContractBillingScheduleRepository billingSchedules;
    private final ContractRepository contracts;
    private final ProjectRepository projects;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final SupplierRepository suppliers;
    private final SourceDocumentService documents;
    private final ScheduleDueDate dueDates;

    public record ItemRow(
            Long id,
            String number,
            String purpose,
            BigDecimal amount,
            BigDecimal outstanding,
            String currency,
            LocalDate dueDate,
            String status) {
    }

    public record PaymentRow(
            Long id,
            String paymentNumber,
            String paymentKind,
            BigDecimal paymentAmount,
            String currency,
            String status,
            BigDecimal unallocated) {
    }

    public List<ItemRow> receivables() {
        return receivables.findAllByOrderByIdDesc().stream().map(item -> new ItemRow(
                item.id,
                item.receivableNumber,
                item.receivablePurpose,
                plain(item.receivableAmount),
                plain(outstandingReceivable(item.id)),
                item.currency,
                item.dueDate,
                item.status)).toList();
    }

    public List<ItemRow> payables() {
        return payables.findAllByOrderByIdDesc().stream().map(item -> new ItemRow(
                item.id,
                item.payableNumber,
                item.payablePurpose,
                plain(item.payableAmount),
                plain(outstandingPayable(item.id)),
                item.currency,
                item.dueDate,
                item.status)).toList();
    }

    public List<PaymentRow> payments() {
        return payments.findAllByOrderByPaidAtDescIdDesc().stream().map(payment -> new PaymentRow(
                payment.id,
                payment.paymentNumber,
                payment.paymentKind,
                plain(payment.paymentAmount),
                payment.currency,
                payment.status,
                plain(unallocated(payment)))).toList();
    }

    public ReceivableItem receivable(Long id) {
        return receivables.findById(id).orElseThrow(NotFoundException::new);
    }

    public PayableItem payable(Long id) {
        return payables.findById(id).orElseThrow(NotFoundException::new);
    }

    public Payment payment(Long id) {
        return payments.findById(id).orElseThrow(NotFoundException::new);
    }

    public BigDecimal outstandingReceivable(Long id) {
        ReceivableItem item = receivable(id);
        if (!Codes.PAYABLE_CONFIRMED.equals(item.status)) {
            return null;
        }
        return adjustedReceivable(item).subtract(net(allocations.findByReceivableItemId(id)));
    }

    public BigDecimal outstandingPayable(Long id) {
        PayableItem item = payable(id);
        if (!Codes.PAYABLE_CONFIRMED.equals(item.status)) {
            return null;
        }
        return adjustedPayable(item).subtract(net(allocations.findByPayableItemId(id)));
    }

    public BigDecimal settledReceivable(Long id) {
        return net(allocations.findByReceivableItemId(id));
    }

    @Transactional
    public long saveReceivable(ReceivableForm form) {
        normalizeItem(form);
        FieldErrors errors = new FieldErrors();
        ReceivableItem stored = loadReceivable(form.getId(), errors);
        applySchedule(form, errors);
        validateReceivable(form, stored, errors);
        errors.throwIfAny();
        ReceivableItem item = stored == null ? new ReceivableItem() : stored;
        copyReceivable(item, form);
        item = receivables.save(item);
        syncReceivableAdjustments(item, form.getAdjustments(), errors);
        errors.throwIfAny();
        return item.id;
    }

    @Transactional
    public long savePayable(PayableForm form) {
        normalizePayable(form);
        FieldErrors errors = new FieldErrors();
        PayableItem stored = form.getId() == null ? null : payables.findById(form.getId()).orElse(null);
        if (form.getId() != null && stored == null) {
            errors.add("id", "应付不存在");
        }
        applyPayableSchedule(form, errors);
        validatePayable(form, stored, errors);
        errors.throwIfAny();
        PayableItem item = stored == null ? new PayableItem() : stored;
        copyPayable(item, form);
        if (stored != null && stored.expenseClaimId != null) {
            item.expenseClaimId = stored.expenseClaimId;
            item.creditorPersonId = stored.creditorPersonId;
            item.payablePurpose = stored.payablePurpose;
        }
        item = payables.save(item);
        syncPayableAdjustments(item, form.getAdjustments(), errors);
        errors.throwIfAny();
        return item.id;
    }

    public ReceivableForm receivableForm(Long id) {
        ReceivableItem item = receivable(id);
        ReceivableForm form = new ReceivableForm();
        form.setId(item.id);
        form.setReceivableNumber(item.receivableNumber);
        form.setCreditorCompanyId(item.creditorCompanyId);
        form.setCreditorLegalPersonId(item.creditorLegalPersonId);
        form.setDebtorCompanyId(item.debtorCompanyId);
        form.setDebtorLegalPersonId(item.debtorLegalPersonId);
        form.setDebtorPersonId(item.debtorPersonId);
        form.setContractId(item.contractId);
        form.setContractPaymentScheduleId(item.contractPaymentScheduleId);
        form.setProjectId(item.projectId);
        form.setContractStageId(item.contractStageId);
        form.setReceivablePurpose(item.receivablePurpose);
        form.setReceivableAmount(item.receivableAmount);
        form.setCurrency(item.currency);
        form.setConfirmedAt(item.confirmedAt);
        form.setConfirmedByEmployeeId(item.confirmedByEmployeeId);
        form.setDueDate(item.dueDate);
        form.setTriggerAcceptanceEventId(item.triggerAcceptanceEventId);
        form.setTriggerInvoiceId(item.triggerInvoiceId);
        form.setSourceDocumentId(item.sourceDocumentId);
        form.setStatus(item.status);
        for (ReceivableAdjustment adjustment : receivableAdjustments.findByReceivableItemIdOrderByIdAsc(id)) {
            form.getAdjustments().add(adjustmentRow(
                    adjustment.id, adjustment.increaseReceivableAmount, adjustment.decreaseReceivableAmount,
                    adjustment.currency, adjustment.adjustedAt, adjustment.adjustmentReason,
                    adjustment.sourceDocumentId, adjustment.approvedByEmployeeId, adjustment.approvedAt, adjustment.status));
        }
        return form;
    }

    public PayableForm payableForm(Long id) {
        PayableItem item = payable(id);
        PayableForm form = new PayableForm();
        form.setId(item.id);
        form.setPayableNumber(item.payableNumber);
        form.setDebtorCompanyId(item.debtorCompanyId);
        form.setDebtorLegalPersonId(item.debtorLegalPersonId);
        form.setCreditorCompanyId(item.creditorCompanyId);
        form.setCreditorLegalPersonId(item.creditorLegalPersonId);
        form.setCreditorPersonId(item.creditorPersonId);
        form.setSupplierId(item.supplierId);
        form.setContractId(item.contractId);
        form.setContractPaymentScheduleId(item.contractPaymentScheduleId);
        form.setProjectId(item.projectId);
        form.setContractStageId(item.contractStageId);
        form.setCostOccurrenceId(item.costOccurrenceId);
        form.setExpenseClaimId(item.expenseClaimId);
        form.setPayablePurpose(item.payablePurpose);
        form.setPayableAmount(item.payableAmount);
        form.setCurrency(item.currency);
        form.setConfirmedAt(item.confirmedAt);
        form.setConfirmedByEmployeeId(item.confirmedByEmployeeId);
        form.setDueDate(item.dueDate);
        form.setTriggerAcceptanceEventId(item.triggerAcceptanceEventId);
        form.setTriggerInvoiceId(item.triggerInvoiceId);
        form.setSourceDocumentId(item.sourceDocumentId);
        form.setStatus(item.status);
        for (PayableAdjustment adjustment : payableAdjustments.findByPayableItemIdOrderByIdAsc(id)) {
            form.getAdjustments().add(adjustmentRow(
                    adjustment.id, adjustment.increasePayableAmount, adjustment.decreasePayableAmount,
                    adjustment.currency, adjustment.adjustedAt, adjustment.adjustmentReason,
                    adjustment.sourceDocumentId, adjustment.approvedByEmployeeId, adjustment.approvedAt, adjustment.status));
        }
        return form;
    }

    public PaymentForm paymentForm(Long id) {
        Payment payment = payment(id);
        PaymentForm form = new PaymentForm();
        form.setId(payment.id);
        form.setPayerCompanyId(payment.payerCompanyId);
        form.setPayerLegalPersonId(payment.payerLegalPersonId);
        form.setPayerPersonId(payment.payerPersonId);
        form.setPayeeCompanyId(payment.payeeCompanyId);
        form.setPayeeLegalPersonId(payment.payeeLegalPersonId);
        form.setPayeePersonId(payment.payeePersonId);
        form.setPaymentForCompanyId(payment.paymentForCompanyId);
        form.setPaymentForLegalPersonId(payment.paymentForLegalPersonId);
        form.setPaymentForPersonId(payment.paymentForPersonId);
        form.setReceiptForCompanyId(payment.receiptForCompanyId);
        form.setReceiptForLegalPersonId(payment.receiptForLegalPersonId);
        form.setReceiptForPersonId(payment.receiptForPersonId);
        form.setAgencyBasisDocumentId(payment.agencyBasisDocumentId);
        form.setPaymentNumber(payment.paymentNumber);
        form.setPaymentKind(payment.paymentKind);
        form.setPaymentPurpose(payment.paymentPurpose);
        form.setRelatedEmployeeId(payment.relatedEmployeeId);
        form.setRefundOfPaymentId(payment.refundOfPaymentId);
        form.setPayerCompanyBankAccountId(payment.payerCompanyBankAccountId);
        form.setPayeeCompanyBankAccountId(payment.payeeCompanyBankAccountId);
        form.setPaidAt(payment.paidAt);
        form.setPaymentAmount(payment.paymentAmount);
        form.setCurrency(payment.currency);
        form.setBankValueDate(payment.bankValueDate);
        form.setBankFeeAmount(payment.bankFeeAmount);
        form.setPaymentChannel(payment.paymentChannel);
        form.setPayerBankAccountNumberSnapshot(payment.payerBankAccountNumberSnapshot);
        form.setPayerBankAccountNameSnapshot(payment.payerBankAccountNameSnapshot);
        form.setPayerBankNameSnapshot(payment.payerBankNameSnapshot);
        form.setPayeeBankAccountNumberSnapshot(payment.payeeBankAccountNumberSnapshot);
        form.setPayeeBankAccountNameSnapshot(payment.payeeBankAccountNameSnapshot);
        form.setPayeeBankNameSnapshot(payment.payeeBankNameSnapshot);
        form.setBankTransactionNumber(payment.bankTransactionNumber);
        form.setPaymentPlatformTransactionNumber(payment.paymentPlatformTransactionNumber);
        form.setConfirmedByEmployeeId(payment.confirmedByEmployeeId);
        form.setConfirmedAt(payment.confirmedAt);
        form.setSourceDocumentId(payment.sourceDocumentId);
        form.setStatus(payment.status);
        for (PaymentAllocation allocation : allocations.findByPaymentIdOrderByIdAsc(id)) {
            AllocationRow row = new AllocationRow();
            row.setId(allocation.id);
            row.setReceivableItemId(allocation.receivableItemId);
            row.setPayableItemId(allocation.payableItemId);
            row.setSettlementAmount(allocation.settlementAmount);
            row.setCurrency(allocation.currency);
            row.setSettlementAction(allocation.settlementAction);
            row.setReversesAllocationId(allocation.reversesAllocationId);
            row.setAllocatedAt(allocation.allocatedAt);
            row.setAllocatedByEmployeeId(allocation.allocatedByEmployeeId);
            row.setSettlementBasisDocumentId(allocation.settlementBasisDocumentId);
            row.setStatus(allocation.status);
            form.getAllocations().add(row);
        }
        return form;
    }

    @Transactional
    public long savePayment(PaymentForm form) {
        normalizePayment(form);
        FieldErrors errors = new FieldErrors();
        Payment stored = form.getId() == null ? null : payments.findById(form.getId()).orElse(null);
        if (form.getId() != null && stored == null) {
            errors.add("id", "收付款不存在");
        }
        validatePayment(form, errors);
        List<AllocationRow> rows = activeAllocations(form);
        validateAllocations(form, stored, rows, errors);
        errors.throwIfAny();
        Payment payment = stored == null ? new Payment() : stored;
        copyPayment(payment, form);
        payment = payments.save(payment);
        syncAllocations(payment, rows);
        return payment.id;
    }

    private void applySchedule(ReceivableForm form, FieldErrors errors) {
        if (form.getContractPaymentScheduleId() != null) {
            ContractPaymentSchedule schedule = paymentSchedules.findById(form.getContractPaymentScheduleId()).orElse(null);
            if (schedule == null) {
                errors.add("contractPaymentScheduleId", "收付款计划不存在");
                return;
            }
            form.setContractId(schedule.contractId);
            if (form.getContractStageId() == null) {
                form.setContractStageId(schedule.contractStageId);
            }
            if (form.getReceivableAmount() == null) {
                form.setReceivableAmount(schedule.plannedPaymentAmount);
            }
            if (form.getCurrency() == null) {
                form.setCurrency(schedule.currency);
            }
            if (!Codes.TRIGGER_MANUAL.equals(schedule.paymentTrigger)) {
                LocalDate due = dueDates.fromPaymentSchedule(
                        schedule.id, form.getTriggerAcceptanceEventId(), form.getTriggerInvoiceId(), errors);
                if (due != null) {
                    form.setDueDate(due);
                }
            }
            return;
        }
        if (form.getBillingScheduleId() != null) {
            ContractBillingSchedule schedule = billingSchedules.findById(form.getBillingScheduleId()).orElse(null);
            if (schedule == null) {
                errors.add("billingScheduleId", "开票计划不存在");
                return;
            }
            form.setContractId(schedule.contractId);
            if (form.getContractStageId() == null) {
                form.setContractStageId(schedule.contractStageId);
            }
            if (form.getReceivableAmount() == null) {
                form.setReceivableAmount(schedule.plannedInvoiceGrossAmount);
            }
            if (form.getCurrency() == null) {
                form.setCurrency(schedule.currency);
            }
            if (!Codes.TRIGGER_MANUAL.equals(schedule.billingTrigger)) {
                LocalDate due = dueDates.fromBillingSchedule(schedule.id, form.getTriggerAcceptanceEventId(), errors);
                if (due != null) {
                    form.setDueDate(due);
                }
            }
        }
    }

    private void applyPayableSchedule(PayableForm form, FieldErrors errors) {
        if (form.getContractPaymentScheduleId() == null) {
            return;
        }
        ContractPaymentSchedule schedule = paymentSchedules.findById(form.getContractPaymentScheduleId()).orElse(null);
        if (schedule == null) {
            errors.add("contractPaymentScheduleId", "收付款计划不存在");
            return;
        }
        form.setContractId(schedule.contractId);
        if (form.getContractStageId() == null) {
            form.setContractStageId(schedule.contractStageId);
        }
        if (form.getPayableAmount() == null) {
            form.setPayableAmount(schedule.plannedPaymentAmount);
        }
        if (form.getCurrency() == null) {
            form.setCurrency(schedule.currency);
        }
        if (!Codes.TRIGGER_MANUAL.equals(schedule.paymentTrigger)) {
            LocalDate due = dueDates.fromPaymentSchedule(
                    schedule.id, form.getTriggerAcceptanceEventId(), form.getTriggerInvoiceId(), errors);
            if (due != null) {
                form.setDueDate(due);
            }
        }
    }

    private void validateReceivable(ReceivableForm form, ReceivableItem stored, FieldErrors errors) {
        if (form.getReceivableNumber() == null) {
            errors.add("receivableNumber", "请填写应收编号");
        } else {
            for (ReceivableItem other : receivables.findByReceivableNumber(form.getReceivableNumber())) {
                if (!other.id.equals(form.getId()) && !Codes.COST_VOID.equals(other.status)) {
                    errors.add("receivableNumber", "应收编号已存在");
                }
            }
        }
        if (count(form.getCreditorCompanyId(), form.getCreditorLegalPersonId()) != 1) {
            errors.add("creditorCompanyId", "债权人在公司和法人中恰好选一个");
        }
        if (count(form.getDebtorCompanyId(), form.getDebtorLegalPersonId(), form.getDebtorPersonId()) != 1) {
            errors.add("debtorCompanyId", "债务人在公司、法人、自然人中恰好选一个");
        }
        requireParty(form.getCreditorCompanyId(), companies, "creditorCompanyId", "债权人公司不存在", errors);
        requireParty(form.getDebtorCompanyId(), companies, "debtorCompanyId", "债务人公司不存在", errors);
        if (form.getReceivableAmount() == null || form.getReceivableAmount().signum() < 0) {
            errors.add("receivableAmount", "应收本金不能为负");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        }
        if (form.getReceivablePurpose() == null || !Codes.RECEIVABLE_PURPOSES.contains(form.getReceivablePurpose())) {
            errors.add("receivablePurpose", "请选择应收用途");
        }
        if (form.getStatus() == null || !Codes.PAYABLE_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择状态");
        }
        if (Codes.PAYABLE_CONFIRMED.equals(form.getStatus())) {
            if (form.getConfirmedByEmployeeId() == null || employees.findById(form.getConfirmedByEmployeeId()).isEmpty()) {
                errors.add("confirmedByEmployeeId", "确认时须选择员工");
            }
            if (form.getConfirmedAt() == null) {
                errors.add("confirmedAt", "确认时须填写时间");
            }
            if (form.getSourceDocumentId() == null) {
                errors.add("sourceDocumentId", "确认时须选择依据材料");
            }
            if (form.getReceivableAmount() == null || form.getReceivableAmount().signum() <= 0) {
                errors.add("receivableAmount", "确认的应收本金须大于 0");
            }
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        if (form.getContractId() != null && contracts.findById(form.getContractId()).isEmpty()) {
            errors.add("contractId", "合同不存在");
        }
        if (form.getProjectId() != null && projects.findById(form.getProjectId()).isEmpty()) {
            errors.add("projectId", "项目不存在");
        }
        scheduleOnce(form.getContractPaymentScheduleId(), form.getId(), null, errors);
        if (stored != null && stored.receivableAmount != null && form.getReceivableAmount() != null
                && stored.receivableAmount.compareTo(form.getReceivableAmount()) != 0
                && Codes.PAYABLE_CONFIRMED.equals(stored.status)) {
            errors.add("receivableAmount", "已确认本金不能直接改，请记调整");
        }
        validateAdjustmentRows(form.getAdjustments(), form.getCurrency(), errors);
    }

    private void validatePayable(PayableForm form, PayableItem stored, FieldErrors errors) {
        if (form.getPayableNumber() == null) {
            errors.add("payableNumber", "请填写应付编号");
        } else {
            for (PayableItem other : payables.findByPayableNumber(form.getPayableNumber())) {
                if (!other.id.equals(form.getId()) && !Codes.COST_VOID.equals(other.status)) {
                    errors.add("payableNumber", "应付编号已存在");
                }
            }
        }
        if (count(form.getDebtorCompanyId(), form.getDebtorLegalPersonId()) != 1) {
            errors.add("debtorCompanyId", "债务人在公司和法人中恰好选一个");
        }
        if (count(form.getCreditorCompanyId(), form.getCreditorLegalPersonId(), form.getCreditorPersonId()) != 1) {
            errors.add("creditorCompanyId", "债权人在公司、法人、自然人中恰好选一个");
        }
        alignSupplier(form, errors);
        if (form.getPayableAmount() == null || form.getPayableAmount().signum() < 0) {
            errors.add("payableAmount", "应付本金不能为负");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        }
        if (form.getPayablePurpose() == null || !Codes.PAYABLE_PURPOSES.contains(form.getPayablePurpose())) {
            errors.add("payablePurpose", "请选择应付用途");
        }
        if (form.getStatus() == null || !Codes.PAYABLE_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择状态");
        }
        boolean claim = stored != null && stored.expenseClaimId != null || form.getExpenseClaimId() != null;
        if (Codes.PAYABLE_CONFIRMED.equals(form.getStatus())) {
            if (form.getConfirmedByEmployeeId() == null || employees.findById(form.getConfirmedByEmployeeId()).isEmpty()) {
                errors.add("confirmedByEmployeeId", "确认时须选择员工");
            }
            if (form.getConfirmedAt() == null) {
                errors.add("confirmedAt", "确认时须填写时间");
            }
            if (!claim && form.getSourceDocumentId() == null) {
                errors.add("sourceDocumentId", "确认时须选择依据材料");
            }
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        scheduleOnce(form.getContractPaymentScheduleId(), null, form.getId(), errors);
        if (stored != null && Codes.PAYABLE_CONFIRMED.equals(stored.status)
                && stored.payableAmount.compareTo(form.getPayableAmount()) != 0) {
            errors.add("payableAmount", "已确认本金不能直接改，请记调整");
        }
        validateAdjustmentRows(form.getAdjustments(), form.getCurrency(), errors);
    }

    private void alignSupplier(PayableForm form, FieldErrors errors) {
        if (form.getSupplierId() == null) {
            return;
        }
        Supplier supplier = suppliers.findById(form.getSupplierId()).orElse(null);
        if (supplier == null) {
            errors.add("supplierId", "供应商档案不存在");
            return;
        }
        if (form.getDebtorCompanyId() != null && !form.getDebtorCompanyId().equals(supplier.purchasingCompanyId)) {
            errors.add("debtorCompanyId", "供应商应付的债务公司须是档案上的采购公司");
        }
        if (!Cmp.eq(form.getCreditorCompanyId(), supplier.companyId)
                || !Cmp.eq(form.getCreditorLegalPersonId(), supplier.legalPersonId)
                || !Cmp.eq(form.getCreditorPersonId(), supplier.personId)) {
            errors.add("supplierId", "债权人须与供应商档案的供货对象一致");
        }
    }

    private void scheduleOnce(Long scheduleId, Long receivableId, Long payableId, FieldErrors errors) {
        if (scheduleId == null) {
            return;
        }
        for (ReceivableItem other : receivables.findByContractPaymentScheduleId(scheduleId)) {
            if (!other.id.equals(receivableId) && !Codes.COST_VOID.equals(other.status)) {
                errors.add("contractPaymentScheduleId", "该收付款计划已经确认过结算项");
            }
        }
        for (PayableItem other : payables.findByContractPaymentScheduleId(scheduleId)) {
            if (!other.id.equals(payableId) && !Codes.COST_VOID.equals(other.status)) {
                errors.add("contractPaymentScheduleId", "该收付款计划已经确认过结算项");
            }
        }
    }

    private void validatePayment(PaymentForm form, FieldErrors errors) {
        if (count(form.getPayerCompanyId(), form.getPayerLegalPersonId(), form.getPayerPersonId()) != 1) {
            errors.add("payerCompanyId", "付款方恰好选一个");
        }
        if (count(form.getPayeeCompanyId(), form.getPayeeLegalPersonId(), form.getPayeePersonId()) != 1) {
            errors.add("payeeCompanyId", "收款方恰好选一个");
        }
        if (count(form.getPaymentForCompanyId(), form.getPaymentForLegalPersonId(), form.getPaymentForPersonId()) > 1) {
            errors.add("paymentForCompanyId", "被代付对象最多选一个");
        }
        if (count(form.getReceiptForCompanyId(), form.getReceiptForLegalPersonId(), form.getReceiptForPersonId()) > 1) {
            errors.add("receiptForCompanyId", "被代收对象最多选一个");
        }
        boolean agency = form.getPaymentForCompanyId() != null || form.getPaymentForLegalPersonId() != null
                || form.getPaymentForPersonId() != null || form.getReceiptForCompanyId() != null
                || form.getReceiptForLegalPersonId() != null || form.getReceiptForPersonId() != null;
        if (agency && form.getAgencyBasisDocumentId() == null) {
            errors.add("agencyBasisDocumentId", "代收或代付须填写依据");
        }
        documents.require(form.getAgencyBasisDocumentId(), errors, "agencyBasisDocumentId");
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        if (form.getPaymentNumber() == null) {
            errors.add("paymentNumber", "请填写收付款编号");
        } else {
            for (Payment other : payments.findByPaymentNumber(form.getPaymentNumber())) {
                if (!other.id.equals(form.getId()) && !Codes.COST_VOID.equals(other.status)) {
                    errors.add("paymentNumber", "收付款编号已存在");
                }
            }
        }
        if (form.getPaymentKind() == null || !Codes.PAYMENT_KINDS.contains(form.getPaymentKind())) {
            errors.add("paymentKind", "请选择普通收付或退款");
        }
        if (form.getPaymentPurpose() == null || !Codes.PAYMENT_PURPOSE.contains(form.getPaymentPurpose())) {
            errors.add("paymentPurpose", "请选择款项用途");
        }
        if (Codes.PAYMENT_REFUND.equals(form.getPaymentKind())) {
            Payment original = form.getRefundOfPaymentId() == null ? null : payments.findById(form.getRefundOfPaymentId()).orElse(null);
            if (original == null) {
                errors.add("refundOfPaymentId", "退款须选择原收付款");
            } else if (!reversedParties(form, original) && form.getAgencyBasisDocumentId() == null) {
                errors.add("payerCompanyId", "退款的收付双方须与原单相反，否则须有代付依据");
            }
        } else if (form.getRefundOfPaymentId() != null) {
            errors.add("refundOfPaymentId", "普通收付不要填写退款原单");
        }
        if (form.getPaidAt() == null) {
            errors.add("paidAt", "请填写支付时间");
        }
        if (form.getPaymentAmount() == null || form.getPaymentAmount().signum() <= 0) {
            errors.add("paymentAmount", "收付本金须大于 0");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        }
        if (form.getPaymentChannel() == null || !Codes.PAYMENT_CHANNELS.contains(form.getPaymentChannel())) {
            errors.add("paymentChannel", "请选择支付渠道");
        } else if ("现金".equals(form.getPaymentChannel()) && hasBankSnapshot(form)) {
            errors.add("paymentChannel", "现金不填写银行账号");
        } else if ("支付平台".equals(form.getPaymentChannel()) && hasBankNumber(form)) {
            errors.add("payerBankAccountNumberSnapshot", "支付平台流水不要填进银行账号");
        } else if ("银行转账".equals(form.getPaymentChannel()) && form.getPaymentPlatformTransactionNumber() != null) {
            errors.add("paymentPlatformTransactionNumber", "银行转账不要填写支付平台流水号");
        }
        if (form.getBankFeeAmount() != null && form.getBankFeeAmount().signum() < 0) {
            errors.add("bankFeeAmount", "手续费不能为负");
        }
        if (form.getStatus() == null || !Codes.PAYMENT_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择状态");
        }
        if (Codes.PAYMENT_CONFIRMED.equals(form.getStatus())) {
            if (form.getConfirmedByEmployeeId() == null || employees.findById(form.getConfirmedByEmployeeId()).isEmpty()) {
                errors.add("confirmedByEmployeeId", "已确认须选择核对人");
            }
            if (form.getConfirmedAt() == null) {
                errors.add("confirmedAt", "已确认须填写核对时间");
            }
        }
        if (Codes.PAYABLE_REIMBURSEMENT.equals(form.getPaymentPurpose()) && form.getRelatedEmployeeId() != null) {
            Employee employee = employees.findById(form.getRelatedEmployeeId()).orElse(null);
            if (employee == null || !Cmp.eq(employee.personId, form.getPayeePersonId())) {
                errors.add("payeePersonId", "员工报销的收款自然人须是该员工本人");
            }
        }
        if (form.getRefundOfPaymentId() != null && form.getPaymentAmount() != null) {
            BigDecimal refunded = form.getPaymentAmount();
            for (Payment other : payments.findByRefundOfPaymentId(form.getRefundOfPaymentId())) {
                if (!other.id.equals(form.getId()) && !Codes.COST_VOID.equals(other.status)) {
                    refunded = refunded.add(other.paymentAmount);
                }
            }
            Payment original = payments.findById(form.getRefundOfPaymentId()).orElse(null);
            if (original != null && refunded.compareTo(original.paymentAmount) > 0) {
                errors.add("paymentAmount", "退款合计不能超过原收付款");
            }
        }
    }

    private void validateAllocations(PaymentForm form, Payment stored, List<AllocationRow> rows, FieldErrors errors) {
        if (!Codes.PAYMENT_CONFIRMED.equals(form.getStatus())) {
            for (AllocationRow row : rows) {
                if (Codes.PAYMENT_CONFIRMED.equals(row.getStatus())) {
                    errors.add("allocations", "草稿或作废的收付款不能有已确认核销");
                }
            }
        }
        BigDecimal receivableSide = BigDecimal.ZERO;
        BigDecimal payableSide = BigDecimal.ZERO;
        for (int i = 0; i < rows.size(); i++) {
            AllocationRow row = rows.get(i);
            String prefix = "allocations[" + i + "]";
            if (count(row.getReceivableItemId(), row.getPayableItemId()) != 1) {
                errors.add(prefix, "核销恰好选择应收或应付之一");
                continue;
            }
            if (row.getSettlementAmount() == null || row.getSettlementAmount().signum() <= 0) {
                errors.add(prefix + ".settlementAmount", "核销金额须大于 0");
            }
            if (row.getSettlementAction() == null || !Codes.SETTLEMENT_ACTIONS.contains(row.getSettlementAction())) {
                errors.add(prefix + ".settlementAction", "请选择核销或撤销");
            }
            if (row.getAllocatedByEmployeeId() == null || employees.findById(row.getAllocatedByEmployeeId()).isEmpty()) {
                errors.add(prefix + ".allocatedByEmployeeId", "请选择核销人");
            }
            String currency = row.getCurrency() == null ? form.getCurrency() : row.getCurrency();
            if (form.getCurrency() != null && !form.getCurrency().equals(currency)) {
                errors.add(prefix + ".currency", "核销币种须与收付款一致");
            }
            if (Codes.SETTLEMENT_REVERSE.equals(row.getSettlementAction())) {
                PaymentAllocation target = row.getReversesAllocationId() == null
                        ? null : allocations.findById(row.getReversesAllocationId()).orElse(null);
                if (target == null || !Codes.SETTLEMENT_APPLY.equals(target.settlementAction)
                        || !Codes.PAYMENT_CONFIRMED.equals(target.status)) {
                    errors.add(prefix + ".reversesAllocationId", "撤销须指向一笔已确认的核销");
                } else {
                    if (!Cmp.eq(target.receivableItemId, row.getReceivableItemId())
                            || !Cmp.eq(target.payableItemId, row.getPayableItemId())) {
                        errors.add(prefix, "撤销的结算项须与原核销一致");
                    }
                    Long expectedPayment = Codes.PAYMENT_REFUND.equals(form.getPaymentKind())
                            ? form.getRefundOfPaymentId() : form.getId();
                    if (expectedPayment != null && !expectedPayment.equals(target.paymentId)) {
                        errors.add(prefix + ".reversesAllocationId", "撤销须指向对应的原收付款核销");
                    }
                    BigDecimal reversed = row.getSettlementAmount() == null ? BigDecimal.ZERO : row.getSettlementAmount();
                    for (PaymentAllocation other : allocations.findByReversesAllocationId(target.id)) {
                        if (!other.id.equals(row.getId()) && Codes.PAYMENT_CONFIRMED.equals(other.status)) {
                            reversed = reversed.add(other.settlementAmount);
                        }
                    }
                    if (reversed.compareTo(target.settlementAmount) > 0) {
                        errors.add(prefix + ".settlementAmount", "撤销合计不能超过原核销");
                    }
                }
            } else if (row.getReversesAllocationId() != null) {
                errors.add(prefix + ".reversesAllocationId", "核销不要填写被撤销的原记录");
            }
            if (row.getId() != null) {
                PaymentAllocation existing = allocations.findById(row.getId()).orElse(null);
                if (existing != null && Codes.PAYMENT_CONFIRMED.equals(existing.status)
                        && existing.settlementAmount.compareTo(row.getSettlementAmount()) != 0) {
                    errors.add(prefix, "已确认的核销不能修改");
                }
            }
            if (Codes.PAYMENT_CONFIRMED.equals(row.getStatus()) && row.getSettlementAmount() != null) {
                BigDecimal signed = Codes.SETTLEMENT_REVERSE.equals(row.getSettlementAction())
                        ? row.getSettlementAmount().negate() : row.getSettlementAmount();
                if (row.getReceivableItemId() != null) {
                    receivableSide = receivableSide.add(signed);
                    checkReceivable(form, row, errors, prefix);
                } else {
                    payableSide = payableSide.add(signed);
                    checkPayable(form, row, errors, prefix);
                }
            }
        }
        if (form.getPaymentAmount() != null) {
            if (receivableSide.compareTo(form.getPaymentAmount()) > 0 || payableSide.compareTo(form.getPaymentAmount()) > 0) {
                errors.add("allocations", "同一侧核销不能超过本次收付本金");
            }
        }
        checkCombinedOutstanding(form, rows, errors);
        if (stored != null) {
            for (PaymentAllocation existing : allocations.findByPaymentIdOrderByIdAsc(stored.id)) {
                if (!Codes.PAYMENT_CONFIRMED.equals(existing.status)) {
                    continue;
                }
                boolean kept = rows.stream().anyMatch(row -> existing.id.equals(row.getId()) && !row.isRemoved());
                if (!kept) {
                    errors.add("allocations", "已确认的核销不能删除");
                }
            }
        }
    }

    private void checkCombinedOutstanding(PaymentForm form, List<AllocationRow> rows, FieldErrors errors) {
        Map<Long, BigDecimal> receivableNet = new LinkedHashMap<>();
        Map<Long, BigDecimal> payableNet = new LinkedHashMap<>();
        for (AllocationRow row : rows) {
            if (!Codes.PAYMENT_CONFIRMED.equals(row.getStatus()) || row.getSettlementAmount() == null) {
                continue;
            }
            BigDecimal signed = Codes.SETTLEMENT_REVERSE.equals(row.getSettlementAction())
                    ? row.getSettlementAmount().negate() : row.getSettlementAmount();
            if (row.getReceivableItemId() != null) {
                receivableNet.merge(row.getReceivableItemId(), signed, BigDecimal::add);
            } else if (row.getPayableItemId() != null) {
                payableNet.merge(row.getPayableItemId(), signed, BigDecimal::add);
            }
        }
        for (Map.Entry<Long, BigDecimal> entry : receivableNet.entrySet()) {
            BigDecimal room = outstandingReceivable(entry.getKey());
            if (room == null) {
                continue;
            }
            room = room.add(postedNet(form.getId(), entry.getKey(), true));
            if (entry.getValue().compareTo(room) > 0) {
                errors.add("allocations", "同一应收的核销合计超过未结余额");
            }
        }
        for (Map.Entry<Long, BigDecimal> entry : payableNet.entrySet()) {
            BigDecimal room = outstandingPayable(entry.getKey());
            if (room == null) {
                continue;
            }
            room = room.add(postedNet(form.getId(), entry.getKey(), false));
            if (entry.getValue().compareTo(room) > 0) {
                errors.add("allocations", "同一应付的核销合计超过未结余额");
            }
        }
    }

    private BigDecimal postedNet(Long paymentId, Long itemId, boolean receivable) {
        if (paymentId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal net = BigDecimal.ZERO;
        for (PaymentAllocation allocation : allocations.findByPaymentIdOrderByIdAsc(paymentId)) {
            if (!Codes.PAYMENT_CONFIRMED.equals(allocation.status)) {
                continue;
            }
            boolean sameItem = receivable
                    ? itemId.equals(allocation.receivableItemId)
                    : itemId.equals(allocation.payableItemId);
            if (!sameItem) {
                continue;
            }
            if (Codes.SETTLEMENT_APPLY.equals(allocation.settlementAction)) {
                net = net.add(allocation.settlementAmount);
            } else if (Codes.SETTLEMENT_REVERSE.equals(allocation.settlementAction)) {
                net = net.subtract(allocation.settlementAmount);
            }
        }
        return net;
    }

    private void checkReceivable(PaymentForm form, AllocationRow row, FieldErrors errors, String prefix) {
        ReceivableItem item = receivables.findById(row.getReceivableItemId()).orElse(null);
        if (item == null || !Codes.PAYABLE_CONFIRMED.equals(item.status)) {
            errors.add(prefix + ".receivableItemId", "只能核销已确认的应收");
            return;
        }
        if (!item.currency.equals(form.getCurrency())) {
            errors.add(prefix + ".currency", "币种须与应收一致");
        }
        if (Codes.SETTLEMENT_APPLY.equals(row.getSettlementAction())) {
            if (!sameParty(form.getPayeeCompanyId(), form.getPayeeLegalPersonId(), form.getPayeePersonId(),
                    item.creditorCompanyId, item.creditorLegalPersonId, null)) {
                errors.add("payeeCompanyId", "收款方须是应收的债权人");
            }
            boolean payerIsDebtor = sameParty(form.getPayerCompanyId(), form.getPayerLegalPersonId(), form.getPayerPersonId(),
                    item.debtorCompanyId, item.debtorLegalPersonId, item.debtorPersonId);
            boolean payingForDebtor = sameParty(form.getPaymentForCompanyId(), form.getPaymentForLegalPersonId(), form.getPaymentForPersonId(),
                    item.debtorCompanyId, item.debtorLegalPersonId, item.debtorPersonId);
            if (payerIsDebtor && hasPaymentFor(form)) {
                errors.add("paymentForCompanyId", "债务人本人付款时不要填写被代付对象");
            }
            if (!payerIsDebtor && !payingForDebtor) {
                errors.add("paymentForCompanyId", "代付须把被代付对象记成这笔应收的债务人");
            }
        }
    }

    private void checkPayable(PaymentForm form, AllocationRow row, FieldErrors errors, String prefix) {
        PayableItem item = payables.findById(row.getPayableItemId()).orElse(null);
        if (item == null || !Codes.PAYABLE_CONFIRMED.equals(item.status)) {
            errors.add(prefix + ".payableItemId", "只能核销已确认的应付");
            return;
        }
        if (!item.currency.equals(form.getCurrency())) {
            errors.add(prefix + ".currency", "币种须与应付一致");
        }
        boolean payerIsDebtor = sameParty(form.getPayerCompanyId(), form.getPayerLegalPersonId(), form.getPayerPersonId(),
                item.debtorCompanyId, item.debtorLegalPersonId, null);
        boolean payingForDebtor = sameParty(form.getPaymentForCompanyId(), form.getPaymentForLegalPersonId(), form.getPaymentForPersonId(),
                item.debtorCompanyId, item.debtorLegalPersonId, null);
        if (!payerIsDebtor && !payingForDebtor && Codes.SETTLEMENT_APPLY.equals(row.getSettlementAction())) {
            errors.add("payerCompanyId", "付款方须是应付的债务人，或标明代其付款");
        }
    }

    private void syncAllocations(Payment payment, List<AllocationRow> rows) {
        for (AllocationRow row : rows) {
            if (row.getId() != null) {
                continue;
            }
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.paymentId = payment.id;
            allocation.receivableItemId = row.getReceivableItemId();
            allocation.payableItemId = row.getPayableItemId();
            allocation.settlementAmount = row.getSettlementAmount();
            allocation.currency = row.getCurrency() == null ? payment.currency : row.getCurrency();
            allocation.settlementAction = row.getSettlementAction();
            allocation.reversesAllocationId = row.getReversesAllocationId();
            allocation.allocatedAt = row.getAllocatedAt() == null
                    ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                    : row.getAllocatedAt().truncatedTo(ChronoUnit.MINUTES);
            allocation.allocatedByEmployeeId = row.getAllocatedByEmployeeId();
            allocation.settlementBasisDocumentId = row.getSettlementBasisDocumentId();
            allocation.status = row.getStatus();
            allocations.save(allocation);
        }
    }

    private void syncReceivableAdjustments(ReceivableItem item, List<AdjustmentRow> rows, FieldErrors errors) {
        BigDecimal principal = item.receivableAmount;
        for (AdjustmentRow row : rows) {
            if (row.isRemoved() || blankAdjustment(row)) {
                continue;
            }
            if (Codes.ADJUSTMENT_APPROVED.equals(row.getStatus())) {
                principal = principal.add(nz(row.getIncreaseAmount())).subtract(nz(row.getDecreaseAmount()));
            }
        }
        BigDecimal settled = net(allocations.findByReceivableItemId(item.id));
        if (principal.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("adjustments", "调整后本金不能小于 0");
        } else if (principal.compareTo(settled) < 0) {
            errors.add("adjustments", "调整后本金小于已结算金额，须先撤销或退款");
        }
        if (errors.hasErrors()) {
            return;
        }
        for (AdjustmentRow row : rows) {
            if (row.isRemoved() || blankAdjustment(row) || row.getId() != null) {
                continue;
            }
            ReceivableAdjustment adjustment = new ReceivableAdjustment();
            adjustment.receivableItemId = item.id;
            adjustment.increaseReceivableAmount = nz(row.getIncreaseAmount());
            adjustment.decreaseReceivableAmount = nz(row.getDecreaseAmount());
            adjustment.currency = item.currency;
            adjustment.adjustedAt = row.getAdjustedAt() == null
                    ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES) : row.getAdjustedAt();
            adjustment.adjustmentReason = row.getAdjustmentReason();
            adjustment.sourceDocumentId = row.getSourceDocumentId();
            adjustment.approvedByEmployeeId = row.getApprovedByEmployeeId();
            adjustment.approvedAt = row.getApprovedAt();
            adjustment.status = row.getStatus();
            receivableAdjustments.save(adjustment);
        }
    }

    private void syncPayableAdjustments(PayableItem item, List<AdjustmentRow> rows, FieldErrors errors) {
        BigDecimal principal = item.payableAmount;
        for (AdjustmentRow row : rows) {
            if (row.isRemoved() || blankAdjustment(row)) {
                continue;
            }
            if (Codes.ADJUSTMENT_APPROVED.equals(row.getStatus())) {
                principal = principal.add(nz(row.getIncreaseAmount())).subtract(nz(row.getDecreaseAmount()));
            }
        }
        BigDecimal settled = net(allocations.findByPayableItemId(item.id));
        if (principal.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("adjustments", "调整后本金不能小于 0");
        } else if (principal.compareTo(settled) < 0) {
            errors.add("adjustments", "调整后本金小于已结算金额，须先撤销或退款");
        }
        if (errors.hasErrors()) {
            return;
        }
        for (AdjustmentRow row : rows) {
            if (row.isRemoved() || blankAdjustment(row) || row.getId() != null) {
                continue;
            }
            PayableAdjustment adjustment = new PayableAdjustment();
            adjustment.payableItemId = item.id;
            adjustment.increasePayableAmount = nz(row.getIncreaseAmount());
            adjustment.decreasePayableAmount = nz(row.getDecreaseAmount());
            adjustment.currency = item.currency;
            adjustment.adjustedAt = row.getAdjustedAt() == null
                    ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES) : row.getAdjustedAt();
            adjustment.adjustmentReason = row.getAdjustmentReason();
            adjustment.sourceDocumentId = row.getSourceDocumentId();
            adjustment.approvedByEmployeeId = row.getApprovedByEmployeeId();
            adjustment.approvedAt = row.getApprovedAt();
            adjustment.status = row.getStatus();
            payableAdjustments.save(adjustment);
        }
    }

    private void validateAdjustmentRows(List<AdjustmentRow> rows, String currency, FieldErrors errors) {
        if (rows == null) {
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            AdjustmentRow row = rows.get(i);
            if (row.isRemoved() || blankAdjustment(row)) {
                continue;
            }
            String prefix = "adjustments[" + i + "]";
            BigDecimal increase = nz(row.getIncreaseAmount());
            BigDecimal decrease = nz(row.getDecreaseAmount());
            if (increase.signum() < 0 || decrease.signum() < 0) {
                errors.add(prefix, "调整金额不能为负");
            }
            if (increase.signum() > 0 && decrease.signum() > 0) {
                errors.add(prefix, "一条调整只增加或只减少");
            }
            if (increase.signum() == 0 && decrease.signum() == 0) {
                errors.add(prefix, "增加或减少的一侧须大于 0");
            }
            if (row.getAdjustmentReason() == null) {
                errors.add(prefix + ".adjustmentReason", "请填写调整原因");
            }
            if (row.getStatus() == null || !Codes.ADJUSTMENT_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择调整状态");
            }
            if (Codes.ADJUSTMENT_APPROVED.equals(row.getStatus())) {
                if (row.getApprovedByEmployeeId() == null || employees.findById(row.getApprovedByEmployeeId()).isEmpty()) {
                    errors.add(prefix + ".approvedByEmployeeId", "已批准的调整须选择审批人");
                }
                if (row.getApprovedAt() == null) {
                    errors.add(prefix + ".approvedAt", "已批准的调整须填写时间");
                }
            }
            documents.require(row.getSourceDocumentId(), errors, prefix + ".sourceDocumentId");
            if (row.getCurrency() != null && currency != null && !currency.equals(row.getCurrency())) {
                errors.add(prefix + ".currency", "调整币种须与原项一致");
            }
        }
    }

    private BigDecimal adjustedReceivable(ReceivableItem item) {
        BigDecimal amount = item.receivableAmount;
        for (ReceivableAdjustment adjustment : receivableAdjustments.findByReceivableItemIdOrderByIdAsc(item.id)) {
            if (Codes.ADJUSTMENT_APPROVED.equals(adjustment.status)) {
                amount = amount.add(adjustment.increaseReceivableAmount).subtract(adjustment.decreaseReceivableAmount);
            }
        }
        return amount;
    }

    private BigDecimal adjustedPayable(PayableItem item) {
        BigDecimal amount = item.payableAmount;
        for (PayableAdjustment adjustment : payableAdjustments.findByPayableItemIdOrderByIdAsc(item.id)) {
            if (Codes.ADJUSTMENT_APPROVED.equals(adjustment.status)) {
                amount = amount.add(adjustment.increasePayableAmount).subtract(adjustment.decreasePayableAmount);
            }
        }
        return amount;
    }

    private BigDecimal unallocated(Payment payment) {
        if (!Codes.PAYMENT_CONFIRMED.equals(payment.status)) {
            return null;
        }
        BigDecimal used = BigDecimal.ZERO;
        boolean refund = Codes.PAYMENT_REFUND.equals(payment.paymentKind);
        for (PaymentAllocation allocation : allocations.findByPaymentIdOrderByIdAsc(payment.id)) {
            if (!Codes.PAYMENT_CONFIRMED.equals(allocation.status)) {
                continue;
            }
            if (refund) {
                if (Codes.SETTLEMENT_REVERSE.equals(allocation.settlementAction)) {
                    used = used.add(allocation.settlementAmount);
                }
            } else if (Codes.SETTLEMENT_APPLY.equals(allocation.settlementAction)) {
                used = used.add(allocation.settlementAmount);
            } else if (Codes.SETTLEMENT_REVERSE.equals(allocation.settlementAction)) {
                used = used.subtract(allocation.settlementAmount);
            }
        }
        BigDecimal side = used.max(BigDecimal.ZERO);
        return payment.paymentAmount.subtract(side);
    }

    private static BigDecimal net(List<PaymentAllocation> rows) {
        BigDecimal amount = BigDecimal.ZERO;
        for (PaymentAllocation row : rows) {
            if (!Codes.PAYMENT_CONFIRMED.equals(row.status)) {
                continue;
            }
            if (Codes.SETTLEMENT_APPLY.equals(row.settlementAction)) {
                amount = amount.add(row.settlementAmount);
            } else if (Codes.SETTLEMENT_REVERSE.equals(row.settlementAction)) {
                amount = amount.subtract(row.settlementAmount);
            }
        }
        return amount;
    }

    private void copyReceivable(ReceivableItem item, ReceivableForm form) {
        item.receivableNumber = form.getReceivableNumber();
        item.creditorCompanyId = form.getCreditorCompanyId();
        item.creditorLegalPersonId = form.getCreditorLegalPersonId();
        item.debtorCompanyId = form.getDebtorCompanyId();
        item.debtorLegalPersonId = form.getDebtorLegalPersonId();
        item.debtorPersonId = form.getDebtorPersonId();
        item.contractId = form.getContractId();
        item.contractPaymentScheduleId = form.getContractPaymentScheduleId();
        item.projectId = form.getProjectId();
        item.contractStageId = form.getContractStageId();
        item.receivablePurpose = form.getReceivablePurpose();
        if (item.id == null || !Codes.PAYABLE_CONFIRMED.equals(item.status)) {
            item.receivableAmount = form.getReceivableAmount();
        }
        item.currency = form.getCurrency();
        item.confirmedAt = form.getConfirmedAt();
        item.confirmedByEmployeeId = form.getConfirmedByEmployeeId();
        item.dueDate = form.getDueDate();
        item.triggerAcceptanceEventId = form.getTriggerAcceptanceEventId();
        item.triggerInvoiceId = form.getTriggerInvoiceId();
        item.sourceDocumentId = form.getSourceDocumentId();
        item.status = form.getStatus();
    }

    private void copyPayable(PayableItem item, PayableForm form) {
        item.payableNumber = form.getPayableNumber();
        item.debtorCompanyId = form.getDebtorCompanyId();
        item.debtorLegalPersonId = form.getDebtorLegalPersonId();
        item.creditorCompanyId = form.getCreditorCompanyId();
        item.creditorLegalPersonId = form.getCreditorLegalPersonId();
        item.creditorPersonId = form.getCreditorPersonId();
        item.supplierId = form.getSupplierId();
        item.contractId = form.getContractId();
        item.contractPaymentScheduleId = form.getContractPaymentScheduleId();
        item.projectId = form.getProjectId();
        item.contractStageId = form.getContractStageId();
        item.costOccurrenceId = form.getCostOccurrenceId();
        item.expenseClaimId = form.getExpenseClaimId();
        item.payablePurpose = form.getPayablePurpose();
        if (item.id == null || !Codes.PAYABLE_CONFIRMED.equals(item.status)) {
            item.payableAmount = form.getPayableAmount();
        }
        item.currency = form.getCurrency();
        item.confirmedAt = form.getConfirmedAt();
        item.confirmedByEmployeeId = form.getConfirmedByEmployeeId();
        item.dueDate = form.getDueDate();
        item.triggerAcceptanceEventId = form.getTriggerAcceptanceEventId();
        item.triggerInvoiceId = form.getTriggerInvoiceId();
        item.sourceDocumentId = form.getSourceDocumentId();
        item.status = form.getStatus();
    }

    private void copyPayment(Payment payment, PaymentForm form) {
        payment.payerCompanyId = form.getPayerCompanyId();
        payment.payerLegalPersonId = form.getPayerLegalPersonId();
        payment.payerPersonId = form.getPayerPersonId();
        payment.payeeCompanyId = form.getPayeeCompanyId();
        payment.payeeLegalPersonId = form.getPayeeLegalPersonId();
        payment.payeePersonId = form.getPayeePersonId();
        payment.paymentForCompanyId = form.getPaymentForCompanyId();
        payment.paymentForLegalPersonId = form.getPaymentForLegalPersonId();
        payment.paymentForPersonId = form.getPaymentForPersonId();
        payment.receiptForCompanyId = form.getReceiptForCompanyId();
        payment.receiptForLegalPersonId = form.getReceiptForLegalPersonId();
        payment.receiptForPersonId = form.getReceiptForPersonId();
        payment.agencyBasisDocumentId = form.getAgencyBasisDocumentId();
        payment.paymentNumber = form.getPaymentNumber();
        payment.paymentKind = form.getPaymentKind();
        payment.paymentPurpose = form.getPaymentPurpose();
        payment.relatedEmployeeId = form.getRelatedEmployeeId();
        payment.refundOfPaymentId = form.getRefundOfPaymentId();
        payment.payerCompanyBankAccountId = form.getPayerCompanyBankAccountId();
        payment.payeeCompanyBankAccountId = form.getPayeeCompanyBankAccountId();
        payment.paidAt = form.getPaidAt().truncatedTo(ChronoUnit.MINUTES);
        payment.paymentAmount = form.getPaymentAmount();
        payment.currency = form.getCurrency();
        payment.bankValueDate = form.getBankValueDate();
        payment.bankFeeAmount = form.getBankFeeAmount();
        payment.paymentChannel = form.getPaymentChannel();
        payment.payerBankAccountNumberSnapshot = form.getPayerBankAccountNumberSnapshot();
        payment.payerBankAccountNameSnapshot = form.getPayerBankAccountNameSnapshot();
        payment.payerBankNameSnapshot = form.getPayerBankNameSnapshot();
        payment.payeeBankAccountNumberSnapshot = form.getPayeeBankAccountNumberSnapshot();
        payment.payeeBankAccountNameSnapshot = form.getPayeeBankAccountNameSnapshot();
        payment.payeeBankNameSnapshot = form.getPayeeBankNameSnapshot();
        payment.bankTransactionNumber = form.getBankTransactionNumber();
        payment.paymentPlatformTransactionNumber = form.getPaymentPlatformTransactionNumber();
        payment.confirmedByEmployeeId = form.getConfirmedByEmployeeId();
        payment.confirmedAt = form.getConfirmedAt();
        payment.sourceDocumentId = form.getSourceDocumentId();
        payment.status = form.getStatus();
    }

    private ReceivableItem loadReceivable(Long id, FieldErrors errors) {
        if (id == null) {
            return null;
        }
        ReceivableItem stored = receivables.findById(id).orElse(null);
        if (stored == null) {
            errors.add("id", "应收不存在");
        }
        return stored;
    }

    private static List<AllocationRow> activeAllocations(PaymentForm form) {
        List<AllocationRow> rows = new ArrayList<>();
        if (form.getAllocations() == null) {
            return rows;
        }
        for (AllocationRow row : form.getAllocations()) {
            if (row.isRemoved() || (row.getId() == null && row.getReceivableItemId() == null
                    && row.getPayableItemId() == null && row.getSettlementAmount() == null)) {
                continue;
            }
            rows.add(row);
        }
        return rows;
    }

    private void normalizeItem(ReceivableForm form) {
        form.setReceivableNumber(Text.blankToNull(form.getReceivableNumber()));
        form.setReceivablePurpose(Text.blankToNull(form.getReceivablePurpose()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        normalizeAdjustments(form.getAdjustments());
    }

    private void normalizePayable(PayableForm form) {
        form.setPayableNumber(Text.blankToNull(form.getPayableNumber()));
        form.setPayablePurpose(Text.blankToNull(form.getPayablePurpose()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        normalizeAdjustments(form.getAdjustments());
    }

    private void normalizeAdjustments(List<AdjustmentRow> rows) {
        if (rows == null) {
            return;
        }
        for (AdjustmentRow row : rows) {
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            row.setAdjustmentReason(Text.blankToNull(row.getAdjustmentReason()));
            row.setStatus(Text.blankToNull(row.getStatus()));
        }
    }

    private void normalizePayment(PaymentForm form) {
        form.setPaymentNumber(Text.blankToNull(form.getPaymentNumber()));
        form.setPaymentKind(Text.blankToNull(form.getPaymentKind()));
        form.setPaymentPurpose(Text.blankToNull(form.getPaymentPurpose()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setPaymentChannel(Text.blankToNull(form.getPaymentChannel()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        form.setPayerBankAccountNumberSnapshot(Text.blankToNull(form.getPayerBankAccountNumberSnapshot()));
        form.setPayerBankAccountNameSnapshot(Text.blankToNull(form.getPayerBankAccountNameSnapshot()));
        form.setPayerBankNameSnapshot(Text.blankToNull(form.getPayerBankNameSnapshot()));
        form.setPayeeBankAccountNumberSnapshot(Text.blankToNull(form.getPayeeBankAccountNumberSnapshot()));
        form.setPayeeBankAccountNameSnapshot(Text.blankToNull(form.getPayeeBankAccountNameSnapshot()));
        form.setPayeeBankNameSnapshot(Text.blankToNull(form.getPayeeBankNameSnapshot()));
        form.setBankTransactionNumber(Text.blankToNull(form.getBankTransactionNumber()));
        form.setPaymentPlatformTransactionNumber(Text.blankToNull(form.getPaymentPlatformTransactionNumber()));
        if (form.getAllocations() == null) {
            form.setAllocations(List.of());
        }
        for (AllocationRow row : form.getAllocations()) {
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            row.setSettlementAction(Text.blankToNull(row.getSettlementAction()));
            row.setStatus(Text.blankToNull(row.getStatus()));
        }
    }

    private static boolean reversedParties(PaymentForm refund, Payment original) {
        return sameParty(refund.getPayerCompanyId(), refund.getPayerLegalPersonId(), refund.getPayerPersonId(),
                original.payeeCompanyId, original.payeeLegalPersonId, original.payeePersonId)
                && sameParty(refund.getPayeeCompanyId(), refund.getPayeeLegalPersonId(), refund.getPayeePersonId(),
                original.payerCompanyId, original.payerLegalPersonId, original.payerPersonId);
    }

    private static boolean sameParty(Long company, Long legal, Long person, Long otherCompany, Long otherLegal, Long otherPerson) {
        return Cmp.eq(company, otherCompany) && Cmp.eq(legal, otherLegal) && Cmp.eq(person, otherPerson);
    }

    private static boolean hasPaymentFor(PaymentForm form) {
        return form.getPaymentForCompanyId() != null || form.getPaymentForLegalPersonId() != null
                || form.getPaymentForPersonId() != null;
    }

    private static boolean hasBankSnapshot(PaymentForm form) {
        return hasBankNumber(form) || form.getPayerBankNameSnapshot() != null || form.getPayeeBankNameSnapshot() != null;
    }

    private static boolean hasBankNumber(PaymentForm form) {
        return form.getPayerBankAccountNumberSnapshot() != null || form.getPayeeBankAccountNumberSnapshot() != null;
    }

    private static boolean blankAdjustment(AdjustmentRow row) {
        return row.getId() == null && row.getIncreaseAmount() == null && row.getDecreaseAmount() == null
                && row.getAdjustmentReason() == null;
    }

    private static AdjustmentRow adjustmentRow(
            Long id, BigDecimal increase, BigDecimal decrease, String currency, LocalDateTime adjustedAt,
            String reason, Long documentId, Long approverId, LocalDateTime approvedAt, String status) {
        AdjustmentRow row = new AdjustmentRow();
        row.setId(id);
        row.setIncreaseAmount(increase);
        row.setDecreaseAmount(decrease);
        row.setCurrency(currency);
        row.setAdjustedAt(adjustedAt);
        row.setAdjustmentReason(reason);
        row.setSourceDocumentId(documentId);
        row.setApprovedByEmployeeId(approverId);
        row.setApprovedAt(approvedAt);
        row.setStatus(status);
        return row;
    }

    private void requireParty(Long id, CompanyRepository repository, String field, String message, FieldErrors errors) {
        if (id != null && repository.findById(id).isEmpty()) {
            errors.add(field, message);
        }
    }

    private static int count(Long... ids) {
        int n = 0;
        for (Long id : ids) {
            if (id != null) {
                n++;
            }
        }
        return n;
    }

    private static BigDecimal nz(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static BigDecimal plain(BigDecimal amount) {
        return amount == null ? null : LaborCostRateService.plain(amount);
    }
}
