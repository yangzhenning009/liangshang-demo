package com.liangshan.demo.service;

import com.liangshan.demo.domain.AccountingAccount;
import com.liangshan.demo.domain.AccountingPeriod;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.JournalEntry;
import com.liangshan.demo.domain.JournalLine;
import com.liangshan.demo.domain.JournalLineSource;
import com.liangshan.demo.domain.Ledger;
import com.liangshan.demo.repository.AccountingAccountRepository;
import com.liangshan.demo.repository.AccountingPeriodRepository;
import com.liangshan.demo.repository.CostAllocationRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.InvoiceLineRepository;
import com.liangshan.demo.repository.JournalEntryRepository;
import com.liangshan.demo.repository.JournalLineRepository;
import com.liangshan.demo.repository.JournalLineSourceRepository;
import com.liangshan.demo.repository.LedgerRepository;
import com.liangshan.demo.repository.PayableAdjustmentRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.PaymentRepository;
import com.liangshan.demo.repository.ReceivableAdjustmentRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.repository.RevenueRecognitionRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.JournalForm;
import com.liangshan.demo.web.form.JournalForm.LineRow;
import com.liangshan.demo.web.form.JournalForm.SourceRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository entries;
    private final JournalLineRepository lines;
    private final JournalLineSourceRepository sources;
    private final LedgerRepository ledgers;
    private final AccountingPeriodRepository periods;
    private final AccountingAccountRepository accounts;
    private final EmployeeRepository employees;
    private final InvoiceLineRepository invoiceLines;
    private final PaymentRepository payments;
    private final ReceivableItemRepository receivables;
    private final PayableItemRepository payables;
    private final ReceivableAdjustmentRepository receivableAdjustments;
    private final PayableAdjustmentRepository payableAdjustments;
    private final CostAllocationRepository costAllocations;
    private final CostOccurrenceRepository costs;
    private final RevenueRecognitionRepository revenues;
    private final SourceDocumentService documents;
    private final LedgerService ledgerService;

    public record Row(Long id, String voucherNumber, String status, String description, java.time.LocalDate voucherDate) {
    }

    public List<Row> list() {
        return entries.findAllByOrderByVoucherDateDescIdDesc().stream()
                .map(entry -> new Row(entry.id, entry.voucherNumber, entry.status, entry.entryDescription, entry.voucherDate))
                .toList();
    }

    public JournalEntry get(Long id) {
        return entries.findById(id).orElseThrow(NotFoundException::new);
    }

    public JournalForm form(Long id) {
        JournalEntry entry = get(id);
        JournalForm form = new JournalForm();
        form.setId(entry.id);
        form.setLedgerId(entry.ledgerId);
        form.setAccountingPeriodId(entry.accountingPeriodId);
        form.setVoucherNumber(entry.voucherNumber);
        form.setVoucherDate(entry.voucherDate);
        form.setEntryDescription(entry.entryDescription);
        form.setPreparedByEmployeeId(entry.preparedByEmployeeId);
        form.setReviewedByEmployeeId(entry.reviewedByEmployeeId);
        form.setReviewedAt(entry.reviewedAt);
        form.setAdjustmentReason(entry.adjustmentReason);
        form.setReversalOfEntryId(entry.reversalOfEntryId);
        form.setStatus(entry.status);
        Map<Long, Integer> numbers = new HashMap<>();
        for (JournalLine line : lines.findByJournalEntryIdOrderByLineNumberAscIdAsc(id)) {
            LineRow row = new LineRow();
            row.setId(line.id);
            row.setLineNumber(line.lineNumber);
            row.setAccountingAccountId(line.accountingAccountId);
            row.setLineDescription(line.lineDescription);
            row.setDebitAmount(line.debitAmount);
            row.setCreditAmount(line.creditAmount);
            row.setCurrency(line.currency);
            row.setCounterpartyCompanyId(line.counterpartyCompanyId);
            row.setCounterpartyLegalPersonId(line.counterpartyLegalPersonId);
            row.setCounterpartyPersonId(line.counterpartyPersonId);
            row.setEmployeeId(line.employeeId);
            row.setDepartmentId(line.departmentId);
            row.setProjectId(line.projectId);
            row.setContractStageId(line.contractStageId);
            row.setSourceDocumentId(line.sourceDocumentId);
            form.getLines().add(row);
            numbers.put(line.id, line.lineNumber);
        }
        for (JournalLine line : lines.findByJournalEntryIdOrderByLineNumberAscIdAsc(id)) {
            for (JournalLineSource source : sources.findByJournalLineIdOrderByIdAsc(line.id)) {
                SourceRow row = new SourceRow();
                row.setId(source.id);
                row.setLineNumber(numbers.get(line.id));
                row.setInvoiceLineId(source.invoiceLineId);
                row.setPaymentId(source.paymentId);
                row.setReceivableItemId(source.receivableItemId);
                row.setPayableItemId(source.payableItemId);
                row.setReceivableAdjustmentId(source.receivableAdjustmentId);
                row.setPayableAdjustmentId(source.payableAdjustmentId);
                row.setCostAllocationId(source.costAllocationId);
                row.setCostOccurrenceId(source.costOccurrenceId);
                row.setRevenueRecognitionId(source.revenueRecognitionId);
                row.setSourceContributionAmount(source.sourceContributionAmount);
                row.setCurrency(source.currency);
                row.setSourceDocumentId(source.sourceDocumentId);
                form.getSources().add(row);
            }
        }
        return form;
    }

    @Transactional
    public long save(JournalForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        JournalEntry stored = form.getId() == null ? null : entries.findById(form.getId()).orElse(null);
        if (form.getId() != null && stored == null) {
            errors.add("id", "凭证不存在");
        }
        if (stored != null && Codes.JOURNAL_POSTED.equals(stored.status)) {
            errors.add("status", "已过账的凭证不能修改，请做冲销");
        }
        validate(form, errors);
        errors.throwIfAny();
        JournalEntry entry = stored == null ? new JournalEntry() : stored;
        copy(entry, form);
        entry.status = Codes.JOURNAL_DRAFT;
        entry.postedAt = null;
        entry.postedByEmployeeId = null;
        entry = entries.save(entry);
        sync(entry, form, errors);
        errors.throwIfAny();
        return entry.id;
    }

    @Transactional
    public void post(Long id, Long employeeId) {
        JournalEntry entry = get(id);
        FieldErrors errors = new FieldErrors();
        if (Codes.JOURNAL_POSTED.equals(entry.status)) {
            errors.add("status", "凭证已经过账");
        }
        if (employeeId == null || employees.findById(employeeId).isEmpty()) {
            errors.add("postedByEmployeeId", "请选择过账人");
        }
        AccountingPeriod period = periods.findById(entry.accountingPeriodId).orElse(null);
        if (period == null || !Codes.PERIOD_OPEN.equals(period.periodStatus)) {
            errors.add("accountingPeriodId", "已关闭期间不能过账");
        } else if (entry.voucherDate.isBefore(period.periodStartDate) || entry.voucherDate.isAfter(period.periodEndDate)) {
            errors.add("voucherDate", "凭证日期不在期间内");
        }
        List<JournalLine> storedLines = lines.findByJournalEntryIdOrderByLineNumberAscIdAsc(id);
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (JournalLine line : storedLines) {
            debit = debit.add(line.debitAmount);
            credit = credit.add(line.creditAmount);
            AccountingAccount account = accounts.findById(line.accountingAccountId).orElse(null);
            if (!ledgerService.accountUsable(account, entry.voucherDate) || account == null
                    || !entry.ledgerId.equals(account.ledgerId)) {
                errors.add("lines", "过账科目须为启用中的记账科目，且属于本账簿");
            }
        }
        if (debit.compareTo(credit) != 0) {
            errors.add("lines", "借贷不平衡，不能过账");
        }
        if (debit.signum() == 0) {
            errors.add("lines", "没有可过账的金额");
        }
        errors.throwIfAny();
        entry.status = Codes.JOURNAL_POSTED;
        entry.postedByEmployeeId = employeeId;
        entry.postedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        entries.save(entry);
    }

    @Transactional
    public long reverse(Long id, Long employeeId, String reason) {
        JournalEntry original = get(id);
        FieldErrors errors = new FieldErrors();
        String cleanReason = Text.blankToNull(reason);
        if (!Codes.JOURNAL_POSTED.equals(original.status)) {
            errors.add("status", "只有已过账的凭证可以冲销");
        }
        if (employeeId == null || employees.findById(employeeId).isEmpty()) {
            errors.add("preparedByEmployeeId", "请选择冲销人");
        }
        if (cleanReason == null) {
            errors.add("adjustmentReason", "冲销须填写原因");
        }
        AccountingPeriod period = periods.findById(original.accountingPeriodId).orElse(null);
        if (period == null || !Codes.PERIOD_OPEN.equals(period.periodStatus)) {
            errors.add("accountingPeriodId", "已关闭期间不能过账");
        }
        for (JournalEntry other : entries.findByReversalOfEntryId(id)) {
            if (Codes.JOURNAL_POSTED.equals(other.status)) {
                errors.add("reversalOfEntryId", "该凭证已经冲销");
            }
        }
        errors.throwIfAny();
        JournalEntry reversal = new JournalEntry();
        reversal.ledgerId = original.ledgerId;
        reversal.accountingPeriodId = original.accountingPeriodId;
        reversal.voucherNumber = original.voucherNumber + "-R";
        reversal.voucherDate = original.voucherDate;
        reversal.entryDescription = "冲销 " + original.voucherNumber;
        reversal.preparedByEmployeeId = employeeId;
        reversal.postedByEmployeeId = employeeId;
        reversal.postedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        reversal.status = Codes.JOURNAL_POSTED;
        reversal.adjustmentReason = cleanReason;
        reversal.reversalOfEntryId = original.id;
        String number = reversal.voucherNumber;
        int suffix = 2;
        while (!entries.findByLedgerIdAndAccountingPeriodIdAndVoucherNumber(
                reversal.ledgerId, reversal.accountingPeriodId, number).isEmpty()) {
            number = original.voucherNumber + "-R" + suffix;
            suffix++;
        }
        reversal.voucherNumber = number;
        reversal = entries.save(reversal);
        for (JournalLine line : lines.findByJournalEntryIdOrderByLineNumberAscIdAsc(original.id)) {
            JournalLine swapped = new JournalLine();
            swapped.journalEntryId = reversal.id;
            swapped.lineNumber = line.lineNumber;
            swapped.accountingAccountId = line.accountingAccountId;
            swapped.accountCodeSnapshot = line.accountCodeSnapshot;
            swapped.accountNameSnapshot = line.accountNameSnapshot;
            swapped.lineDescription = line.lineDescription;
            swapped.debitAmount = line.creditAmount;
            swapped.creditAmount = line.debitAmount;
            swapped.currency = line.currency;
            swapped.counterpartyCompanyId = line.counterpartyCompanyId;
            swapped.counterpartyLegalPersonId = line.counterpartyLegalPersonId;
            swapped.counterpartyPersonId = line.counterpartyPersonId;
            swapped.employeeId = line.employeeId;
            swapped.departmentId = line.departmentId;
            swapped.projectId = line.projectId;
            swapped.contractStageId = line.contractStageId;
            swapped.sourceDocumentId = line.sourceDocumentId;
            swapped = lines.save(swapped);
            for (JournalLineSource source : sources.findByJournalLineIdOrderByIdAsc(line.id)) {
                JournalLineSource copy = new JournalLineSource();
                copy.journalLineId = swapped.id;
                copy.invoiceLineId = source.invoiceLineId;
                copy.paymentId = source.paymentId;
                copy.receivableItemId = source.receivableItemId;
                copy.payableItemId = source.payableItemId;
                copy.receivableAdjustmentId = source.receivableAdjustmentId;
                copy.payableAdjustmentId = source.payableAdjustmentId;
                copy.costAllocationId = source.costAllocationId;
                copy.costOccurrenceId = source.costOccurrenceId;
                copy.revenueRecognitionId = source.revenueRecognitionId;
                copy.sourceContributionAmount = source.sourceContributionAmount;
                copy.currency = source.currency;
                copy.sourceDocumentId = source.sourceDocumentId;
                sources.save(copy);
            }
        }
        return reversal.id;
    }

    private void validate(JournalForm form, FieldErrors errors) {
        Ledger ledger = form.getLedgerId() == null ? null : ledgers.findById(form.getLedgerId()).orElse(null);
        if (ledger == null) {
            errors.add("ledgerId", "请选择账簿");
        }
        AccountingPeriod period = form.getAccountingPeriodId() == null
                ? null : periods.findById(form.getAccountingPeriodId()).orElse(null);
        if (period == null || ledger == null || !ledger.id.equals(period.ledgerId)) {
            errors.add("accountingPeriodId", "期间须属于所选账簿");
        } else if (form.getVoucherDate() != null
                && (form.getVoucherDate().isBefore(period.periodStartDate) || form.getVoucherDate().isAfter(period.periodEndDate))) {
            errors.add("voucherDate", "凭证日期不在期间内");
        }
        if (form.getVoucherNumber() == null) {
            errors.add("voucherNumber", "请填写凭证号");
        } else if (ledger != null && period != null) {
            for (JournalEntry other : entries.findByLedgerIdAndAccountingPeriodIdAndVoucherNumber(
                    ledger.id, period.id, form.getVoucherNumber())) {
                if (!other.id.equals(form.getId())) {
                    errors.add("voucherNumber", "同一账簿和期间内凭证号已存在");
                }
            }
        }
        if (form.getVoucherDate() == null) {
            errors.add("voucherDate", "请填写记账日期");
        }
        if (form.getEntryDescription() == null) {
            errors.add("entryDescription", "请填写摘要");
        }
        if (form.getPreparedByEmployeeId() == null || employees.findById(form.getPreparedByEmployeeId()).isEmpty()) {
            errors.add("preparedByEmployeeId", "请选择制单人");
        }
        if (form.getReviewedByEmployeeId() != null && employees.findById(form.getReviewedByEmployeeId()).isEmpty()) {
            errors.add("reviewedByEmployeeId", "审核人不存在");
        }
        if (form.getReversalOfEntryId() != null && entries.findById(form.getReversalOfEntryId()).isEmpty()) {
            errors.add("reversalOfEntryId", "被冲销凭证不存在");
        }
        documents.require(null, errors, "sourceDocumentId");
        List<LineRow> active = new ArrayList<>();
        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < form.getLines().size(); i++) {
            LineRow row = form.getLines().get(i);
            if (row.isRemoved() || (row.getId() == null && row.getAccountingAccountId() == null && row.getLineNumber() == null)) {
                continue;
            }
            active.add(row);
            String prefix = "lines[" + i + "]";
            if (row.getLineNumber() == null || row.getLineNumber() <= 0 || !numbers.add(row.getLineNumber())) {
                errors.add(prefix + ".lineNumber", "行号须为正且不重复");
            }
            AccountingAccount account = row.getAccountingAccountId() == null
                    ? null : accounts.findById(row.getAccountingAccountId()).orElse(null);
            if (account == null || ledger == null || !ledger.id.equals(account.ledgerId)) {
                errors.add(prefix + ".accountingAccountId", "科目须属于本账簿");
            } else if (!ledgerService.accountUsable(account, form.getVoucherDate())) {
                errors.add(prefix + ".accountingAccountId", "停用或非记账科目不能用于新分录");
            }
            BigDecimal debit = nz(row.getDebitAmount());
            BigDecimal credit = nz(row.getCreditAmount());
            if (debit.signum() < 0 || credit.signum() < 0) {
                errors.add(prefix + ".debitAmount", "借贷金额不能为负");
            }
            if (debit.signum() > 0 && credit.signum() > 0) {
                errors.add(prefix, "借方和贷方不能同时为正");
            }
            if (debit.signum() == 0 && credit.signum() == 0) {
                errors.add(prefix, "每行借方或贷方须有一边为正");
            }
            String currency = row.getCurrency() == null && ledger != null ? ledger.baseCurrency : row.getCurrency();
            if (ledger != null && (currency == null || !ledger.baseCurrency.equals(currency))) {
                errors.add(prefix + ".currency", "分录币种须与账簿本位币一致");
            }
            int parties = count(row.getCounterpartyCompanyId(), row.getCounterpartyLegalPersonId(), row.getCounterpartyPersonId());
            if (parties > 1) {
                errors.add(prefix, "往来对象最多选一个");
            }
            documents.require(row.getSourceDocumentId(), errors, prefix + ".sourceDocumentId");
        }
        if (active.isEmpty()) {
            errors.add("lines", "请至少填写一行分录");
        }
        Map<Integer, BigDecimal> side = new HashMap<>();
        Map<Integer, BigDecimal> contributed = new HashMap<>();
        for (LineRow row : active) {
            if (row.getLineNumber() == null) {
                continue;
            }
            BigDecimal amount = nz(row.getDebitAmount()).max(nz(row.getCreditAmount()));
            side.put(row.getLineNumber(), amount);
        }
        for (int i = 0; i < form.getSources().size(); i++) {
            SourceRow row = form.getSources().get(i);
            if (row.isRemoved() || blankSource(row)) {
                continue;
            }
            String prefix = "sources[" + i + "]";
            if (row.getLineNumber() == null || !side.containsKey(row.getLineNumber())) {
                errors.add(prefix + ".lineNumber", "来源须指向本凭证的行号");
                continue;
            }
            if (sourceCount(row) != 1) {
                errors.add(prefix, "每条来源恰好指向一种业务事实");
            }
            requireExists(row, errors, prefix);
            if (row.getSourceContributionAmount() == null || row.getSourceContributionAmount().signum() <= 0) {
                errors.add(prefix + ".sourceContributionAmount", "来源贡献金额须大于 0");
            } else {
                contributed.merge(row.getLineNumber(), row.getSourceContributionAmount(), BigDecimal::add);
            }
            if (ledger != null && row.getCurrency() != null && !ledger.baseCurrency.equals(row.getCurrency())) {
                errors.add(prefix + ".currency", "来源币种须与账簿本位币一致");
            }
            documents.require(row.getSourceDocumentId(), errors, prefix + ".sourceDocumentId");
        }
        for (Map.Entry<Integer, BigDecimal> entry : contributed.entrySet()) {
            BigDecimal expected = side.get(entry.getKey());
            if (expected != null && entry.getValue().compareTo(expected) != 0) {
                errors.add("sources", "来源贡献合计须等于该行非零一侧金额");
            }
        }
    }

    private void sync(JournalEntry entry, JournalForm form, FieldErrors errors) {
        Map<Integer, Long> lineIds = new HashMap<>();
        Set<Long> keepLines = new HashSet<>();
        Ledger ledger = ledgers.findById(entry.ledgerId).orElseThrow();
        for (LineRow row : form.getLines()) {
            if (row.isRemoved() || (row.getId() == null && row.getAccountingAccountId() == null && row.getLineNumber() == null)) {
                continue;
            }
            AccountingAccount account = accounts.findById(row.getAccountingAccountId()).orElseThrow();
            JournalLine line = row.getId() == null ? new JournalLine() : lines.findById(row.getId()).orElseGet(JournalLine::new);
            line.journalEntryId = entry.id;
            line.lineNumber = row.getLineNumber();
            line.accountingAccountId = account.id;
            line.accountCodeSnapshot = account.accountCode;
            line.accountNameSnapshot = account.accountName;
            line.lineDescription = row.getLineDescription();
            line.debitAmount = nz(row.getDebitAmount());
            line.creditAmount = nz(row.getCreditAmount());
            line.currency = row.getCurrency() == null ? ledger.baseCurrency : row.getCurrency();
            line.counterpartyCompanyId = row.getCounterpartyCompanyId();
            line.counterpartyLegalPersonId = row.getCounterpartyLegalPersonId();
            line.counterpartyPersonId = row.getCounterpartyPersonId();
            line.employeeId = row.getEmployeeId();
            line.departmentId = row.getDepartmentId();
            line.projectId = row.getProjectId();
            line.contractStageId = row.getContractStageId();
            line.sourceDocumentId = row.getSourceDocumentId();
            line = lines.save(line);
            lineIds.put(row.getLineNumber(), line.id);
            keepLines.add(line.id);
        }
        Set<Long> keepSources = new HashSet<>();
        for (SourceRow row : form.getSources()) {
            if (row.isRemoved() || blankSource(row)) {
                continue;
            }
            Long lineId = lineIds.get(row.getLineNumber());
            if (lineId == null) {
                continue;
            }
            JournalLineSource source = row.getId() == null
                    ? new JournalLineSource()
                    : sources.findById(row.getId()).orElseGet(JournalLineSource::new);
            source.journalLineId = lineId;
            source.invoiceLineId = row.getInvoiceLineId();
            source.paymentId = row.getPaymentId();
            source.receivableItemId = row.getReceivableItemId();
            source.payableItemId = row.getPayableItemId();
            source.receivableAdjustmentId = row.getReceivableAdjustmentId();
            source.payableAdjustmentId = row.getPayableAdjustmentId();
            source.costAllocationId = row.getCostAllocationId();
            source.costOccurrenceId = row.getCostOccurrenceId();
            source.revenueRecognitionId = row.getRevenueRecognitionId();
            source.sourceContributionAmount = row.getSourceContributionAmount();
            source.currency = row.getCurrency() == null ? ledger.baseCurrency : row.getCurrency();
            source.sourceDocumentId = row.getSourceDocumentId();
            keepSources.add(sources.save(source).id);
        }
        for (JournalLine existing : lines.findByJournalEntryIdOrderByLineNumberAscIdAsc(entry.id)) {
            if (!keepLines.contains(existing.id)) {
                sources.findByJournalLineIdOrderByIdAsc(existing.id).forEach(sources::delete);
                lines.delete(existing);
            } else {
                for (JournalLineSource source : sources.findByJournalLineIdOrderByIdAsc(existing.id)) {
                    if (!keepSources.contains(source.id)) {
                        sources.delete(source);
                    }
                }
            }
        }
        if (errors.hasErrors()) {
            errors.throwIfAny();
        }
    }

    private void requireExists(SourceRow row, FieldErrors errors, String prefix) {
        if (row.getInvoiceLineId() != null && invoiceLines.findById(row.getInvoiceLineId()).isEmpty()) {
            errors.add(prefix + ".invoiceLineId", "发票行不存在");
        }
        if (row.getPaymentId() != null && payments.findById(row.getPaymentId()).isEmpty()) {
            errors.add(prefix + ".paymentId", "收付款不存在");
        }
        if (row.getReceivableItemId() != null && receivables.findById(row.getReceivableItemId()).isEmpty()) {
            errors.add(prefix + ".receivableItemId", "应收不存在");
        }
        if (row.getPayableItemId() != null && payables.findById(row.getPayableItemId()).isEmpty()) {
            errors.add(prefix + ".payableItemId", "应付不存在");
        }
        if (row.getReceivableAdjustmentId() != null && receivableAdjustments.findById(row.getReceivableAdjustmentId()).isEmpty()) {
            errors.add(prefix + ".receivableAdjustmentId", "应收调整不存在");
        }
        if (row.getPayableAdjustmentId() != null && payableAdjustments.findById(row.getPayableAdjustmentId()).isEmpty()) {
            errors.add(prefix + ".payableAdjustmentId", "应付调整不存在");
        }
        if (row.getCostAllocationId() != null && costAllocations.findById(row.getCostAllocationId()).isEmpty()) {
            errors.add(prefix + ".costAllocationId", "费用分配不存在");
        }
        if (row.getCostOccurrenceId() != null && costs.findById(row.getCostOccurrenceId()).isEmpty()) {
            errors.add(prefix + ".costOccurrenceId", "费用不存在");
        }
        if (row.getRevenueRecognitionId() != null && revenues.findById(row.getRevenueRecognitionId()).isEmpty()) {
            errors.add(prefix + ".revenueRecognitionId", "收入确认不存在");
        }
    }

    private void copy(JournalEntry entry, JournalForm form) {
        entry.ledgerId = form.getLedgerId();
        entry.accountingPeriodId = form.getAccountingPeriodId();
        entry.voucherNumber = form.getVoucherNumber();
        entry.voucherDate = form.getVoucherDate();
        entry.entryDescription = form.getEntryDescription();
        entry.preparedByEmployeeId = form.getPreparedByEmployeeId();
        entry.reviewedByEmployeeId = form.getReviewedByEmployeeId();
        entry.reviewedAt = form.getReviewedAt();
        entry.adjustmentReason = form.getAdjustmentReason();
        entry.reversalOfEntryId = form.getReversalOfEntryId();
    }

    private void normalize(JournalForm form) {
        form.setVoucherNumber(Text.blankToNull(form.getVoucherNumber()));
        form.setEntryDescription(Text.blankToNull(form.getEntryDescription()));
        form.setAdjustmentReason(Text.blankToNull(form.getAdjustmentReason()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        if (form.getLines() == null) {
            form.setLines(List.of());
        }
        if (form.getSources() == null) {
            form.setSources(List.of());
        }
        for (LineRow row : form.getLines()) {
            row.setLineDescription(Text.blankToNull(row.getLineDescription()));
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
        }
        for (SourceRow row : form.getSources()) {
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
        }
    }

    private static boolean blankSource(SourceRow row) {
        return row.getId() == null && sourceCount(row) == 0 && row.getSourceContributionAmount() == null;
    }

    private static int sourceCount(SourceRow row) {
        int n = 0;
        if (row.getInvoiceLineId() != null) {
            n++;
        }
        if (row.getPaymentId() != null) {
            n++;
        }
        if (row.getReceivableItemId() != null) {
            n++;
        }
        if (row.getPayableItemId() != null) {
            n++;
        }
        if (row.getReceivableAdjustmentId() != null) {
            n++;
        }
        if (row.getPayableAdjustmentId() != null) {
            n++;
        }
        if (row.getCostAllocationId() != null) {
            n++;
        }
        if (row.getCostOccurrenceId() != null) {
            n++;
        }
        if (row.getRevenueRecognitionId() != null) {
            n++;
        }
        return n;
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
}
