package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.CostInvoiceLine;
import com.liangshan.demo.domain.Invoice;
import com.liangshan.demo.domain.InvoiceLine;
import com.liangshan.demo.domain.InvoiceLineAllocation;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.ProjectContract;
import com.liangshan.demo.domain.ReceivableItem;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.CostInvoiceLineRepository;
import com.liangshan.demo.repository.InvoiceLineAllocationRepository;
import com.liangshan.demo.repository.InvoiceLineRepository;
import com.liangshan.demo.repository.InvoiceRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.InvoiceForm;
import com.liangshan.demo.web.form.InvoiceForm.AllocationRow;
import com.liangshan.demo.web.form.InvoiceForm.LineRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final InvoiceLineRepository lines;
    private final InvoiceLineAllocationRepository allocations;
    private final CostInvoiceLineRepository costMatches;
    private final ReceivableItemRepository receivables;
    private final PayableItemRepository payables;
    private final ContractRepository contracts;
    private final ContractStageRepository stages;
    private final ContractBillingScheduleRepository billingSchedules;
    private final ProjectRepository projects;
    private final ProjectContractRepository projectContracts;
    private final SourceDocumentService documents;
    private final com.liangshan.demo.repository.CompanyRepository companies;
    private final com.liangshan.demo.repository.LegalPersonRepository legalPersons;
    private final com.liangshan.demo.repository.PersonRepository persons;
    private final com.liangshan.demo.repository.EmployeeRepository employees;

    public record Row(Long id, String invoiceNumber, String invoiceColor, BigDecimal grossAmount, String currency, String status) {
    }

    public List<Row> list() {
        return invoices.findAllByOrderByIssuedAtDescIdDesc().stream()
                .map(row -> new Row(row.id, row.invoiceNumber, row.invoiceColor,
                        LaborCostRateService.plain(row.invoiceGrossAmount), row.currency, row.status))
                .toList();
    }

    public Invoice get(Long id) {
        return invoices.findById(id).orElseThrow(NotFoundException::new);
    }

    public InvoiceForm form(Long id) {
        Invoice invoice = get(id);
        InvoiceForm form = new InvoiceForm();
        form.setId(invoice.id);
        form.setInvoiceNumber(invoice.invoiceNumber);
        form.setInvoiceCode(invoice.invoiceCode);
        form.setInvoiceType(invoice.invoiceType);
        form.setInvoiceMedium(invoice.invoiceMedium);
        form.setInvoiceColor(invoice.invoiceColor);
        form.setInvoiceNetAmount(invoice.invoiceNetAmount);
        form.setInvoiceTaxAmount(invoice.invoiceTaxAmount);
        form.setInvoiceGrossAmount(invoice.invoiceGrossAmount);
        form.setOriginalInvoiceId(invoice.originalInvoiceId);
        form.setInvoiceVerifiedAt(invoice.invoiceVerifiedAt);
        form.setVerifiedByEmployeeId(invoice.verifiedByEmployeeId);
        form.setVerificationResult(invoice.verificationResult);
        form.setVerificationComment(invoice.verificationComment);
        form.setVoidedAt(invoice.voidedAt);
        form.setVoidReason(invoice.voidReason);
        form.setIssuerCompanyId(invoice.issuerCompanyId);
        form.setIssuerLegalPersonId(invoice.issuerLegalPersonId);
        form.setRecipientCompanyId(invoice.recipientCompanyId);
        form.setRecipientLegalPersonId(invoice.recipientLegalPersonId);
        form.setRecipientPersonId(invoice.recipientPersonId);
        form.setIssuerNameSnapshot(invoice.issuerNameSnapshot);
        form.setIssuerTaxpayerIdSnapshot(invoice.issuerTaxpayerIdSnapshot);
        form.setRecipientNameSnapshot(invoice.recipientNameSnapshot);
        form.setRecipientTaxpayerIdSnapshot(invoice.recipientTaxpayerIdSnapshot);
        form.setIssuedAt(invoice.issuedAt);
        form.setCurrency(invoice.currency);
        form.setStatus(invoice.status);
        form.setSourceDocumentId(invoice.sourceDocumentId);
        Map<Long, Integer> numbers = new HashMap<>();
        for (InvoiceLine line : lines.findByInvoiceIdOrderByLineNumberAscIdAsc(id)) {
            LineRow row = new LineRow();
            row.setId(line.id);
            row.setLineNumber(line.lineNumber);
            row.setItemName(line.itemName);
            row.setItemSpecification(line.itemSpecification);
            row.setTaxClassificationCode(line.taxClassificationCode);
            row.setQuantity(line.quantity);
            row.setUnit(line.unit);
            row.setUnitNetPrice(line.unitNetPrice);
            row.setTaxRate(line.taxRate);
            row.setTaxTreatment(line.taxTreatment);
            row.setNetAmount(line.netAmount);
            row.setTaxAmount(line.taxAmount);
            row.setGrossAmount(line.grossAmount);
            row.setOriginalInvoiceLineId(line.originalInvoiceLineId);
            form.getLines().add(row);
            numbers.put(line.id, line.lineNumber);
            for (InvoiceLineAllocation allocation : allocations.findByInvoiceLineIdOrderByIdAsc(line.id)) {
                AllocationRow item = new AllocationRow();
                item.setId(allocation.id);
                item.setLineNumber(line.lineNumber);
                item.setContractId(allocation.contractId);
                item.setProjectId(allocation.projectId);
                item.setContractStageId(allocation.contractStageId);
                item.setBillingScheduleId(allocation.billingScheduleId);
                item.setReceivableItemId(allocation.receivableItemId);
                item.setPayableItemId(allocation.payableItemId);
                item.setAllocatedAmount(allocation.allocatedAmount);
                item.setCurrency(allocation.currency);
                item.setAllocationBasis(allocation.allocationBasis);
                form.getAllocations().add(item);
            }
        }
        return form;
    }

    @Transactional
    public long save(InvoiceForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        Invoice stored = null;
        if (form.getId() != null) {
            stored = invoices.findById(form.getId()).orElse(null);
            if (stored == null) {
                errors.add("id", "发票不存在");
            }
        }
        validate(form, errors);
        errors.throwIfAny();
        Invoice invoice = stored == null ? new Invoice() : stored;
        apply(invoice, form);
        invoice = invoices.save(invoice);
        Map<Integer, Long> lineIds = syncLines(invoice, form.getLines());
        syncAllocations(invoice, form.getAllocations(), lineIds, errors);
        errors.throwIfAny();
        return invoice.id;
    }

    public void assertCostMatch(Long invoiceLineId, BigDecimal matched, String currency, Long exceptCostLineId, FieldErrors errors, String field) {
        InvoiceLine line = invoiceLineId == null ? null : lines.findById(invoiceLineId).orElse(null);
        if (line == null) {
            errors.add(field, "发票行不存在");
            return;
        }
        Invoice invoice = invoices.findById(line.invoiceId).orElse(null);
        if (invoice == null) {
            errors.add(field, "发票不存在");
            return;
        }
        if (currency != null && !currency.equals(invoice.currency)) {
            errors.add(field, "费用发票匹配币种须与发票一致");
        }
        if (matched != null && line.grossAmount.signum() != 0 && matched.signum() != 0
                && matched.signum() != line.grossAmount.signum()) {
            errors.add(field, "匹配金额符号须与发票行含税金额一致");
        }
        BigDecimal sum = matched == null ? BigDecimal.ZERO : matched;
        for (CostInvoiceLine existing : costMatches.findByInvoiceLineId(invoiceLineId)) {
            if (existing.id.equals(exceptCostLineId)) {
                continue;
            }
            sum = sum.add(existing.matchedExpenseGrossAmount);
        }
        if (exceeds(sum, line.grossAmount)) {
            errors.add(field, "发票行的费用匹配超过票面含税金额");
        }
    }

    private void validate(InvoiceForm form, FieldErrors errors) {
        if (form.getInvoiceNumber() == null) {
            errors.add("invoiceNumber", "请填写发票号码");
        } else {
            for (Invoice other : invoices.findByInvoiceNumber(form.getInvoiceNumber())) {
                if (!other.id.equals(form.getId()) && !"已作废".equals(other.status)) {
                    errors.add("invoiceNumber", "发票号码已存在");
                }
            }
        }
        if (form.getInvoiceType() == null || !Codes.INVOICE_TYPES.contains(form.getInvoiceType())) {
            errors.add("invoiceType", "请选择发票种类");
        }
        if (form.getInvoiceMedium() == null || !Codes.INVOICE_MEDIUMS.contains(form.getInvoiceMedium())) {
            errors.add("invoiceMedium", "请选择发票介质");
        }
        if (form.getInvoiceColor() == null || !Codes.INVOICE_COLORS.contains(form.getInvoiceColor())) {
            errors.add("invoiceColor", "请选择蓝字或红字");
        }
        if ("红字".equals(form.getInvoiceColor())) {
            if (form.getOriginalInvoiceId() == null || invoices.findById(form.getOriginalInvoiceId()).isEmpty()) {
                errors.add("originalInvoiceId", "红字发票须关联原发票");
            }
        }
        requireMoney(form.getInvoiceNetAmount(), "invoiceNetAmount", "不含税金额", errors);
        requireMoney(form.getInvoiceTaxAmount(), "invoiceTaxAmount", "税额", errors);
        requireMoney(form.getInvoiceGrossAmount(), "invoiceGrossAmount", "含税金额", errors);
        if (form.getInvoiceNetAmount() != null && form.getInvoiceTaxAmount() != null && form.getInvoiceGrossAmount() != null
                && form.getInvoiceGrossAmount().compareTo(form.getInvoiceNetAmount().add(form.getInvoiceTaxAmount())) != 0) {
            errors.add("invoiceGrossAmount", "含税金额须等于不含税加税额");
        }
        if (count(form.getIssuerCompanyId(), form.getIssuerLegalPersonId()) != 1) {
            errors.add("issuerCompanyId", "开票方在公司和法人中恰好选一个");
        }
        if (count(form.getRecipientCompanyId(), form.getRecipientLegalPersonId(), form.getRecipientPersonId()) != 1) {
            errors.add("recipientCompanyId", "受票方在公司、法人、自然人中恰好选一个");
        }
        if (form.getIssuerCompanyId() != null && companies.findById(form.getIssuerCompanyId()).isEmpty()) {
            errors.add("issuerCompanyId", "开票公司不存在");
        }
        if (form.getRecipientCompanyId() != null && companies.findById(form.getRecipientCompanyId()).isEmpty()) {
            errors.add("recipientCompanyId", "受票公司不存在");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        }
        if (form.getStatus() == null || !Codes.INVOICE_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择发票状态");
        }
        if (Codes.INVOICE_ISSUED.equals(form.getStatus()) || "已作废".equals(form.getStatus())) {
            if (form.getIssuedAt() == null) {
                errors.add("issuedAt", "已开具须填写开票日");
            }
            if (form.getIssuerNameSnapshot() == null || form.getIssuerTaxpayerIdSnapshot() == null) {
                errors.add("issuerNameSnapshot", "已开具须保留开票方名称和税号");
            }
            if (form.getRecipientNameSnapshot() == null) {
                errors.add("recipientNameSnapshot", "已开具须保留受票方名称");
            }
            documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
            if (form.getSourceDocumentId() == null) {
                errors.add("sourceDocumentId", "已开具须选择依据材料");
            }
        } else {
            documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        }
        if ("已作废".equals(form.getStatus()) && (form.getVoidedAt() == null || form.getVoidReason() == null)) {
            errors.add("voidReason", "作废须填写时间和原因");
        }
        if (form.getVerifiedByEmployeeId() != null && employees.findById(form.getVerifiedByEmployeeId()).isEmpty()) {
            errors.add("verifiedByEmployeeId", "核对人不存在");
        }
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal gross = BigDecimal.ZERO;
        int kept = 0;
        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < form.getLines().size(); i++) {
            LineRow row = form.getLines().get(i);
            if (row.isRemoved() || blankLine(row)) {
                continue;
            }
            kept++;
            String prefix = "lines[" + i + "]";
            if (row.getLineNumber() == null || row.getLineNumber() < 1 || !numbers.add(row.getLineNumber())) {
                errors.add(prefix + ".lineNumber", "行号须为正且不重复");
            }
            if (row.getItemName() == null) {
                errors.add(prefix + ".itemName", "请填写项目名称");
            }
            if (row.getTaxTreatment() != null && !Codes.TAX_TREATMENTS.contains(row.getTaxTreatment())) {
                errors.add(prefix + ".taxTreatment", "请选择税率处理");
            }
            if ("适用税率".equals(row.getTaxTreatment()) && row.getTaxRate() == null) {
                errors.add(prefix + ".taxRate", "适用税率时须填写税率，免税和不征税不要填 0");
            }
            if (("免税".equals(row.getTaxTreatment()) || "不征税".equals(row.getTaxTreatment())) && row.getTaxRate() != null) {
                errors.add(prefix + ".taxRate", "免税和不征税不填写税率");
            }
            if (row.getNetAmount() == null || row.getTaxAmount() == null || row.getGrossAmount() == null) {
                errors.add(prefix + ".grossAmount", "请填写不含税、税额和含税金额");
            } else if (row.getGrossAmount().compareTo(row.getNetAmount().add(row.getTaxAmount())) != 0) {
                errors.add(prefix + ".grossAmount", "行含税金额须等于不含税加税额");
            } else {
                net = net.add(row.getNetAmount());
                tax = tax.add(row.getTaxAmount());
                gross = gross.add(row.getGrossAmount());
            }
            if ("红字".equals(form.getInvoiceColor()) && row.getOriginalInvoiceLineId() == null) {
                errors.add(prefix + ".originalInvoiceLineId", "红字行须关联原发票行");
            }
        }
        if (kept == 0) {
            errors.add("lines", "请至少填写一行明细");
        } else if (form.getInvoiceGrossAmount() != null && gross.compareTo(form.getInvoiceGrossAmount()) != 0
                && form.getVerificationComment() == null) {
            errors.add("verificationComment", "明细合计与票面不一致时须填写核对说明");
        }
        if (form.getInvoiceNetAmount() != null && net.compareTo(form.getInvoiceNetAmount()) != 0 && form.getVerificationComment() == null && kept > 0) {
            errors.add("verificationComment", "明细合计与票面不一致时须填写核对说明");
        }
        if (form.getInvoiceTaxAmount() != null && tax.compareTo(form.getInvoiceTaxAmount()) != 0 && form.getVerificationComment() == null && kept > 0) {
            errors.add("verificationComment", "明细合计与票面不一致时须填写核对说明");
        }
    }

    private void syncAllocations(Invoice invoice, List<AllocationRow> rows, Map<Integer, Long> lineIds, FieldErrors errors) {
        Map<Long, String> basisOfLine = new HashMap<>();
        Map<Long, BigDecimal> sumOfLine = new HashMap<>();
        Set<Long> keep = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            AllocationRow row = rows.get(i);
            if (row.isRemoved() || blankAllocation(row)) {
                continue;
            }
            String prefix = "allocations[" + i + "]";
            Long lineId = row.getLineNumber() == null ? null : lineIds.get(row.getLineNumber());
            if (lineId == null) {
                errors.add(prefix + ".lineNumber", "分配须指向本票的行号");
                continue;
            }
            InvoiceLine line = lines.findById(lineId).orElseThrow();
            int targets = count(row.getContractId(), row.getProjectId(), row.getContractStageId(),
                    row.getBillingScheduleId(), row.getReceivableItemId(), row.getPayableItemId());
            if (targets < 1) {
                errors.add(prefix, "分配至少指定合同、项目、阶段、开票计划或结算项之一");
            }
            if (row.getReceivableItemId() != null && row.getPayableItemId() != null) {
                errors.add(prefix, "应收和应付最多选一个");
            }
            if (row.getAllocationBasis() == null || !Codes.INVOICE_ALLOCATION_BASIS.contains(row.getAllocationBasis())) {
                errors.add(prefix + ".allocationBasis", "请选择含税或不含税");
            }
            String prior = basisOfLine.putIfAbsent(lineId, row.getAllocationBasis());
            if (prior != null && row.getAllocationBasis() != null && !prior.equals(row.getAllocationBasis())) {
                errors.add(prefix + ".allocationBasis", "同一发票行只能用一种分配口径");
            }
            if ((row.getReceivableItemId() != null || row.getPayableItemId() != null)
                    && !Codes.ALLOCATION_GROSS.equals(row.getAllocationBasis())) {
                errors.add(prefix + ".allocationBasis", "关联应收或应付时须用含税口径");
            }
            String currency = row.getCurrency() == null ? invoice.currency : row.getCurrency();
            if (!invoice.currency.equals(currency)) {
                errors.add(prefix + ".currency", "分配币种须与发票一致");
            }
            if (row.getAllocatedAmount() == null || row.getAllocatedAmount().signum() == 0) {
                errors.add(prefix + ".allocatedAmount", "分配金额不能为 0");
            } else if (row.getAllocatedAmount().signum() != line.grossAmount.signum() && line.grossAmount.signum() != 0) {
                errors.add(prefix + ".allocatedAmount", "分配金额符号须与发票行一致");
            }
            if (row.getContractStageId() != null) {
                ContractStage stage = stages.findById(row.getContractStageId()).orElse(null);
                if (stage == null) {
                    errors.add(prefix + ".contractStageId", "阶段不存在");
                } else if (row.getContractId() != null && !row.getContractId().equals(stage.contractId)) {
                    errors.add(prefix + ".contractStageId", "阶段不属于所选合同");
                }
            }
            if (row.getProjectId() != null && projects.findById(row.getProjectId()).isEmpty()) {
                errors.add(prefix + ".projectId", "项目不存在");
            }
            Long contractId = row.getContractId();
            if (row.getReceivableItemId() != null) {
                ReceivableItem item = receivables.findById(row.getReceivableItemId()).orElse(null);
                if (item == null) {
                    errors.add(prefix + ".receivableItemId", "应收不存在");
                } else {
                    matchParties(invoice, item.creditorCompanyId, item.creditorLegalPersonId, null,
                            item.debtorCompanyId, item.debtorLegalPersonId, item.debtorPersonId, errors, prefix);
                    if (item.contractId != null && contractId != null && !item.contractId.equals(contractId)) {
                        errors.add(prefix + ".contractId", "合同须与应收一致");
                    }
                    if (!invoice.currency.equals(item.currency)) {
                        errors.add(prefix + ".currency", "币种须与应收一致");
                    }
                }
            }
            if (row.getPayableItemId() != null) {
                PayableItem item = payables.findById(row.getPayableItemId()).orElse(null);
                if (item == null) {
                    errors.add(prefix + ".payableItemId", "应付不存在");
                } else {
                    matchParties(invoice, item.creditorCompanyId, item.creditorLegalPersonId, item.creditorPersonId,
                            item.debtorCompanyId, item.debtorLegalPersonId, null, errors, prefix);
                    if (item.contractId != null && contractId != null && !item.contractId.equals(contractId)) {
                        errors.add(prefix + ".contractId", "合同须与应付一致");
                    }
                }
            }
            if (row.getProjectId() != null && contractId != null) {
                boolean linked = false;
                for (ProjectContract link : projectContracts.findByProjectIdOrderByIdAsc(row.getProjectId())) {
                    if (contractId.equals(link.contractId)) {
                        linked = true;
                    }
                }
                if (!linked) {
                    errors.add(prefix + ".projectId", "该合同不在项目合同中");
                }
            }
            if (row.getBillingScheduleId() != null && billingSchedules.findById(row.getBillingScheduleId()).isEmpty()) {
                errors.add(prefix + ".billingScheduleId", "开票计划不存在");
            }
            if (row.getContractId() != null && contracts.findById(row.getContractId()).isEmpty()) {
                errors.add(prefix + ".contractId", "合同不存在");
            }
            if (errors.hasErrors()) {
                continue;
            }
            if (row.getAllocatedAmount() != null) {
                sumOfLine.merge(lineId, row.getAllocatedAmount(), BigDecimal::add);
            }
            InvoiceLineAllocation allocation = row.getId() == null
                    ? new InvoiceLineAllocation()
                    : allocations.findById(row.getId()).orElseGet(InvoiceLineAllocation::new);
            allocation.invoiceLineId = lineId;
            allocation.contractId = row.getContractId();
            allocation.projectId = row.getProjectId();
            allocation.contractStageId = row.getContractStageId();
            allocation.billingScheduleId = row.getBillingScheduleId();
            allocation.receivableItemId = row.getReceivableItemId();
            allocation.payableItemId = row.getPayableItemId();
            allocation.allocatedAmount = row.getAllocatedAmount();
            allocation.currency = currency;
            allocation.allocationBasis = row.getAllocationBasis();
            keep.add(allocations.save(allocation).id);
        }
        if (errors.hasErrors()) {
            return;
        }
        for (Map.Entry<Long, BigDecimal> entry : sumOfLine.entrySet()) {
            InvoiceLine line = lines.findById(entry.getKey()).orElseThrow();
            BigDecimal cap = Codes.ALLOCATION_NET.equals(basisOfLine.get(entry.getKey())) ? line.netAmount : line.grossAmount;
            if (exceeds(entry.getValue(), cap)) {
                errors.add("allocations", "发票行分配超过该口径的票面金额");
            }
            BigDecimal matched = BigDecimal.ZERO;
            for (CostInvoiceLine costLine : costMatches.findByInvoiceLineId(entry.getKey())) {
                matched = matched.add(costLine.matchedExpenseGrossAmount);
            }
            if (exceeds(matched, line.grossAmount)) {
                errors.add("lines", "改后的票面含税金额盖不住已有费用匹配");
            }
        }
        for (InvoiceLine line : lines.findByInvoiceIdOrderByLineNumberAscIdAsc(invoice.id)) {
            for (InvoiceLineAllocation existing : allocations.findByInvoiceLineIdOrderByIdAsc(line.id)) {
                if (!keep.contains(existing.id)) {
                    allocations.delete(existing);
                }
            }
        }
    }

    private void matchParties(
            Invoice invoice,
            Long creditorCompanyId,
            Long creditorLegalPersonId,
            Long creditorPersonId,
            Long debtorCompanyId,
            Long debtorLegalPersonId,
            Long debtorPersonId,
            FieldErrors errors,
            String prefix) {
        boolean issuer = Cmp.eq(invoice.issuerCompanyId, creditorCompanyId)
                && Cmp.eq(invoice.issuerLegalPersonId, creditorLegalPersonId)
                && creditorPersonId == null;
        boolean recipient = Cmp.eq(invoice.recipientCompanyId, debtorCompanyId)
                && Cmp.eq(invoice.recipientLegalPersonId, debtorLegalPersonId)
                && Cmp.eq(invoice.recipientPersonId, debtorPersonId);
        if (!issuer || !recipient) {
            errors.add(prefix, "发票的开票方、受票方须与结算项的债权人和债务人一致");
        }
    }

    private Map<Integer, Long> syncLines(Invoice invoice, List<LineRow> rows) {
        Map<Integer, Long> ids = new HashMap<>();
        Set<Long> keep = new HashSet<>();
        for (LineRow row : rows) {
            if (row.isRemoved() || blankLine(row)) {
                continue;
            }
            InvoiceLine line = row.getId() == null ? new InvoiceLine() : lines.findById(row.getId()).orElseGet(InvoiceLine::new);
            line.invoiceId = invoice.id;
            line.lineNumber = row.getLineNumber();
            line.itemName = row.getItemName();
            line.itemSpecification = row.getItemSpecification();
            line.taxClassificationCode = row.getTaxClassificationCode();
            line.quantity = row.getQuantity();
            line.unit = row.getUnit();
            line.unitNetPrice = row.getUnitNetPrice();
            line.taxRate = row.getTaxRate();
            line.netAmount = row.getNetAmount();
            line.taxAmount = row.getTaxAmount();
            line.grossAmount = row.getGrossAmount();
            line.taxTreatment = row.getTaxTreatment();
            line.originalInvoiceLineId = row.getOriginalInvoiceLineId();
            line = lines.save(line);
            ids.put(line.lineNumber, line.id);
            keep.add(line.id);
        }
        for (InvoiceLine existing : lines.findByInvoiceIdOrderByLineNumberAscIdAsc(invoice.id)) {
            if (!keep.contains(existing.id)) {
                allocations.findByInvoiceLineIdOrderByIdAsc(existing.id).forEach(allocations::delete);
                lines.delete(existing);
            }
        }
        return ids;
    }

    private void apply(Invoice invoice, InvoiceForm form) {
        invoice.invoiceNumber = form.getInvoiceNumber();
        invoice.invoiceCode = form.getInvoiceCode();
        invoice.invoiceType = form.getInvoiceType();
        invoice.invoiceMedium = form.getInvoiceMedium();
        invoice.invoiceColor = form.getInvoiceColor();
        invoice.invoiceNetAmount = form.getInvoiceNetAmount();
        invoice.invoiceTaxAmount = form.getInvoiceTaxAmount();
        invoice.invoiceGrossAmount = form.getInvoiceGrossAmount();
        invoice.originalInvoiceId = form.getOriginalInvoiceId();
        invoice.invoiceVerifiedAt = form.getInvoiceVerifiedAt();
        invoice.verifiedByEmployeeId = form.getVerifiedByEmployeeId();
        invoice.verificationResult = form.getVerificationResult();
        invoice.verificationComment = form.getVerificationComment();
        invoice.voidedAt = form.getVoidedAt();
        invoice.voidReason = form.getVoidReason();
        invoice.issuerCompanyId = form.getIssuerCompanyId();
        invoice.issuerLegalPersonId = form.getIssuerLegalPersonId();
        invoice.recipientCompanyId = form.getRecipientCompanyId();
        invoice.recipientLegalPersonId = form.getRecipientLegalPersonId();
        invoice.recipientPersonId = form.getRecipientPersonId();
        invoice.issuerNameSnapshot = form.getIssuerNameSnapshot();
        invoice.issuerTaxpayerIdSnapshot = form.getIssuerTaxpayerIdSnapshot();
        invoice.recipientNameSnapshot = form.getRecipientNameSnapshot();
        invoice.recipientTaxpayerIdSnapshot = form.getRecipientTaxpayerIdSnapshot();
        invoice.issuedAt = form.getIssuedAt();
        invoice.currency = form.getCurrency();
        invoice.status = form.getStatus();
        invoice.sourceDocumentId = form.getSourceDocumentId();
    }

    private void normalize(InvoiceForm form) {
        form.setInvoiceNumber(Text.blankToNull(form.getInvoiceNumber()));
        form.setInvoiceCode(Text.blankToNull(form.getInvoiceCode()));
        form.setInvoiceType(Text.blankToNull(form.getInvoiceType()));
        form.setInvoiceMedium(Text.blankToNull(form.getInvoiceMedium()));
        form.setInvoiceColor(Text.blankToNull(form.getInvoiceColor()));
        form.setVerificationResult(Text.blankToNull(form.getVerificationResult()));
        form.setVerificationComment(Text.blankToNull(form.getVerificationComment()));
        form.setVoidReason(Text.blankToNull(form.getVoidReason()));
        form.setIssuerNameSnapshot(Text.blankToNull(form.getIssuerNameSnapshot()));
        form.setIssuerTaxpayerIdSnapshot(Text.blankToNull(form.getIssuerTaxpayerIdSnapshot()));
        form.setRecipientNameSnapshot(Text.blankToNull(form.getRecipientNameSnapshot()));
        form.setRecipientTaxpayerIdSnapshot(Text.blankToNull(form.getRecipientTaxpayerIdSnapshot()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        if (form.getLines() == null) {
            form.setLines(List.of());
        }
        if (form.getAllocations() == null) {
            form.setAllocations(List.of());
        }
        for (LineRow row : form.getLines()) {
            row.setItemName(Text.blankToNull(row.getItemName()));
            row.setItemSpecification(Text.blankToNull(row.getItemSpecification()));
            row.setTaxClassificationCode(Text.blankToNull(row.getTaxClassificationCode()));
            row.setUnit(Text.blankToNull(row.getUnit()));
            row.setTaxTreatment(Text.blankToNull(row.getTaxTreatment()));
        }
        for (AllocationRow row : form.getAllocations()) {
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            row.setAllocationBasis(Text.blankToNull(row.getAllocationBasis()));
        }
    }

    private static boolean blankLine(LineRow row) {
        return row.getId() == null && row.getItemName() == null && row.getGrossAmount() == null;
    }

    private static boolean blankAllocation(AllocationRow row) {
        return row.getId() == null && row.getAllocatedAmount() == null && row.getReceivableItemId() == null
                && row.getPayableItemId() == null && row.getContractId() == null && row.getProjectId() == null;
    }

    private static void requireMoney(BigDecimal amount, String field, String label, FieldErrors errors) {
        if (amount == null) {
            errors.add(field, "请填写" + label);
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

    static boolean exceeds(BigDecimal sum, BigDecimal cap) {
        if (sum == null || cap == null) {
            return false;
        }
        if (cap.signum() >= 0) {
            return sum.compareTo(cap) > 0;
        }
        return sum.compareTo(cap) < 0;
    }
}
