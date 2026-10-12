package com.liangshan.demo.service;

import com.liangshan.demo.domain.AccountingPeriod;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.Ledger;
import com.liangshan.demo.domain.RevenueRecognition;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.AccountingPeriodRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LedgerRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.RevenueRecognitionRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.RevenueForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueRecognitionService {

    private final RevenueRecognitionRepository recognitions;
    private final LedgerRepository ledgers;
    private final AccountingPeriodRepository periods;
    private final ContractRepository contracts;
    private final ContractTermVersionRepository terms;
    private final ContractStageRepository stages;
    private final ProjectRepository projects;
    private final ProjectContractRepository projectContracts;
    private final AcceptanceEventRepository acceptances;
    private final EmployeeRepository employees;
    private final SourceDocumentService documents;

    public record Row(Long id, String status, java.time.LocalDate recognitionDate, BigDecimal amount, String currency) {
    }

    public List<Row> list() {
        return recognitions.findAllByOrderByRecognitionDateDescIdDesc().stream()
                .map(row -> new Row(row.id, row.status, row.recognitionDate, row.recognizedRevenueAmount, row.currency))
                .toList();
    }

    public RevenueRecognition get(Long id) {
        return recognitions.findById(id).orElseThrow(NotFoundException::new);
    }

    public RevenueForm form(Long id) {
        RevenueRecognition row = get(id);
        RevenueForm form = new RevenueForm();
        form.setId(row.id);
        form.setLedgerId(row.ledgerId);
        form.setContractId(row.contractId);
        form.setContractTermVersionId(row.contractTermVersionId);
        form.setProjectId(row.projectId);
        form.setContractStageId(row.contractStageId);
        form.setRecognitionDate(row.recognitionDate);
        form.setAccountingPeriodId(row.accountingPeriodId);
        form.setRecognizedRevenueAmount(row.recognizedRevenueAmount);
        form.setCurrency(row.currency);
        form.setRevenuePolicyCode(row.revenuePolicyCode);
        form.setRevenuePolicyVersion(row.revenuePolicyVersion);
        form.setAcceptanceEventId(row.acceptanceEventId);
        form.setSourceDocumentId(row.sourceDocumentId);
        form.setApprovedByEmployeeId(row.approvedByEmployeeId);
        form.setApprovedAt(row.approvedAt);
        form.setStatus(row.status);
        form.setReversalOfRecognitionId(row.reversalOfRecognitionId);
        return form;
    }

    @Transactional
    public long save(RevenueForm form) {
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setRevenuePolicyCode(Text.blankToNull(form.getRevenuePolicyCode()));
        form.setRevenuePolicyVersion(Text.blankToNull(form.getRevenuePolicyVersion()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        FieldErrors errors = new FieldErrors();
        RevenueRecognition stored = form.getId() == null ? null : recognitions.findById(form.getId()).orElse(null);
        if (form.getId() != null && stored == null) {
            errors.add("id", "收入确认不存在");
        }
        if (stored != null && Codes.REVENUE_APPROVED.equals(stored.status)) {
            errors.add("status", "已审批的收入确认不能修改，请另记冲回");
        }
        if (stored != null && Codes.REVENUE_VOID.equals(stored.status)) {
            errors.add("status", "已作废的收入确认不能修改");
        }
        Ledger ledger = form.getLedgerId() == null ? null : ledgers.findById(form.getLedgerId()).orElse(null);
        if (ledger == null) {
            errors.add("ledgerId", "请选择账簿");
        } else if (form.getCurrency() == null) {
            form.setCurrency(ledger.baseCurrency);
        } else if (!ledger.baseCurrency.equals(form.getCurrency())) {
            errors.add("currency", "币种须与账簿本位币一致");
        }
        Contract contract = form.getContractId() == null ? null : contracts.findById(form.getContractId()).orElse(null);
        if (contract == null) {
            errors.add("contractId", "请选择合同");
        }
        ContractTermVersion term = form.getContractTermVersionId() == null
                ? null : terms.findById(form.getContractTermVersionId()).orElse(null);
        if (term == null || contract == null || !contract.id.equals(term.contractId)) {
            errors.add("contractTermVersionId", "条款版本须属于该合同");
        }
        if (form.getProjectId() == null || projects.findById(form.getProjectId()).isEmpty()) {
            errors.add("projectId", "请选择项目");
        } else if (contract != null && projectContracts.findByContractIdOrderByIdAsc(contract.id).stream()
                .noneMatch(link -> form.getProjectId().equals(link.projectId))) {
            errors.add("projectId", "项目须已关联该合同");
        }
        if (form.getContractStageId() != null) {
            ContractStage stage = stages.findById(form.getContractStageId()).orElse(null);
            if (stage == null || contract == null || !contract.id.equals(stage.contractId)) {
                errors.add("contractStageId", "阶段须属于该合同");
            }
        }
        AccountingPeriod period = form.getAccountingPeriodId() == null
                ? null : periods.findById(form.getAccountingPeriodId()).orElse(null);
        if (period == null || ledger == null || !ledger.id.equals(period.ledgerId)) {
            errors.add("accountingPeriodId", "期间须属于所选账簿");
        } else if (form.getRecognitionDate() != null
                && (form.getRecognitionDate().isBefore(period.periodStartDate)
                || form.getRecognitionDate().isAfter(period.periodEndDate))) {
            errors.add("recognitionDate", "确认日不在期间内");
        }
        if (form.getRecognitionDate() == null) {
            errors.add("recognitionDate", "请填写确认日");
        }
        if (form.getRevenuePolicyCode() == null || form.getRevenuePolicyVersion() == null) {
            errors.add("revenuePolicyCode", "请填写收入政策编号和版本");
        }
        if (form.getStatus() == null || !Codes.REVENUE_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择收入确认状态");
        }
        if (form.getAcceptanceEventId() != null && acceptances.findById(form.getAcceptanceEventId()).isEmpty()) {
            errors.add("acceptanceEventId", "验收不存在");
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        if (Codes.REVENUE_APPROVED.equals(form.getStatus())) {
            if (form.getApprovedByEmployeeId() == null || employees.findById(form.getApprovedByEmployeeId()).isEmpty()) {
                errors.add("approvedByEmployeeId", "请选择审批人");
            }
            if (form.getApprovedAt() == null) {
                errors.add("approvedAt", "请填写审批时间");
            }
            if (form.getSourceDocumentId() == null) {
                errors.add("sourceDocumentId", "已审批的收入确认须填写依据材料");
            }
        } else if (form.getApprovedByEmployeeId() != null && employees.findById(form.getApprovedByEmployeeId()).isEmpty()) {
            errors.add("approvedByEmployeeId", "审批人不存在");
        }
        checkAmount(form, errors);
        errors.throwIfAny();
        RevenueRecognition row = stored == null ? new RevenueRecognition() : stored;
        row.ledgerId = form.getLedgerId();
        row.contractId = form.getContractId();
        row.contractTermVersionId = form.getContractTermVersionId();
        row.projectId = form.getProjectId();
        row.contractStageId = form.getContractStageId();
        row.recognitionDate = form.getRecognitionDate();
        row.accountingPeriodId = form.getAccountingPeriodId();
        row.recognizedRevenueAmount = form.getRecognizedRevenueAmount();
        row.currency = form.getCurrency();
        row.revenuePolicyCode = form.getRevenuePolicyCode();
        row.revenuePolicyVersion = form.getRevenuePolicyVersion();
        row.acceptanceEventId = form.getAcceptanceEventId();
        row.sourceDocumentId = form.getSourceDocumentId();
        row.approvedByEmployeeId = form.getApprovedByEmployeeId();
        row.approvedAt = form.getApprovedAt();
        row.status = form.getStatus();
        row.reversalOfRecognitionId = form.getReversalOfRecognitionId();
        return recognitions.save(row).id;
    }

    private void checkAmount(RevenueForm form, FieldErrors errors) {
        BigDecimal amount = form.getRecognizedRevenueAmount();
        if (form.getReversalOfRecognitionId() == null) {
            if (amount == null || amount.signum() <= 0) {
                errors.add("recognizedRevenueAmount", "确认金额须大于 0");
            }
            return;
        }
        if (form.getId() != null && form.getId().equals(form.getReversalOfRecognitionId())) {
            errors.add("reversalOfRecognitionId", "不能冲回自身");
            return;
        }
        RevenueRecognition original = recognitions.findById(form.getReversalOfRecognitionId()).orElse(null);
        if (original == null || original.reversalOfRecognitionId != null || !Codes.REVENUE_APPROVED.equals(original.status)) {
            errors.add("reversalOfRecognitionId", "只能冲回已审批的原始确认");
            return;
        }
        if (amount == null || amount.signum() >= 0) {
            errors.add("recognizedRevenueAmount", "冲回金额须为负");
            return;
        }
        if (!original.ledgerId.equals(form.getLedgerId())
                || !original.contractId.equals(form.getContractId())
                || !original.projectId.equals(form.getProjectId())
                || !original.currency.equals(form.getCurrency())) {
            errors.add("reversalOfRecognitionId", "冲回须与原确认的账簿、合同、项目和币种一致");
        }
        BigDecimal used = BigDecimal.ZERO;
        for (RevenueRecognition other : recognitions.findByReversalOfRecognitionId(original.id)) {
            if (other.id.equals(form.getId()) || Codes.REVENUE_VOID.equals(other.status) || other.recognizedRevenueAmount == null) {
                continue;
            }
            used = used.add(other.recognizedRevenueAmount.abs());
        }
        if (used.add(amount.abs()).compareTo(original.recognizedRevenueAmount) > 0) {
            errors.add("recognizedRevenueAmount", "累计冲回不能超过原确认额");
        }
    }
}
