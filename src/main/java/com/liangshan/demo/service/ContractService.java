package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractAdditionalSignatory;
import com.liangshan.demo.domain.ContractAmendment;
import com.liangshan.demo.domain.ContractLine;
import com.liangshan.demo.domain.ContractLineTermVersion;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.ContractStageDeliverable;
import com.liangshan.demo.domain.ContractStageTermVersion;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.ProjectContract;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractAdditionalSignatoryRepository;
import com.liangshan.demo.repository.ContractAmendmentRepository;
import com.liangshan.demo.repository.ContractLineRepository;
import com.liangshan.demo.repository.ContractLineTermVersionRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageDeliverableRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractStageTermVersionRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.ContractAmendmentForm;
import com.liangshan.demo.web.form.ContractDetailForm;
import com.liangshan.demo.web.form.ContractDetailForm.DeliverableForm;
import com.liangshan.demo.web.form.ContractDetailForm.LineForm;
import com.liangshan.demo.web.form.ContractDetailForm.LinkForm;
import com.liangshan.demo.web.form.ContractDetailForm.SignatoryForm;
import com.liangshan.demo.web.form.ContractDetailForm.StageForm;
import com.liangshan.demo.web.form.ContractForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
public class ContractService {

    private final ContractRepository contracts;
    private final ContractTermVersionRepository terms;
    private final ContractLineRepository lines;
    private final ContractLineTermVersionRepository lineTerms;
    private final ContractAmendmentRepository amendments;
    private final ContractStageRepository stages;
    private final ContractStageTermVersionRepository stageTerms;
    private final ContractStageDeliverableRepository deliverables;
    private final ContractAdditionalSignatoryRepository signatories;
    private final ProjectContractRepository projectContracts;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final SupplierRepository suppliers;
    private final ProjectRepository projects;
    private final SourceDocumentService documents;
    private final FulfillmentLinkGuard fulfillmentLinks;

    public record Row(
            Long id,
            String contractNumber,
            String contractTitle,
            String contractType,
            String buyer,
            String seller,
            String approvalStatus,
            String status) {
    }

    public record Header(
            Long id,
            String contractNumber,
            String contractTitle,
            String contractType,
            String buyer,
            String seller,
            String approvalStatus,
            String status,
            String pricingMethod,
            String currency) {
    }

    public record ProjectLinkRow(
            Long id,
            Long contractId,
            String contractNumber,
            String contractTitle,
            String relationshipNote) {
    }

    public record LineChoice(Long lineTermId, Integer lineNumber, String label) {
    }

    public record StageChoice(Long stageId, String stageCode, String label) {
    }

    public record StageHistoryRow(
            String stageCode,
            Integer versionNumber,
            Long amendmentId,
            String deliverableCodes,
            LocalDate validFrom,
            LocalDate validTo) {
    }

    public List<Row> list() {
        Map<Long, Company> companyMap = companyMap();
        Map<Long, LegalPerson> legalMap = legalMap();
        Map<Long, Person> personMap = personMap();
        return contracts.findAllByOrderByContractNumberAsc().stream()
                .map(contract -> new Row(
                        contract.id,
                        contract.contractNumber,
                        contract.contractTitle,
                        contract.contractType,
                        partyLabel(contract.buyerCompanyId, contract.buyerLegalPersonId, contract.buyerPersonId, companyMap, legalMap, personMap),
                        partyLabel(contract.sellerCompanyId, contract.sellerLegalPersonId, contract.sellerPersonId, companyMap, legalMap, personMap),
                        contract.approvalStatus,
                        contract.status))
                .toList();
    }

    public Header header(Long id) {
        Contract contract = get(id);
        Map<Long, Company> companyMap = companyMap();
        Map<Long, LegalPerson> legalMap = legalMap();
        Map<Long, Person> personMap = personMap();
        return new Header(
                contract.id,
                contract.contractNumber,
                contract.contractTitle,
                contract.contractType,
                partyLabel(contract.buyerCompanyId, contract.buyerLegalPersonId, contract.buyerPersonId, companyMap, legalMap, personMap),
                partyLabel(contract.sellerCompanyId, contract.sellerLegalPersonId, contract.sellerPersonId, companyMap, legalMap, personMap),
                contract.approvalStatus,
                contract.status,
                contract.pricingMethod,
                contract.currency);
    }

    public ContractForm form(Long id) {
        return toForm(get(id));
    }

    public ContractDetailForm detailForm(Long id) {
        ContractTermVersion open = openTerm(id);
        Map<Long, ContractLine> lineMap = new HashMap<>();
        for (ContractLine line : lines.findByContractIdOrderByLineNumberAsc(id)) {
            lineMap.put(line.id, line);
        }
        ContractDetailForm form = new ContractDetailForm();
        List<LineForm> lineRows = new ArrayList<>();
        for (ContractLineTermVersion term : lineTerms.findByContractTermVersionId(open.id)) {
            if (term.validTo != null) {
                continue;
            }
            ContractLine line = lineMap.get(term.contractLineId);
            if (line == null) {
                continue;
            }
            lineRows.add(toLine(line, term));
        }
        lineRows.sort((left, right) -> Integer.compare(
                left.getLineNumber() == null ? 0 : left.getLineNumber(),
                right.getLineNumber() == null ? 0 : right.getLineNumber()));
        form.setLines(lineRows);

        List<StageForm> stageRows = new ArrayList<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(id)) {
            ContractStageTermVersion term = openStageTerm(stage.id);
            if (term != null) {
                stageRows.add(toStage(stage, term));
            }
        }
        form.setStages(stageRows);

        List<DeliverableForm> deliverableRows = new ArrayList<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(id)) {
            ContractStageTermVersion term = openStageTerm(stage.id);
            if (term == null) {
                continue;
            }
            for (ContractStageDeliverable item : deliverables.findByStageTermVersionIdOrderByIdAsc(term.id)) {
                deliverableRows.add(toDeliverable(stage, item));
            }
        }
        form.setDeliverables(deliverableRows);
        form.setSignatories(new ArrayList<>(signatories.findByContractIdOrderByIdAsc(id).stream().map(this::toSignatory).toList()));
        form.setLinks(new ArrayList<>(projectContracts.findByContractIdOrderByIdAsc(id).stream().map(this::toLink).toList()));
        return form;
    }

    public List<ContractTermVersion> termHistory(Long id) {
        get(id);
        return terms.findByContractIdOrderByVersionNumberAsc(id);
    }

    public List<StageHistoryRow> stageHistory(Long id) {
        get(id);
        List<StageHistoryRow> rows = new ArrayList<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(id)) {
            for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(stage.id)) {
                String codes = deliverables.findByStageTermVersionIdOrderByIdAsc(term.id).stream()
                        .map(item -> item.deliverableCode)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("");
                rows.add(new StageHistoryRow(stage.stageCode, term.versionNumber, term.amendmentId, codes, term.validFrom, term.validTo));
            }
        }
        return rows;
    }

    public ContractAmendmentForm amendmentDraft(Long id) {
        ContractAmendmentForm draft = new ContractAmendmentForm();
        draft.setSignedAt(LocalDate.now());
        draft.setEffectiveAt(LocalDate.now());
        draft.setTerms(form(id));
        return draft;
    }

    public List<LineChoice> lineChoices(Long id) {
        ContractTermVersion open = openTerm(id);
        Map<Long, ContractLine> lineMap = new HashMap<>();
        for (ContractLine line : lines.findByContractIdOrderByLineNumberAsc(id)) {
            lineMap.put(line.id, line);
        }
        List<LineChoice> choices = new ArrayList<>();
        for (ContractLineTermVersion term : lineTerms.findByContractTermVersionId(open.id)) {
            if (term.validTo != null) {
                continue;
            }
            ContractLine line = lineMap.get(term.contractLineId);
            int number = line == null || line.lineNumber == null ? 0 : line.lineNumber;
            choices.add(new LineChoice(term.id, number, number + " " + term.itemName));
        }
        choices.sort((left, right) -> Integer.compare(left.lineNumber(), right.lineNumber()));
        return choices;
    }

    public List<StageChoice> stageChoices(Long id) {
        get(id);
        return stages.findByContractIdOrderBySequenceNoAsc(id).stream()
                .map(stage -> new StageChoice(stage.id, stage.stageCode, stage.stageCode + " " + stage.stageName))
                .toList();
    }

    public List<ProjectLinkRow> linksForProject(Long projectId) {
        return projectContracts.findByProjectIdOrderByIdAsc(projectId).stream().map(link -> {
            Contract contract = contracts.findById(link.contractId).orElse(null);
            return new ProjectLinkRow(
                    link.id,
                    link.contractId,
                    contract == null ? "" : contract.contractNumber,
                    contract == null ? "未找到合同" : contract.contractTitle,
                    link.relationshipNote);
        }).toList();
    }

    @Transactional
    public long save(ContractForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validateHeader(form, errors);
        validateCommercial(form, form.getContractType(), errors, "");
        ContractTermVersion open = form.getId() == null ? null : openTerm(form.getId());
        if (open != null && commercialChanged(open, form) && open.validFrom.isBefore(LocalDate.now())) {
            errors.add("*", "跨日变更条款须登记合同变更");
        }
        errors.throwIfAny();

        Contract contract = form.getId() == null ? new Contract() : get(form.getId());
        applyHeader(contract, form);
        contract = contracts.saveAndFlush(contract);
        if (open == null) {
            ContractTermVersion created = new ContractTermVersion();
            created.contractId = contract.id;
            created.versionNumber = 1;
            created.amendmentId = null;
            created.validFrom = contract.validFrom;
            created.recordedAt = now();
            applyCommercial(created, form);
            terms.save(created);
        } else if (commercialChanged(open, form)) {
            applyCommercial(open, form);
            terms.save(open);
        }
        return contract.id;
    }

    @Transactional
    public void saveDetails(Long contractId, ContractDetailForm form) {
        Contract contract = get(contractId);
        ContractTermVersion open = openTerm(contractId);
        prepareDetails(form, contract.currency);
        FieldErrors errors = new FieldErrors();
        validateDetails(contract, open, form, errors);
        errors.throwIfAny();

        Map<Integer, Long> lineTermsByNumber = syncLines(contractId, open, form.getLines());
        Map<String, Long> stageIdsByCode = syncStages(contractId, open, form.getStages());
        syncDeliverables(open, form.getDeliverables(), lineTermsByNumber, stageIdsByCode);
        syncSignatories(contractId, form.getSignatories());
        syncLinks(contractId, form.getLinks());
    }

    @Transactional
    public void registerAmendment(Long contractId, ContractAmendmentForm form) {
        Contract contract = get(contractId);
        if (form.getTerms() == null) {
            form.setTerms(new ContractForm());
        }
        normalize(form.getTerms());
        form.setAmendmentNumber(Text.blankToNull(form.getAmendmentNumber()));
        form.setSummary(Text.blankToNull(form.getSummary()));
        ContractTermVersion open = openTerm(contractId);
        FieldErrors errors = new FieldErrors();
        if (form.getAmendmentNumber() == null) {
            errors.add("amendmentNumber", "请填写变更编号");
        } else {
            for (ContractAmendment existing : amendments.findByContractIdOrderByIdAsc(contractId)) {
                if (form.getAmendmentNumber().equals(existing.amendmentNumber)) {
                    errors.add("amendmentNumber", "变更编号在本合同内已存在");
                }
            }
        }
        if (form.getSignedAt() == null) {
            errors.add("signedAt", "请填写变更签署日");
        }
        if (form.getEffectiveAt() == null) {
            errors.add("effectiveAt", "请填写变更生效日");
        } else if (!form.getEffectiveAt().isAfter(open.validFrom)) {
            errors.add("effectiveAt", "变更生效日须晚于当前条款的开始日");
        }
        if (form.getSourceDocumentId() == null) {
            errors.add("sourceDocumentId", "变更须引用来源材料");
        } else {
            documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        }
        if (form.getSummary() == null) {
            errors.add("summary", "请填写变更摘要");
        }
        validateCommercial(form.getTerms(), contract.contractType, errors, "terms.");
        boolean terminating = form.getTerms().getTerminationDate() != null || form.getTerms().getTerminationReason() != null;
        if ((form.getTerms().getTerminationDate() == null) != (form.getTerms().getTerminationReason() == null)) {
            errors.add("terms.terminationDate", "终止日期和原因须同时填写");
        } else if (terminating) {
            if (!"已批准".equals(contract.approvalStatus) || contract.sourceDocumentId == null) {
                errors.add("terms.terminationDate", "写入终止条款前，合同须已批准并引用来源材料");
            }
        } else if ("终止".equals(contract.status)) {
            errors.add("terms.terminationDate", "已终止的合同须保留终止日期和原因");
        }
        errors.throwIfAny();

        ContractAmendment amendment = new ContractAmendment();
        amendment.contractId = contractId;
        amendment.amendmentNumber = form.getAmendmentNumber();
        amendment.signedAt = form.getSignedAt();
        amendment.effectiveAt = form.getEffectiveAt();
        amendment.sourceDocumentId = form.getSourceDocumentId();
        amendment.summary = form.getSummary();
        amendment = amendments.saveAndFlush(amendment);

        LocalDate cut = form.getEffectiveAt();
        open.validTo = cut;
        terms.save(open);

        ContractTermVersion next = new ContractTermVersion();
        next.contractId = contractId;
        next.versionNumber = open.versionNumber + 1;
        next.amendmentId = amendment.id;
        next.validFrom = cut;
        next.recordedAt = now();
        applyCommercial(next, form.getTerms());
        next = terms.saveAndFlush(next);

        Map<Long, Long> lineTermMap = new HashMap<>();
        for (ContractLine line : lines.findByContractIdOrderByLineNumberAsc(contractId)) {
            ContractLineTermVersion current = currentLineTerm(line.id, open.id);
            if (current == null) {
                continue;
            }
            current.validTo = cut;
            lineTerms.save(current);
            ContractLineTermVersion copy = copyLineTerm(current, next.id, cut);
            copy = lineTerms.saveAndFlush(copy);
            lineTermMap.put(current.id, copy.id);
        }
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            ContractStageTermVersion current = openStageTerm(stage.id);
            if (current == null) {
                continue;
            }
            current.validTo = cut;
            stageTerms.save(current);
            ContractStageTermVersion copy = copyStageTerm(current, amendment.id, cut, next.currency);
            copy = stageTerms.saveAndFlush(copy);
            for (ContractStageDeliverable item : deliverables.findByStageTermVersionIdOrderByIdAsc(current.id)) {
                deliverables.save(copyDeliverable(item, copy.id, lineTermMap));
            }
        }

        applyCommercial(contract, form.getTerms());
        if (terminating) {
            contract.status = "终止";
        }
        contracts.save(contract);
    }

    @Transactional
    public void linkProject(Long projectId, Long contractId, String note) {
        if (projects.findById(projectId).isEmpty()) {
            throw new NotFoundException();
        }
        get(contractId);
        for (ProjectContract existing : projectContracts.findByProjectIdOrderByIdAsc(projectId)) {
            if (contractId.equals(existing.contractId)) {
                throw new FieldErrorsException(Map.of("*", "该项目已关联这份合同"));
            }
        }
        ProjectContract link = new ProjectContract();
        link.projectId = projectId;
        link.contractId = contractId;
        link.relationshipNote = Text.blankToNull(note);
        projectContracts.save(link);
    }

    @Transactional
    public void unlinkProject(Long projectId, Long linkId) {
        ProjectContract link = projectContracts.findById(linkId).orElseThrow(NotFoundException::new);
        if (!projectId.equals(link.projectId)) {
            throw new NotFoundException();
        }
        projectContracts.delete(link);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (fulfillmentLinks.contractUsed(id)) {
            throw new FieldErrorsException(Map.of("*", "该合同已有开票计划、收付款计划或交付记录，不能删除"));
        }
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(id)) {
            for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(stage.id)) {
                deliverables.findByStageTermVersionIdOrderByIdAsc(term.id).forEach(deliverables::delete);
                stageTerms.delete(term);
            }
            stages.delete(stage);
        }
        for (ContractLine line : lines.findByContractIdOrderByLineNumberAsc(id)) {
            lineTerms.findByContractLineId(line.id).forEach(lineTerms::delete);
            lines.delete(line);
        }
        signatories.findByContractIdOrderByIdAsc(id).forEach(signatories::delete);
        projectContracts.findByContractIdOrderByIdAsc(id).forEach(projectContracts::delete);
        amendments.findByContractIdOrderByIdAsc(id).forEach(amendments::delete);
        terms.findByContractIdOrderByVersionNumberAsc(id).forEach(terms::delete);
        contracts.deleteById(id);
    }

    private void validateHeader(ContractForm form, FieldErrors errors) {
        if (form.getContractNumber() == null) {
            errors.add("contractNumber", "请填写合同编号");
        } else {
            for (Contract other : contracts.findByContractNumber(form.getContractNumber())) {
                if (!other.id.equals(form.getId())) {
                    errors.add("contractNumber", "合同编号已存在");
                }
            }
        }
        if (form.getContractTitle() == null) {
            errors.add("contractTitle", "请填写合同名称");
        }
        if (form.getContractType() == null || !Codes.CONTRACT_TYPES.contains(form.getContractType())) {
            errors.add("contractType", "请选择合同类型");
        }
        if (form.getManagingCompanyId() != null && companies.findById(form.getManagingCompanyId()).isEmpty()) {
            errors.add("managingCompanyId", "管理公司不存在");
        }
        requireEmployee(form.getContractOwnerEmployeeId(), errors, "contractOwnerEmployeeId", "经办人");
        requireEmployee(form.getContractApprovedByEmployeeId(), errors, "contractApprovedByEmployeeId", "审批人");
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        if (form.getApprovalStatus() == null || !Codes.CONTRACT_APPROVAL_STATUS.contains(form.getApprovalStatus())) {
            errors.add("approvalStatus", "请选择审批状态");
        } else if ("已批准".equals(form.getApprovalStatus())) {
            if (form.getContractApprovedByEmployeeId() == null || form.getContractApprovedAt() == null) {
                errors.add("contractApprovedByEmployeeId", "已批准须填写审批人和时间");
            }
        } else if (form.getContractApprovedByEmployeeId() != null || form.getContractApprovedAt() != null) {
            errors.add("contractApprovedByEmployeeId", "未批准时审批人和时间留空");
        }
        if (form.getStatus() == null || !Codes.CONTRACT_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择合同状态");
        } else if ("生效".equals(form.getStatus()) || "终止".equals(form.getStatus())) {
            if (!"已批准".equals(form.getApprovalStatus())) {
                errors.add("status", "生效或终止前须已批准");
            }
            if (form.getSourceDocumentId() == null) {
                errors.add("sourceDocumentId", "生效或终止须引用来源材料");
            }
        }
        if ("终止".equals(form.getStatus())) {
            if (form.getTerminationDate() == null || form.getTerminationReason() == null) {
                errors.add("terminationDate", "终止须填写日期和原因");
            }
        } else if (form.getTerminationDate() != null || form.getTerminationReason() != null) {
            errors.add("terminationDate", "未终止时终止日期和原因留空");
        }
    }

    private void validateCommercial(ContractForm form, String contractType, FieldErrors errors, String prefix) {
        requireParty(form.getBuyerCompanyId(), form.getBuyerLegalPersonId(), form.getBuyerPersonId(), errors, prefix + "buyerCompanyId", "买方");
        requireParty(form.getSellerCompanyId(), form.getSellerLegalPersonId(), form.getSellerPersonId(), errors, prefix + "sellerCompanyId", "卖方");
        if (form.getBuyerNameSnapshot() == null) {
            errors.add(prefix + "buyerNameSnapshot", "请填写买方签约名称");
        }
        if (form.getSellerNameSnapshot() == null) {
            errors.add(prefix + "sellerNameSnapshot", "请填写卖方签约名称");
        }
        if (form.getCurrency() == null) {
            errors.add(prefix + "currency", "请填写币种");
        } else if (form.getCurrency().length() > 8) {
            errors.add(prefix + "currency", "币种过长");
        }
        if (form.getPricingMethod() == null || !Codes.PRICING_METHODS.contains(form.getPricingMethod())) {
            errors.add(prefix + "pricingMethod", "请选择计价方式");
        }
        if (form.getValidFrom() == null) {
            errors.add(prefix + "validFrom", "请填写合同生效日");
        }
        String period = Periods.orderError(form.getValidFrom(), form.getValidTo());
        if (period != null) {
            errors.add(prefix + "validTo", period);
        }
        checkMoney(form.getContractNetAmount(), form.getContractTaxAmount(), form.getContractGrossAmount(), errors, prefix + "contractGrossAmount", "合同");
        checkProcurement(form, contractType, errors, prefix);
        requirePerson(form.getBuyerSignatoryPersonId(), errors, prefix + "buyerSignatoryPersonId", "买方签署人");
        requirePerson(form.getSellerSignatoryPersonId(), errors, prefix + "sellerSignatoryPersonId", "卖方签署人");
        requirePerson(form.getBuyerContactPersonId(), errors, prefix + "buyerContactPersonId", "买方联系人");
        requirePerson(form.getSellerContactPersonId(), errors, prefix + "sellerContactPersonId", "卖方联系人");
        if (form.getProcurementCompanyId() != null && companies.findById(form.getProcurementCompanyId()).isEmpty()) {
            errors.add(prefix + "procurementCompanyId", "采购公司不存在");
        }
    }

    private void checkProcurement(ContractForm form, String contractType, FieldErrors errors, String prefix) {
        if (!Codes.CONTRACT_PROCUREMENT.equals(contractType)) {
            if (form.getSupplierId() != null || form.getProcurementCompanyId() != null) {
                errors.add(prefix + "supplierId", "非采购合同不记录采购公司和供应商档案");
            }
            return;
        }
        if (form.getSupplierId() == null) {
            errors.add(prefix + "supplierId", "采购合同须选择供应商档案");
            return;
        }
        Supplier supplier = suppliers.findById(form.getSupplierId()).orElse(null);
        if (supplier == null) {
            errors.add(prefix + "supplierId", "供应商档案不存在");
            return;
        }
        if (form.getProcurementCompanyId() == null) {
            errors.add(prefix + "procurementCompanyId", "采购合同须填写采购公司");
        } else if (!form.getProcurementCompanyId().equals(supplier.purchasingCompanyId)) {
            errors.add(prefix + "procurementCompanyId", "采购公司须与供应商档案的采购公司一致");
        }
        if (!supplier.purchasingCompanyId.equals(form.getBuyerCompanyId())
                || form.getBuyerLegalPersonId() != null
                || form.getBuyerPersonId() != null) {
            errors.add(prefix + "buyerCompanyId", "采购合同的买方公司须与供应商档案的采购公司一致");
        }
        if (!Cmp.eq(form.getSellerCompanyId(), supplier.companyId)
                || !Cmp.eq(form.getSellerLegalPersonId(), supplier.legalPersonId)
                || !Cmp.eq(form.getSellerPersonId(), supplier.personId)) {
            errors.add(prefix + "sellerCompanyId", "采购合同的卖方须与供应商档案的供货对象一致");
        }
    }

    private void validateDetails(Contract contract, ContractTermVersion open, ContractDetailForm form, FieldErrors errors) {
        Set<Integer> lineNumbers = new HashSet<>();
        Set<Long> openLineTermIds = new HashSet<>();
        for (ContractLineTermVersion term : lineTerms.findByContractTermVersionId(open.id)) {
            if (term.validTo == null) {
                openLineTermIds.add(term.id);
            }
        }
        int lineIndex = 0;
        for (LineForm row : form.getLines()) {
            String prefix = "lines[" + lineIndex + "].";
            lineIndex++;
            if (row == null || row.isRemoved()) {
                if (row != null && row.getId() != null && lineHasHistory(row.getId(), open.id)) {
                    errors.add(prefix + "lineNumber", "已有历史条款的品项不能删除");
                }
                continue;
            }
            if (row.getId() != null) {
                ContractLine line = lines.findById(row.getId()).orElse(null);
                if (line == null || !contract.id.equals(line.contractId)) {
                    errors.add(prefix + "lineNumber", "品项不属于本合同");
                }
            }
            if (row.getLineNumber() == null || row.getLineNumber() <= 0) {
                errors.add(prefix + "lineNumber", "行号须大于 0");
            } else if (!lineNumbers.add(row.getLineNumber())) {
                errors.add(prefix + "lineNumber", "行号在合同内不能重复");
            }
            if (row.getItemName() == null) {
                errors.add(prefix + "itemName", "请填写品项名称");
            }
            if (row.getUnit() == null) {
                errors.add(prefix + "unit", "请填写单位");
            }
            checkQuantity(row.getContractedQuantity(), errors, prefix + "contractedQuantity", false);
            if (row.getUnitNetPrice() != null && row.getUnitNetPrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.add(prefix + "unitNetPrice", "不含税单价不能为负");
            }
            checkMoney(row.getLineNetAmount(), row.getLineTaxAmount(), row.getLineGrossAmount(), errors, prefix + "lineGrossAmount", "品项");
            checkRate(row.getTaxRate(), row.getLineNetAmount(), row.getLineTaxAmount(), errors, prefix + "taxRate");
        }

        Set<String> stageCodes = new HashSet<>();
        Set<Integer> sequences = new HashSet<>();
        Set<Long> keptStageIds = new HashSet<>();
        int stageIndex = 0;
        for (StageForm row : form.getStages()) {
            String prefix = "stages[" + stageIndex + "].";
            stageIndex++;
            if (row == null || row.isRemoved()) {
                if (row != null && row.getId() != null && stageHasHistory(row.getId())) {
                    errors.add(prefix + "stageCode", "已有历史条款的阶段不能删除");
                }
                continue;
            }
            if (row.getId() != null) {
                ContractStage stage = stages.findById(row.getId()).orElse(null);
                if (stage == null || !contract.id.equals(stage.contractId)) {
                    errors.add(prefix + "stageCode", "阶段不属于本合同");
                } else {
                    keptStageIds.add(stage.id);
                }
            }
            if (row.getStageCode() == null) {
                errors.add(prefix + "stageCode", "请填写阶段编号");
            } else if (!stageCodes.add(row.getStageCode())) {
                errors.add(prefix + "stageCode", "阶段编号在合同内不能重复");
            }
            if (row.getSequenceNo() == null || row.getSequenceNo() <= 0) {
                errors.add(prefix + "sequenceNo", "阶段序号须大于 0");
            } else if (!sequences.add(row.getSequenceNo())) {
                errors.add(prefix + "sequenceNo", "阶段序号在合同内不能重复");
            }
            if (row.getStageName() == null) {
                errors.add(prefix + "stageName", "请填写阶段名称");
            }
            if (row.getStatus() == null || !Codes.STAGE_STATUS.contains(row.getStatus())) {
                errors.add(prefix + "status", "请选择阶段状态");
            }
            requireEmployee(row.getResponsibleEmployeeId(), errors, prefix + "responsibleEmployeeId", "阶段负责人");
            if (row.getDeliverableDescription() == null) {
                errors.add(prefix + "deliverableDescription", "请填写交付说明");
            }
            if (row.getAcceptanceCriteria() == null) {
                errors.add(prefix + "acceptanceCriteria", "请填写验收条件");
            }
            if (row.getPlannedStartAt() == null || row.getPlannedDueAt() == null) {
                errors.add(prefix + "plannedStartAt", "请填写阶段计划开始和完成日");
            } else if (row.getPlannedDueAt().isBefore(row.getPlannedStartAt())) {
                errors.add(prefix + "plannedDueAt", "计划完成日不能早于计划开始日");
            }
            if (row.getCurrency() != null && !row.getCurrency().equals(contract.currency)) {
                errors.add(prefix + "currency", "阶段币种须与合同币种一致");
            }
            checkMoney(row.getStageNetAmount(), row.getStageTaxAmount(), row.getStageGrossAmount(), errors, prefix + "stageGrossAmount", "阶段");
        }

        Set<String> deliverableKeys = new HashSet<>();
        int deliverableIndex = 0;
        for (DeliverableForm row : form.getDeliverables()) {
            String prefix = "deliverables[" + deliverableIndex + "].";
            deliverableIndex++;
            if (row == null || row.isRemoved()) {
                continue;
            }
            boolean stageKnown = (row.getStageId() != null && keptStageIds.contains(row.getStageId()))
                    || (row.getStageCode() != null && stageCodes.contains(row.getStageCode()));
            if (!stageKnown) {
                errors.add(prefix + "stageId", "交付项须指定本合同的阶段");
            }
            if (row.getContractLineTermVersionId() != null && !openLineTermIds.contains(row.getContractLineTermVersionId())) {
                errors.add(prefix + "contractLineTermVersionId", "交付项只能关联当前条款的品项");
            }
            if (row.getLineNumber() != null && !lineNumbers.contains(row.getLineNumber())) {
                errors.add(prefix + "lineNumber", "交付项关联的行号不在本合同当前品项中");
            }
            if (row.getDeliverableCode() == null) {
                errors.add(prefix + "deliverableCode", "请填写交付项编号");
            } else {
                String key = (row.getStageId() == null ? "code:" + row.getStageCode() : "id:" + row.getStageId()) + "/" + row.getDeliverableCode();
                if (!deliverableKeys.add(key)) {
                    errors.add(prefix + "deliverableCode", "同一阶段的交付项编号不能重复");
                }
            }
            if (row.getDeliverableName() == null) {
                errors.add(prefix + "deliverableName", "请填写交付项名称");
            }
            if (row.getUnit() == null) {
                errors.add(prefix + "unit", "请填写交付项单位");
            }
            checkQuantity(row.getPlannedQuantity(), errors, prefix + "plannedQuantity", true);
            if (row.getAcceptanceCriteria() == null) {
                errors.add(prefix + "acceptanceCriteria", "请填写交付项验收条件");
            }
            if (row.getPlannedDeliveryDate() == null) {
                errors.add(prefix + "plannedDeliveryDate", "请填写计划交付日");
            }
        }

        int signatoryIndex = 0;
        for (SignatoryForm row : form.getSignatories()) {
            String prefix = "signatories[" + signatoryIndex + "].";
            signatoryIndex++;
            if (row == null || row.isRemoved()) {
                continue;
            }
            requireParty(row.getSignatoryCompanyId(), row.getSignatoryLegalPersonId(), row.getSignatoryPersonId(), errors, prefix + "signatoryCompanyId", "附加签署方");
            if (sameParty(row.getSignatoryCompanyId(), row.getSignatoryLegalPersonId(), row.getSignatoryPersonId(), contract.buyerCompanyId, contract.buyerLegalPersonId, contract.buyerPersonId)
                    || sameParty(row.getSignatoryCompanyId(), row.getSignatoryLegalPersonId(), row.getSignatoryPersonId(), contract.sellerCompanyId, contract.sellerLegalPersonId, contract.sellerPersonId)) {
                errors.add(prefix + "signatoryCompanyId", "主合同的买卖双方不在附加签署方中重复记录");
            }
            if (row.getSignatoryRole() == null || !Codes.SIGNATORY_ROLES.contains(row.getSignatoryRole())) {
                errors.add(prefix + "signatoryRole", "请选择签署角色");
            }
            if (row.getResponsibilityDescription() == null) {
                errors.add(prefix + "responsibilityDescription", "请填写责任说明");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + "validFrom", "请填写签署方生效日");
            }
            String period = Periods.orderError(row.getValidFrom(), row.getValidTo());
            if (period != null) {
                errors.add(prefix + "validTo", period);
            }
            documents.require(row.getBasisDocumentId(), errors, prefix + "basisDocumentId");
        }

        Set<Long> projectIds = new HashSet<>();
        int linkIndex = 0;
        for (LinkForm row : form.getLinks()) {
            String prefix = "links[" + linkIndex + "].";
            linkIndex++;
            if (row == null || row.isRemoved()) {
                continue;
            }
            if (row.getProjectId() == null || projects.findById(row.getProjectId()).isEmpty()) {
                errors.add(prefix + "projectId", "请选择已有项目");
            } else if (!projectIds.add(row.getProjectId())) {
                errors.add(prefix + "projectId", "同一项目不能重复关联");
            }
        }
    }

    private Map<Integer, Long> syncLines(Long contractId, ContractTermVersion open, List<LineForm> rows) {
        Set<Long> kept = new HashSet<>();
        Map<Integer, Long> lineTermsByNumber = new HashMap<>();
        for (LineForm row : rows) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            ContractLine line = row.getId() == null ? new ContractLine() : lines.findById(row.getId()).orElseThrow(NotFoundException::new);
            line.contractId = contractId;
            line.lineNumber = row.getLineNumber();
            line = lines.saveAndFlush(line);
            kept.add(line.id);
            ContractLineTermVersion term = currentLineTerm(line.id, open.id);
            if (term == null) {
                term = new ContractLineTermVersion();
                term.contractLineId = line.id;
                term.contractTermVersionId = open.id;
                term.validFrom = open.validFrom;
            }
            applyLine(term, row);
            term = lineTerms.saveAndFlush(term);
            lineTermsByNumber.put(line.lineNumber, term.id);
        }
        for (ContractLine existing : lines.findByContractIdOrderByLineNumberAsc(contractId)) {
            if (kept.contains(existing.id)) {
                continue;
            }
            lineTerms.findByContractLineId(existing.id).forEach(lineTerms::delete);
            lines.delete(existing);
        }
        return lineTermsByNumber;
    }

    private Map<String, Long> syncStages(Long contractId, ContractTermVersion open, List<StageForm> rows) {
        Set<Long> kept = new HashSet<>();
        Map<String, Long> idsByCode = new HashMap<>();
        for (StageForm row : rows) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            ContractStage stage = row.getId() == null ? new ContractStage() : stages.findById(row.getId()).orElseThrow(NotFoundException::new);
            stage.contractId = contractId;
            stage.stageCode = row.getStageCode();
            stage.sequenceNo = row.getSequenceNo();
            stage.stageName = row.getStageName();
            stage.responsibleEmployeeId = row.getResponsibleEmployeeId();
            stage.status = row.getStatus();
            stage = stages.saveAndFlush(stage);
            kept.add(stage.id);
            idsByCode.put(stage.stageCode, stage.id);
            ContractStageTermVersion term = openStageTerm(stage.id);
            if (term == null) {
                term = new ContractStageTermVersion();
                term.contractStageId = stage.id;
                term.versionNumber = 1;
                term.amendmentId = null;
                term.validFrom = open.validFrom;
                term.recordedAt = now();
            }
            applyStageCommercial(term, row);
            stageTerms.save(term);
        }
        for (ContractStage existing : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            if (kept.contains(existing.id)) {
                continue;
            }
            if (fulfillmentLinks.stageUsed(existing.id)) {
                throw new FieldErrorsException(Map.of("*", "该阶段已有开票计划、收付款计划或交付记录，不能删除"));
            }
            for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(existing.id)) {
                deliverables.findByStageTermVersionIdOrderByIdAsc(term.id).forEach(deliverables::delete);
                stageTerms.delete(term);
            }
            stages.delete(existing);
        }
        return idsByCode;
    }

    private void syncDeliverables(
            ContractTermVersion open,
            List<DeliverableForm> rows,
            Map<Integer, Long> lineTermsByNumber,
            Map<String, Long> stageIdsByCode) {
        Map<Long, ContractStageTermVersion> openTerms = new HashMap<>();
        for (Long stageId : stageIdsByCode.values()) {
            ContractStageTermVersion term = openStageTerm(stageId);
            if (term != null) {
                openTerms.put(stageId, term);
            }
        }
        Set<Long> kept = new HashSet<>();
        for (DeliverableForm row : rows) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            Long stageId = row.getStageId() != null ? row.getStageId() : stageIdsByCode.get(row.getStageCode());
            ContractStageTermVersion term = openTerms.get(stageId);
            if (term == null) {
                throw new FieldErrorsException(Map.of("*", "交付项所属阶段没有当前条款"));
            }
            ContractStageDeliverable item = row.getId() == null
                    ? new ContractStageDeliverable()
                    : deliverables.findById(row.getId()).orElseThrow(NotFoundException::new);
            item.stageTermVersionId = term.id;
            Long lineTermId = row.getContractLineTermVersionId();
            if (lineTermId == null && row.getLineNumber() != null) {
                lineTermId = lineTermsByNumber.get(row.getLineNumber());
            }
            if (lineTermId != null) {
                ContractLineTermVersion lineTerm = lineTerms.findById(lineTermId).orElse(null);
                if (lineTerm == null || !open.id.equals(lineTerm.contractTermVersionId)) {
                    throw new FieldErrorsException(Map.of("*", "交付项只能关联当前条款的品项"));
                }
            }
            item.contractLineTermVersionId = lineTermId;
            item.deliverableCode = row.getDeliverableCode();
            item.deliverableName = row.getDeliverableName();
            item.deliverableSpecification = row.getDeliverableSpecification();
            item.unit = row.getUnit();
            item.plannedQuantity = row.getPlannedQuantity();
            item.acceptanceCriteria = row.getAcceptanceCriteria();
            item.plannedDeliveryDate = row.getPlannedDeliveryDate();
            item.plannedAcceptanceDate = row.getPlannedAcceptanceDate();
            item = deliverables.saveAndFlush(item);
            kept.add(item.id);
        }
        for (ContractStageTermVersion term : openTerms.values()) {
            for (ContractStageDeliverable existing : deliverables.findByStageTermVersionIdOrderByIdAsc(term.id)) {
                if (!kept.contains(existing.id)) {
                    if (fulfillmentLinks.deliverableUsed(existing.id)) {
                        throw new FieldErrorsException(Map.of("*", "该交付项已有交付记录，不能删除"));
                    }
                    deliverables.delete(existing);
                }
            }
        }
    }

    private void syncSignatories(Long contractId, List<SignatoryForm> rows) {
        Set<Long> kept = new HashSet<>();
        for (SignatoryForm row : rows) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            ContractAdditionalSignatory signatory = row.getId() == null
                    ? new ContractAdditionalSignatory()
                    : signatories.findById(row.getId()).orElseThrow(NotFoundException::new);
            signatory.contractId = contractId;
            signatory.signatoryCompanyId = row.getSignatoryCompanyId();
            signatory.signatoryLegalPersonId = row.getSignatoryLegalPersonId();
            signatory.signatoryPersonId = row.getSignatoryPersonId();
            signatory.signatoryRole = row.getSignatoryRole();
            signatory.responsibilityDescription = row.getResponsibilityDescription();
            signatory.validFrom = row.getValidFrom();
            signatory.validTo = row.getValidTo();
            signatory.basisDocumentId = row.getBasisDocumentId();
            kept.add(signatories.save(signatory).id);
        }
        for (ContractAdditionalSignatory existing : signatories.findByContractIdOrderByIdAsc(contractId)) {
            if (!kept.contains(existing.id)) {
                signatories.delete(existing);
            }
        }
    }

    private void syncLinks(Long contractId, List<LinkForm> rows) {
        Set<Long> kept = new HashSet<>();
        for (LinkForm row : rows) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            ProjectContract link = row.getId() == null
                    ? new ProjectContract()
                    : projectContracts.findById(row.getId()).orElseThrow(NotFoundException::new);
            if (link.contractId != null && !contractId.equals(link.contractId)) {
                throw new FieldErrorsException(Map.of("*", "项目关联不属于本合同"));
            }
            link.contractId = contractId;
            link.projectId = row.getProjectId();
            link.relationshipNote = row.getRelationshipNote();
            kept.add(projectContracts.save(link).id);
        }
        for (ProjectContract existing : projectContracts.findByContractIdOrderByIdAsc(contractId)) {
            if (!kept.contains(existing.id)) {
                projectContracts.delete(existing);
            }
        }
    }

    private void prepareDetails(ContractDetailForm form, String contractCurrency) {
        form.setLines(Periods.mutable(form.getLines()));
        form.setStages(Periods.mutable(form.getStages()));
        form.setDeliverables(Periods.mutable(form.getDeliverables()));
        form.setSignatories(Periods.mutable(form.getSignatories()));
        form.setLinks(Periods.mutable(form.getLinks()));
        for (LineForm row : form.getLines()) {
            if (row == null) {
                continue;
            }
            row.setItemName(Text.blankToNull(row.getItemName()));
            row.setItemSpecification(Text.blankToNull(row.getItemSpecification()));
            row.setUnit(Text.blankToNull(row.getUnit()));
        }
        for (StageForm row : form.getStages()) {
            if (row == null) {
                continue;
            }
            row.setStageCode(Text.blankToNull(row.getStageCode()));
            row.setStageName(Text.blankToNull(row.getStageName()));
            row.setStatus(Text.blankToNull(row.getStatus()));
            row.setDeliverableDescription(Text.blankToNull(row.getDeliverableDescription()));
            row.setAcceptanceCriteria(Text.blankToNull(row.getAcceptanceCriteria()));
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            if (row.getCurrency() == null) {
                row.setCurrency(contractCurrency);
            }
        }
        for (DeliverableForm row : form.getDeliverables()) {
            if (row == null) {
                continue;
            }
            row.setStageCode(Text.blankToNull(row.getStageCode()));
            row.setDeliverableCode(Text.blankToNull(row.getDeliverableCode()));
            row.setDeliverableName(Text.blankToNull(row.getDeliverableName()));
            row.setDeliverableSpecification(Text.blankToNull(row.getDeliverableSpecification()));
            row.setUnit(Text.blankToNull(row.getUnit()));
            row.setAcceptanceCriteria(Text.blankToNull(row.getAcceptanceCriteria()));
        }
        for (SignatoryForm row : form.getSignatories()) {
            if (row == null) {
                continue;
            }
            row.setSignatoryRole(Text.blankToNull(row.getSignatoryRole()));
            row.setResponsibilityDescription(Text.blankToNull(row.getResponsibilityDescription()));
        }
        for (LinkForm row : form.getLinks()) {
            if (row == null) {
                continue;
            }
            row.setRelationshipNote(Text.blankToNull(row.getRelationshipNote()));
        }
    }

    private void normalize(ContractForm form) {
        form.setContractNumber(Text.blankToNull(form.getContractNumber()));
        form.setContractType(Text.blankToNull(form.getContractType()));
        form.setContractTitle(Text.blankToNull(form.getContractTitle()));
        form.setBuyerNameSnapshot(Text.blankToNull(form.getBuyerNameSnapshot()));
        form.setSellerNameSnapshot(Text.blankToNull(form.getSellerNameSnapshot()));
        form.setBuyerTaxpayerIdSnapshot(Text.blankToNull(form.getBuyerTaxpayerIdSnapshot()));
        form.setSellerTaxpayerIdSnapshot(Text.blankToNull(form.getSellerTaxpayerIdSnapshot()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setPricingMethod(Text.blankToNull(form.getPricingMethod()));
        form.setApprovalStatus(Text.blankToNull(form.getApprovalStatus()));
        form.setTerminationReason(Text.blankToNull(form.getTerminationReason()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        if (form.getContractApprovedAt() != null) {
            form.setContractApprovedAt(form.getContractApprovedAt().truncatedTo(ChronoUnit.MINUTES));
        }
    }

    private void applyHeader(Contract contract, ContractForm form) {
        contract.contractNumber = form.getContractNumber();
        contract.contractType = form.getContractType();
        contract.contractTitle = form.getContractTitle();
        contract.managingCompanyId = form.getManagingCompanyId();
        contract.contractOwnerEmployeeId = form.getContractOwnerEmployeeId();
        contract.approvalStatus = form.getApprovalStatus();
        contract.contractApprovedAt = form.getContractApprovedAt();
        contract.contractApprovedByEmployeeId = form.getContractApprovedByEmployeeId();
        contract.status = form.getStatus();
        contract.sourceDocumentId = form.getSourceDocumentId();
        applyCommercial(contract, form);
    }

    private void applyCommercial(Contract contract, ContractForm form) {
        contract.buyerCompanyId = form.getBuyerCompanyId();
        contract.buyerLegalPersonId = form.getBuyerLegalPersonId();
        contract.buyerPersonId = form.getBuyerPersonId();
        contract.sellerCompanyId = form.getSellerCompanyId();
        contract.sellerLegalPersonId = form.getSellerLegalPersonId();
        contract.sellerPersonId = form.getSellerPersonId();
        contract.buyerNameSnapshot = form.getBuyerNameSnapshot();
        contract.sellerNameSnapshot = form.getSellerNameSnapshot();
        contract.buyerTaxpayerIdSnapshot = form.getBuyerTaxpayerIdSnapshot();
        contract.sellerTaxpayerIdSnapshot = form.getSellerTaxpayerIdSnapshot();
        contract.buyerSignatoryPersonId = form.getBuyerSignatoryPersonId();
        contract.sellerSignatoryPersonId = form.getSellerSignatoryPersonId();
        contract.buyerContactPersonId = form.getBuyerContactPersonId();
        contract.sellerContactPersonId = form.getSellerContactPersonId();
        contract.procurementCompanyId = form.getProcurementCompanyId();
        contract.supplierId = form.getSupplierId();
        contract.currency = form.getCurrency();
        contract.pricingMethod = form.getPricingMethod();
        contract.contractNetAmount = form.getContractNetAmount();
        contract.contractTaxAmount = form.getContractTaxAmount();
        contract.contractGrossAmount = form.getContractGrossAmount();
        contract.signedAt = form.getSignedAt();
        contract.validFrom = form.getValidFrom();
        contract.validTo = form.getValidTo();
        contract.terminationDate = form.getTerminationDate();
        contract.terminationReason = form.getTerminationReason();
    }

    private void applyCommercial(ContractTermVersion version, ContractForm form) {
        version.buyerCompanyId = form.getBuyerCompanyId();
        version.buyerLegalPersonId = form.getBuyerLegalPersonId();
        version.buyerPersonId = form.getBuyerPersonId();
        version.sellerCompanyId = form.getSellerCompanyId();
        version.sellerLegalPersonId = form.getSellerLegalPersonId();
        version.sellerPersonId = form.getSellerPersonId();
        version.buyerNameSnapshot = form.getBuyerNameSnapshot();
        version.sellerNameSnapshot = form.getSellerNameSnapshot();
        version.buyerTaxpayerIdSnapshot = form.getBuyerTaxpayerIdSnapshot();
        version.sellerTaxpayerIdSnapshot = form.getSellerTaxpayerIdSnapshot();
        version.buyerSignatoryPersonId = form.getBuyerSignatoryPersonId();
        version.sellerSignatoryPersonId = form.getSellerSignatoryPersonId();
        version.buyerContactPersonId = form.getBuyerContactPersonId();
        version.sellerContactPersonId = form.getSellerContactPersonId();
        version.procurementCompanyId = form.getProcurementCompanyId();
        version.supplierId = form.getSupplierId();
        version.currency = form.getCurrency();
        version.pricingMethod = form.getPricingMethod();
        version.contractNetAmount = form.getContractNetAmount();
        version.contractTaxAmount = form.getContractTaxAmount();
        version.contractGrossAmount = form.getContractGrossAmount();
        version.signedAt = form.getSignedAt();
        version.terminationDate = form.getTerminationDate();
        version.terminationReason = form.getTerminationReason();
        version.agreedValidFrom = form.getValidFrom();
        version.agreedValidTo = form.getValidTo();
    }

    private boolean commercialChanged(ContractTermVersion version, ContractForm form) {
        return !Cmp.eq(version.buyerCompanyId, form.getBuyerCompanyId())
                || !Cmp.eq(version.buyerLegalPersonId, form.getBuyerLegalPersonId())
                || !Cmp.eq(version.buyerPersonId, form.getBuyerPersonId())
                || !Cmp.eq(version.sellerCompanyId, form.getSellerCompanyId())
                || !Cmp.eq(version.sellerLegalPersonId, form.getSellerLegalPersonId())
                || !Cmp.eq(version.sellerPersonId, form.getSellerPersonId())
                || !Cmp.eq(version.buyerNameSnapshot, form.getBuyerNameSnapshot())
                || !Cmp.eq(version.sellerNameSnapshot, form.getSellerNameSnapshot())
                || !Cmp.eq(version.buyerTaxpayerIdSnapshot, form.getBuyerTaxpayerIdSnapshot())
                || !Cmp.eq(version.sellerTaxpayerIdSnapshot, form.getSellerTaxpayerIdSnapshot())
                || !Cmp.eq(version.buyerSignatoryPersonId, form.getBuyerSignatoryPersonId())
                || !Cmp.eq(version.sellerSignatoryPersonId, form.getSellerSignatoryPersonId())
                || !Cmp.eq(version.buyerContactPersonId, form.getBuyerContactPersonId())
                || !Cmp.eq(version.sellerContactPersonId, form.getSellerContactPersonId())
                || !Cmp.eq(version.procurementCompanyId, form.getProcurementCompanyId())
                || !Cmp.eq(version.supplierId, form.getSupplierId())
                || !Cmp.eq(version.currency, form.getCurrency())
                || !Cmp.eq(version.pricingMethod, form.getPricingMethod())
                || !Cmp.eq(version.contractNetAmount, form.getContractNetAmount())
                || !Cmp.eq(version.contractTaxAmount, form.getContractTaxAmount())
                || !Cmp.eq(version.contractGrossAmount, form.getContractGrossAmount())
                || !Cmp.eq(version.signedAt, form.getSignedAt())
                || !Cmp.eq(version.terminationDate, form.getTerminationDate())
                || !Cmp.eq(version.terminationReason, form.getTerminationReason())
                || !Cmp.eq(version.agreedValidFrom, form.getValidFrom())
                || !Cmp.eq(version.agreedValidTo, form.getValidTo());
    }

    private void applyLine(ContractLineTermVersion term, LineForm row) {
        term.itemName = row.getItemName();
        term.itemSpecification = row.getItemSpecification();
        term.unit = row.getUnit();
        term.contractedQuantity = row.getContractedQuantity();
        term.unitNetPrice = row.getUnitNetPrice();
        term.taxRate = row.getTaxRate();
        term.lineNetAmount = row.getLineNetAmount();
        term.lineTaxAmount = row.getLineTaxAmount();
        term.lineGrossAmount = row.getLineGrossAmount();
    }

    private void applyStageCommercial(ContractStageTermVersion term, StageForm row) {
        term.stageName = row.getStageName();
        term.deliverableDescription = row.getDeliverableDescription();
        term.acceptanceCriteria = row.getAcceptanceCriteria();
        term.plannedStartAt = row.getPlannedStartAt();
        term.plannedDueAt = row.getPlannedDueAt();
        term.stageNetAmount = row.getStageNetAmount();
        term.stageTaxAmount = row.getStageTaxAmount();
        term.stageGrossAmount = row.getStageGrossAmount();
        term.currency = row.getCurrency();
    }

    private ContractLineTermVersion copyLineTerm(ContractLineTermVersion source, Long contractTermVersionId, LocalDate validFrom) {
        ContractLineTermVersion copy = new ContractLineTermVersion();
        copy.contractLineId = source.contractLineId;
        copy.contractTermVersionId = contractTermVersionId;
        copy.itemName = source.itemName;
        copy.itemSpecification = source.itemSpecification;
        copy.unit = source.unit;
        copy.contractedQuantity = source.contractedQuantity;
        copy.unitNetPrice = source.unitNetPrice;
        copy.taxRate = source.taxRate;
        copy.lineNetAmount = source.lineNetAmount;
        copy.lineTaxAmount = source.lineTaxAmount;
        copy.lineGrossAmount = source.lineGrossAmount;
        copy.validFrom = validFrom;
        return copy;
    }

    private ContractStageTermVersion copyStageTerm(ContractStageTermVersion source, Long amendmentId, LocalDate validFrom, String currency) {
        ContractStageTermVersion copy = new ContractStageTermVersion();
        copy.contractStageId = source.contractStageId;
        copy.versionNumber = source.versionNumber + 1;
        copy.amendmentId = amendmentId;
        copy.stageName = source.stageName;
        copy.deliverableDescription = source.deliverableDescription;
        copy.acceptanceCriteria = source.acceptanceCriteria;
        copy.plannedStartAt = source.plannedStartAt;
        copy.plannedDueAt = source.plannedDueAt;
        copy.stageNetAmount = source.stageNetAmount;
        copy.stageTaxAmount = source.stageTaxAmount;
        copy.stageGrossAmount = source.stageGrossAmount;
        copy.currency = currency;
        copy.validFrom = validFrom;
        copy.recordedAt = now();
        return copy;
    }

    private ContractStageDeliverable copyDeliverable(ContractStageDeliverable source, Long stageTermId, Map<Long, Long> lineTermMap) {
        ContractStageDeliverable copy = new ContractStageDeliverable();
        copy.stageTermVersionId = stageTermId;
        if (source.contractLineTermVersionId == null) {
            copy.contractLineTermVersionId = null;
        } else {
            copy.contractLineTermVersionId = lineTermMap.getOrDefault(source.contractLineTermVersionId, source.contractLineTermVersionId);
        }
        copy.deliverableCode = source.deliverableCode;
        copy.deliverableName = source.deliverableName;
        copy.deliverableSpecification = source.deliverableSpecification;
        copy.unit = source.unit;
        copy.plannedQuantity = source.plannedQuantity;
        copy.acceptanceCriteria = source.acceptanceCriteria;
        copy.plannedDeliveryDate = source.plannedDeliveryDate;
        copy.plannedAcceptanceDate = source.plannedAcceptanceDate;
        return copy;
    }

    private ContractLineTermVersion currentLineTerm(Long lineId, Long contractTermVersionId) {
        for (ContractLineTermVersion term : lineTerms.findByContractLineIdAndContractTermVersionId(lineId, contractTermVersionId)) {
            if (term.validTo == null) {
                return term;
            }
        }
        return null;
    }

    private ContractStageTermVersion openStageTerm(Long stageId) {
        ContractStageTermVersion open = null;
        for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(stageId)) {
            if (term.validTo == null) {
                open = term;
            }
        }
        return open;
    }

    private boolean lineHasHistory(Long lineId, Long openTermId) {
        for (ContractLineTermVersion term : lineTerms.findByContractLineId(lineId)) {
            if (!openTermId.equals(term.contractTermVersionId)) {
                return true;
            }
        }
        return false;
    }

    private boolean stageHasHistory(Long stageId) {
        for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(stageId)) {
            if (term.validTo != null) {
                return true;
            }
        }
        return false;
    }

    private ContractTermVersion openTerm(Long contractId) {
        ContractTermVersion open = null;
        for (ContractTermVersion version : terms.findByContractIdOrderByVersionNumberAsc(contractId)) {
            if (version.validTo == null) {
                open = version;
            }
        }
        if (open == null) {
            throw new NotFoundException();
        }
        return open;
    }

    private Contract get(Long id) {
        return contracts.findById(id).orElseThrow(NotFoundException::new);
    }

    private ContractForm toForm(Contract contract) {
        ContractForm form = new ContractForm();
        form.setId(contract.id);
        form.setContractNumber(contract.contractNumber);
        form.setContractType(contract.contractType);
        form.setContractTitle(contract.contractTitle);
        form.setManagingCompanyId(contract.managingCompanyId);
        form.setProcurementCompanyId(contract.procurementCompanyId);
        form.setSupplierId(contract.supplierId);
        form.setContractOwnerEmployeeId(contract.contractOwnerEmployeeId);
        form.setBuyerSignatoryPersonId(contract.buyerSignatoryPersonId);
        form.setSellerSignatoryPersonId(contract.sellerSignatoryPersonId);
        form.setBuyerNameSnapshot(contract.buyerNameSnapshot);
        form.setSellerNameSnapshot(contract.sellerNameSnapshot);
        form.setBuyerTaxpayerIdSnapshot(contract.buyerTaxpayerIdSnapshot);
        form.setSellerTaxpayerIdSnapshot(contract.sellerTaxpayerIdSnapshot);
        form.setBuyerContactPersonId(contract.buyerContactPersonId);
        form.setSellerContactPersonId(contract.sellerContactPersonId);
        form.setBuyerCompanyId(contract.buyerCompanyId);
        form.setBuyerLegalPersonId(contract.buyerLegalPersonId);
        form.setBuyerPersonId(contract.buyerPersonId);
        form.setSellerCompanyId(contract.sellerCompanyId);
        form.setSellerLegalPersonId(contract.sellerLegalPersonId);
        form.setSellerPersonId(contract.sellerPersonId);
        form.setSignedAt(contract.signedAt);
        form.setValidFrom(contract.validFrom);
        form.setValidTo(contract.validTo);
        form.setCurrency(contract.currency);
        form.setPricingMethod(contract.pricingMethod);
        form.setContractNetAmount(contract.contractNetAmount);
        form.setContractTaxAmount(contract.contractTaxAmount);
        form.setContractGrossAmount(contract.contractGrossAmount);
        form.setApprovalStatus(contract.approvalStatus);
        form.setContractApprovedAt(contract.contractApprovedAt);
        form.setContractApprovedByEmployeeId(contract.contractApprovedByEmployeeId);
        form.setTerminationDate(contract.terminationDate);
        form.setTerminationReason(contract.terminationReason);
        form.setStatus(contract.status);
        form.setSourceDocumentId(contract.sourceDocumentId);
        return form;
    }

    private LineForm toLine(ContractLine line, ContractLineTermVersion term) {
        LineForm row = new LineForm();
        row.setId(line.id);
        row.setLineNumber(line.lineNumber);
        row.setItemName(term.itemName);
        row.setItemSpecification(term.itemSpecification);
        row.setUnit(term.unit);
        row.setContractedQuantity(term.contractedQuantity);
        row.setUnitNetPrice(term.unitNetPrice);
        row.setTaxRate(term.taxRate);
        row.setLineNetAmount(term.lineNetAmount);
        row.setLineTaxAmount(term.lineTaxAmount);
        row.setLineGrossAmount(term.lineGrossAmount);
        return row;
    }

    private StageForm toStage(ContractStage stage, ContractStageTermVersion term) {
        StageForm row = new StageForm();
        row.setId(stage.id);
        row.setStageCode(stage.stageCode);
        row.setSequenceNo(stage.sequenceNo);
        row.setStageName(term.stageName);
        row.setResponsibleEmployeeId(stage.responsibleEmployeeId);
        row.setStatus(stage.status);
        row.setDeliverableDescription(term.deliverableDescription);
        row.setAcceptanceCriteria(term.acceptanceCriteria);
        row.setPlannedStartAt(term.plannedStartAt);
        row.setPlannedDueAt(term.plannedDueAt);
        row.setStageNetAmount(term.stageNetAmount);
        row.setStageTaxAmount(term.stageTaxAmount);
        row.setStageGrossAmount(term.stageGrossAmount);
        row.setCurrency(term.currency);
        return row;
    }

    private DeliverableForm toDeliverable(ContractStage stage, ContractStageDeliverable item) {
        DeliverableForm row = new DeliverableForm();
        row.setId(item.id);
        row.setStageId(stage.id);
        row.setStageCode(stage.stageCode);
        row.setContractLineTermVersionId(item.contractLineTermVersionId);
        row.setDeliverableCode(item.deliverableCode);
        row.setDeliverableName(item.deliverableName);
        row.setDeliverableSpecification(item.deliverableSpecification);
        row.setUnit(item.unit);
        row.setPlannedQuantity(item.plannedQuantity);
        row.setAcceptanceCriteria(item.acceptanceCriteria);
        row.setPlannedDeliveryDate(item.plannedDeliveryDate);
        row.setPlannedAcceptanceDate(item.plannedAcceptanceDate);
        return row;
    }

    private SignatoryForm toSignatory(ContractAdditionalSignatory signatory) {
        SignatoryForm row = new SignatoryForm();
        row.setId(signatory.id);
        row.setSignatoryCompanyId(signatory.signatoryCompanyId);
        row.setSignatoryLegalPersonId(signatory.signatoryLegalPersonId);
        row.setSignatoryPersonId(signatory.signatoryPersonId);
        row.setSignatoryRole(signatory.signatoryRole);
        row.setResponsibilityDescription(signatory.responsibilityDescription);
        row.setValidFrom(signatory.validFrom);
        row.setValidTo(signatory.validTo);
        row.setBasisDocumentId(signatory.basisDocumentId);
        return row;
    }

    private LinkForm toLink(ProjectContract link) {
        LinkForm row = new LinkForm();
        row.setId(link.id);
        row.setProjectId(link.projectId);
        row.setRelationshipNote(link.relationshipNote);
        return row;
    }

    private void checkMoney(BigDecimal net, BigDecimal tax, BigDecimal gross, FieldErrors errors, String field, String label) {
        if (isNegative(net) || isNegative(tax) || isNegative(gross)) {
            errors.add(field, label + "金额不能为负");
            return;
        }
        if (net != null && tax != null && gross != null && gross.compareTo(net.add(tax)) != 0) {
            errors.add(field, label + "含税价须等于不含税价加税额");
        }
    }

    private void checkRate(BigDecimal rate, BigDecimal net, BigDecimal tax, FieldErrors errors, String field) {
        if (rate == null) {
            return;
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            errors.add(field, "税率按 0 到 1 填写，例如 0.13");
            return;
        }
        if (net != null && tax != null) {
            BigDecimal expected = net.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            if (expected.subtract(tax).abs().compareTo(new BigDecimal("0.01")) > 0) {
                errors.add(field, "税额与不含税价乘税率不一致");
            }
        }
    }

    private void checkQuantity(BigDecimal quantity, FieldErrors errors, String field, boolean required) {
        if (quantity == null) {
            if (required) {
                errors.add(field, "请填写计划数量");
            }
            return;
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(field, "数量未定时请留空，已知数量须大于 0");
        }
    }

    private boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    private void requireParty(Long companyId, Long legalPersonId, Long personId, FieldErrors errors, String field, String label) {
        int count = (companyId == null ? 0 : 1) + (legalPersonId == null ? 0 : 1) + (personId == null ? 0 : 1);
        if (count != 1) {
            errors.add(field, label + "须在公司、法人、自然人中恰好选择一个");
            return;
        }
        if (companyId != null && companies.findById(companyId).isEmpty()) {
            errors.add(field, label + "公司不存在");
        }
        if (legalPersonId != null && legalPersons.findById(legalPersonId).isEmpty()) {
            errors.add(field, label + "法人不存在");
        }
        if (personId != null && persons.findById(personId).isEmpty()) {
            errors.add(field, label + "自然人不存在");
        }
    }

    private boolean sameParty(Long companyId, Long legalPersonId, Long personId, Long otherCompanyId, Long otherLegalPersonId, Long otherPersonId) {
        return Cmp.eq(companyId, otherCompanyId) && Cmp.eq(legalPersonId, otherLegalPersonId) && Cmp.eq(personId, otherPersonId);
    }

    private void requirePerson(Long personId, FieldErrors errors, String field, String label) {
        if (personId != null && persons.findById(personId).isEmpty()) {
            errors.add(field, label + "不存在");
        }
    }

    private void requireEmployee(Long employeeId, FieldErrors errors, String field, String label) {
        if (employeeId != null && employees.findById(employeeId).isEmpty()) {
            errors.add(field, label + "不存在");
        }
    }

    private String partyLabel(
            Long companyId,
            Long legalPersonId,
            Long personId,
            Map<Long, Company> companyMap,
            Map<Long, LegalPerson> legalMap,
            Map<Long, Person> personMap) {
        if (companyId != null) {
            Company company = companyMap.get(companyId);
            return company == null ? "未找到公司" : company.name;
        }
        if (legalPersonId != null) {
            LegalPerson legalPerson = legalMap.get(legalPersonId);
            return legalPerson == null ? "未找到法人" : legalPerson.legalName;
        }
        if (personId != null) {
            Person person = personMap.get(personId);
            return person == null ? "未找到自然人" : person.name;
        }
        return "未指定";
    }

    private Map<Long, Company> companyMap() {
        return companies.findAll().stream().collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
    }

    private Map<Long, LegalPerson> legalMap() {
        return legalPersons.findAll().stream().collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
    }

    private Map<Long, Person> personMap() {
        return persons.findAll().stream().collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
    }

    private LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    }
}
