package com.liangshan.demo.service;

import com.liangshan.demo.domain.AcceptanceEvent;
import com.liangshan.demo.domain.AcceptanceLine;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.ContractBillingSchedule;
import com.liangshan.demo.domain.ContractPaymentSchedule;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.ContractStageDeliverable;
import com.liangshan.demo.domain.ContractStageTermVersion;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.DeliveryEvent;
import com.liangshan.demo.domain.DeliveryLine;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ProjectContract;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.AcceptanceLineRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractPaymentScheduleRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageDeliverableRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractStageTermVersionRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.DeliveryEventRepository;
import com.liangshan.demo.repository.DeliveryLineRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectContractRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.AcceptanceEventForm;
import com.liangshan.demo.web.form.ContractPlanForm;
import com.liangshan.demo.web.form.ContractPlanForm.BillingRow;
import com.liangshan.demo.web.form.ContractPlanForm.PaymentRow;
import com.liangshan.demo.web.form.DeliveryEventForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 开票计划、收付款计划、交付和验收。
 * 累计不通过是复验后仍未通过的数量，不是历次不通过数量相加。
 * 复验只记录尚未通过的部分，已通过数量留在原来的验收明细上。
 */
@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private final ContractRepository contracts;
    private final ContractTermVersionRepository terms;
    private final ContractStageRepository stages;
    private final ContractStageTermVersionRepository stageTerms;
    private final ContractStageDeliverableRepository deliverables;
    private final ProjectContractRepository projectContracts;
    private final ProjectRepository projects;
    private final ContractBillingScheduleRepository billings;
    private final ContractPaymentScheduleRepository payments;
    private final DeliveryEventRepository deliveryEvents;
    private final DeliveryLineRepository deliveryLines;
    private final AcceptanceEventRepository acceptanceEvents;
    private final AcceptanceLineRepository acceptanceLines;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final SourceDocumentService documents;

    public record ProgressRow(
            Long stageId,
            String stageCode,
            String deliverableCode,
            String deliverableName,
            String unit,
            BigDecimal plannedQuantity,
            BigDecimal deliveredQuantity,
            BigDecimal acceptedQuantity,
            BigDecimal rejectedQuantity) {
    }

    public record DeliverableChoice(Long id, String label, String unit) {
    }

    public record DeliveryView(
            Long id,
            String deliveryNumber,
            String status,
            LocalDateTime submittedAt,
            String description,
            String stageCode,
            Long resubmissionOfDeliveryEventId,
            List<LineView> lines,
            List<AcceptanceView> acceptances,
            boolean openForAcceptance) {
    }

    public record LineView(
            Long id,
            String deliverableCode,
            String deliverableName,
            BigDecimal submittedQuantity,
            BigDecimal acceptedQuantity,
            BigDecimal outstandingRejected,
            String unit,
            boolean open) {
    }

    public record AcceptanceView(
            Long id,
            String acceptanceNumber,
            String acceptanceResult,
            String status,
            LocalDateTime acceptedAt,
            String comments,
            List<AcceptanceLine> lines) {
    }

    public List<ProgressRow> progress(Long contractId) {
        requireContract(contractId);
        Map<String, MutableProgress> rows = new HashMap<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            ContractStageTermVersion current = currentTerm(stage.id);
            if (current != null) {
                for (ContractStageDeliverable item : deliverables.findByStageTermVersionIdOrderByIdAsc(current.id)) {
                    MutableProgress row = rows.computeIfAbsent(key(stage.id, item.deliverableCode), ignored -> new MutableProgress());
                    row.stageId = stage.id;
                    row.stageCode = stage.stageCode;
                    row.deliverableCode = item.deliverableCode;
                    row.deliverableName = item.deliverableName;
                    row.unit = item.unit;
                    row.planned = item.plannedQuantity;
                }
            }
            for (DeliveryEvent event : deliveryEvents.findByContractStageIdOrderBySubmittedAtAscIdAsc(stage.id)) {
                if (!Codes.DELIVERY_SUBMITTED.equals(event.status)) {
                    continue;
                }
                for (DeliveryLine line : deliveryLines.findByDeliveryEventIdOrderByIdAsc(event.id)) {
                    ContractStageDeliverable item = deliverables.findById(line.stageDeliverableId).orElse(null);
                    if (item == null) {
                        continue;
                    }
                    MutableProgress row = rows.computeIfAbsent(key(stage.id, item.deliverableCode), ignored -> new MutableProgress());
                    if (row.stageCode == null) {
                        row.stageId = stage.id;
                        row.stageCode = stage.stageCode;
                        row.deliverableCode = item.deliverableCode;
                        row.deliverableName = item.deliverableName;
                        row.unit = item.unit;
                        row.planned = item.plannedQuantity;
                    }
                    if (event.resubmissionOfDeliveryEventId == null) {
                        row.delivered = row.delivered.add(line.submittedQuantity);
                    }
                    Pool pool = settle(line, List.of(), null);
                    row.accepted = row.accepted.add(pool.accepted);
                    row.rejected = row.rejected.add(pool.rejected);
                }
            }
        }
        List<ProgressRow> result = new ArrayList<>();
        for (MutableProgress row : rows.values()) {
            result.add(new ProgressRow(
                    row.stageId,
                    row.stageCode,
                    row.deliverableCode,
                    row.deliverableName,
                    row.unit,
                    plain(row.planned),
                    plain(row.delivered),
                    plain(row.accepted),
                    plain(row.rejected)));
        }
        result.sort(Comparator.comparing((ProgressRow row) -> row.stageCode == null ? "" : row.stageCode)
                .thenComparing(row -> row.deliverableCode == null ? "" : row.deliverableCode));
        return result;
    }

    public List<DeliveryView> deliveries(Long contractId) {
        requireContract(contractId);
        List<DeliveryView> views = new ArrayList<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            for (DeliveryEvent event : deliveryEvents.findByContractStageIdOrderBySubmittedAtAscIdAsc(stage.id)) {
                List<LineView> lines = new ArrayList<>();
                boolean open = false;
                for (DeliveryLine line : deliveryLines.findByDeliveryEventIdOrderByIdAsc(event.id)) {
                    ContractStageDeliverable item = deliverables.findById(line.stageDeliverableId).orElse(null);
                    Pool pool = settle(line, List.of(), null);
                    boolean lineOpen = Codes.DELIVERY_SUBMITTED.equals(event.status)
                            && pool.accepted.compareTo(line.submittedQuantity) < 0;
                    if (lineOpen) {
                        open = true;
                    }
                    lines.add(new LineView(
                            line.id,
                            item == null ? "" : item.deliverableCode,
                            item == null ? "" : item.deliverableName,
                            plain(line.submittedQuantity),
                            plain(pool.accepted),
                            plain(pool.rejected),
                            line.unit,
                            lineOpen));
                }
                List<AcceptanceView> acceptances = new ArrayList<>();
                for (AcceptanceEvent acceptance : acceptanceEvents.findByDeliveryEventIdOrderByAcceptedAtAscIdAsc(event.id)) {
                    acceptances.add(new AcceptanceView(
                            acceptance.id,
                            acceptance.acceptanceNumber,
                            acceptance.acceptanceResult,
                            acceptance.status,
                            acceptance.acceptedAt,
                            acceptance.acceptanceComments,
                            acceptanceLines.findByAcceptanceEventIdOrderByIdAsc(acceptance.id)));
                }
                views.add(new DeliveryView(
                        event.id,
                        event.deliveryNumber,
                        event.status,
                        event.submittedAt,
                        event.deliverableDescription,
                        stage.stageCode,
                        event.resubmissionOfDeliveryEventId,
                        lines,
                        acceptances,
                        open));
            }
        }
        return views;
    }

    public ContractPlanForm planForm(Long contractId) {
        requireContract(contractId);
        ContractPlanForm form = new ContractPlanForm();
        for (ContractBillingSchedule row : billings.findByContractIdOrderByScheduleNumberAsc(contractId)) {
            BillingRow item = new BillingRow();
            item.setId(row.id);
            item.setContractTermVersionId(row.contractTermVersionId);
            item.setContractStageId(row.contractStageId);
            item.setScheduleNumber(row.scheduleNumber);
            item.setBillingTrigger(row.billingTrigger);
            item.setPlannedInvoiceGrossAmount(row.plannedInvoiceGrossAmount);
            item.setCurrency(row.currency);
            item.setPlannedInvoiceDate(row.plannedInvoiceDate);
            item.setDaysAfterTrigger(row.daysAfterTrigger);
            item.setTriggerDescription(row.triggerDescription);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setSupersedesBillingScheduleId(row.supersedesBillingScheduleId);
            form.getBillings().add(item);
        }
        for (ContractPaymentSchedule row : payments.findByContractIdOrderByScheduleNumberAsc(contractId)) {
            PaymentRow item = new PaymentRow();
            item.setId(row.id);
            item.setContractTermVersionId(row.contractTermVersionId);
            item.setContractStageId(row.contractStageId);
            item.setScheduleNumber(row.scheduleNumber);
            item.setInstallmentName(row.installmentName);
            item.setPaymentPurpose(row.paymentPurpose);
            item.setPaymentTrigger(row.paymentTrigger);
            item.setPlannedPaymentAmount(row.plannedPaymentAmount);
            item.setCurrency(row.currency);
            item.setPlannedDueDate(row.plannedDueDate);
            item.setDaysAfterTrigger(row.daysAfterTrigger);
            item.setTriggerDescription(row.triggerDescription);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setSupersedesPaymentScheduleId(row.supersedesPaymentScheduleId);
            form.getPayments().add(item);
        }
        return form;
    }

    public List<ContractTermVersion> terms(Long contractId) {
        return terms.findByContractIdOrderByVersionNumberAsc(contractId);
    }

    public List<ContractStage> stageChoices(Long contractId) {
        return stages.findByContractIdOrderBySequenceNoAsc(contractId);
    }

    public List<Project> linkedProjects(Long contractId) {
        List<Project> result = new ArrayList<>();
        for (ProjectContract link : projectContracts.findByContractIdOrderByIdAsc(contractId)) {
            projects.findById(link.projectId).ifPresent(result::add);
        }
        return result;
    }

    public List<DeliverableChoice> deliverableChoices(Long contractId) {
        List<DeliverableChoice> result = new ArrayList<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            ContractStageTermVersion current = currentTerm(stage.id);
            if (current == null) {
                continue;
            }
            for (ContractStageDeliverable item : deliverables.findByStageTermVersionIdOrderByIdAsc(current.id)) {
                String label = stage.stageCode + " " + item.deliverableCode + " " + item.deliverableName
                        + " 约定 " + plain(item.plannedQuantity).toPlainString() + " " + item.unit;
                result.add(new DeliverableChoice(item.id, label, item.unit));
            }
        }
        return result;
    }

    @Transactional
    public void savePlans(Long contractId, ContractPlanForm form) {
        requireContract(contractId);
        if (form.getBillings() == null) {
            form.setBillings(new ArrayList<>());
        }
        if (form.getPayments() == null) {
            form.setPayments(new ArrayList<>());
        }
        normalizePlans(form);
        FieldErrors errors = new FieldErrors();
        List<BillingRow> billingRows = keptBillings(form);
        List<PaymentRow> paymentRows = keptPayments(form);
        validateBillings(contractId, billingRows, errors);
        validatePayments(contractId, paymentRows, errors);
        errors.throwIfAny();
        Map<Long, ContractBillingSchedule> storedBillings = new HashMap<>();
        for (ContractBillingSchedule row : billings.findByContractIdOrderByScheduleNumberAsc(contractId)) {
            storedBillings.put(row.id, row);
        }
        Map<Long, ContractPaymentSchedule> storedPayments = new HashMap<>();
        for (ContractPaymentSchedule row : payments.findByContractIdOrderByScheduleNumberAsc(contractId)) {
            storedPayments.put(row.id, row);
        }
        Set<Long> keptBillingIds = new HashSet<>();
        List<ContractBillingSchedule> savedBillings = new ArrayList<>();
        for (BillingRow row : billingRows) {
            ContractBillingSchedule schedule = row.getId() == null
                    ? new ContractBillingSchedule()
                    : storedBillings.get(row.getId());
            applyBilling(schedule, contractId, row);
            schedule = billings.save(schedule);
            keptBillingIds.add(schedule.id);
            savedBillings.add(schedule);
        }
        for (ContractBillingSchedule saved : savedBillings) {
            if (saved.supersedesBillingScheduleId == null) {
                continue;
            }
            ContractBillingSchedule prior = storedBillings.get(saved.supersedesBillingScheduleId);
            if (prior != null && prior.validTo == null && keptBillingIds.contains(prior.id)) {
                prior.validTo = saved.validFrom;
                billings.save(prior);
            }
        }
        for (ContractBillingSchedule existing : storedBillings.values()) {
            if (!keptBillingIds.contains(existing.id)) {
                billings.delete(existing);
            }
        }
        Set<Long> keptPaymentIds = new HashSet<>();
        List<ContractPaymentSchedule> savedPayments = new ArrayList<>();
        for (PaymentRow row : paymentRows) {
            ContractPaymentSchedule schedule = row.getId() == null
                    ? new ContractPaymentSchedule()
                    : storedPayments.get(row.getId());
            applyPayment(schedule, contractId, row);
            schedule = payments.save(schedule);
            keptPaymentIds.add(schedule.id);
            savedPayments.add(schedule);
        }
        for (ContractPaymentSchedule saved : savedPayments) {
            if (saved.supersedesPaymentScheduleId == null) {
                continue;
            }
            ContractPaymentSchedule prior = storedPayments.get(saved.supersedesPaymentScheduleId);
            if (prior != null && prior.validTo == null && keptPaymentIds.contains(prior.id)) {
                prior.validTo = saved.validFrom;
                payments.save(prior);
            }
        }
        for (ContractPaymentSchedule existing : storedPayments.values()) {
            if (!keptPaymentIds.contains(existing.id)) {
                payments.delete(existing);
            }
        }
    }

    @Transactional
    public long recordDelivery(Long contractId, DeliveryEventForm form) {
        requireContract(contractId);
        normalizeDelivery(form);
        FieldErrors errors = new FieldErrors();
        if (form.getDeliveryNumber() == null) {
            errors.add("deliveryNumber", "请填写交付编号");
        } else if (form.getDeliveryNumber().length() > 64) {
            errors.add("deliveryNumber", "交付编号过长");
        } else if (deliveryNumberTaken(contractId, form.getDeliveryNumber())) {
            errors.add("deliveryNumber", "交付编号已存在");
        }
        if (form.getSubmittedAt() == null) {
            errors.add("submittedAt", "请填写提交时间");
        }
        if (form.getDeliverableDescription() == null) {
            errors.add("deliverableDescription", "请填写交付说明");
        } else if (form.getDeliverableDescription().length() > 1000) {
            errors.add("deliverableDescription", "交付说明过长");
        }
        if (form.getProjectId() == null || projects.findById(form.getProjectId()).isEmpty()) {
            errors.add("projectId", "请选择已关联的项目");
        } else if (projectContracts.findByContractIdOrderByIdAsc(contractId).stream()
                .noneMatch(link -> form.getProjectId().equals(link.projectId))) {
            errors.add("projectId", "项目未关联这份合同");
        }
        requireParty(form.getSubmitterCompanyId(), form.getSubmitterLegalPersonId(), form.getSubmitterPersonId(),
                errors, "submitterCompanyId", "提交方");
        if (form.getSubmittedByEmployeeId() != null && employees.findById(form.getSubmittedByEmployeeId()).isEmpty()) {
            errors.add("submittedByEmployeeId", "提交员工不存在");
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        List<DeliveryEventForm.Line> lines = new ArrayList<>();
        if (form.getLines() != null) {
            for (DeliveryEventForm.Line line : form.getLines()) {
                if (line != null && !line.isRemoved()) {
                    lines.add(line);
                }
            }
        }
        if (lines.isEmpty()) {
            errors.add("lines", "请至少填写一条交付明细");
        }
        Long stageId = null;
        Long stageTermId = null;
        Set<Long> deliverableIds = new HashSet<>();
        List<ResolvedLine> resolved = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            DeliveryEventForm.Line line = lines.get(i);
            String prefix = "lines[" + i + "].";
            ContractStageDeliverable item = line.getStageDeliverableId() == null
                    ? null
                    : deliverables.findById(line.getStageDeliverableId()).orElse(null);
            if (item == null) {
                errors.add(prefix + "stageDeliverableId", "请选择交付项");
                continue;
            }
            if (!deliverableIds.add(item.id)) {
                errors.add(prefix + "stageDeliverableId", "同一次提交不能重复同一交付项");
                continue;
            }
            ContractStageTermVersion term = stageTerms.findById(item.stageTermVersionId).orElse(null);
            ContractStage stage = term == null ? null : stages.findById(term.contractStageId).orElse(null);
            if (stage == null || !contractId.equals(stage.contractId)) {
                errors.add(prefix + "stageDeliverableId", "交付项不属于该合同");
                continue;
            }
            if (stageTermId == null) {
                stageTermId = term.id;
                stageId = stage.id;
            } else if (!stageTermId.equals(term.id)) {
                errors.add(prefix + "stageDeliverableId", "一次提交的交付项须属于同一阶段条款");
            }
            if (line.getUnit() != null && !line.getUnit().equals(item.unit)) {
                errors.add(prefix + "unit", "单位须与交付项一致");
            }
            if (line.getSubmittedQuantity() == null) {
                errors.add(prefix + "submittedQuantity", "请填写交付数量");
            } else if (line.getSubmittedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(prefix + "submittedQuantity", "交付数量须大于 0");
            }
            documents.require(line.getDeliverableDocumentId(), errors, prefix + "deliverableDocumentId");
            if (line.getDeliveredArtifactVersion() != null && line.getDeliveredArtifactVersion().length() > 64) {
                errors.add(prefix + "deliveredArtifactVersion", "成果版本过长");
            }
            resolved.add(new ResolvedLine(line, item));
        }
        DeliveryEvent ancestor = null;
        if (form.getResubmissionOfDeliveryEventId() != null) {
            ancestor = deliveryEvents.findById(form.getResubmissionOfDeliveryEventId()).orElse(null);
            if (ancestor == null || !contractOwnsDelivery(contractId, ancestor)) {
                errors.add("resubmissionOfDeliveryEventId", "重提的原交付不属于该合同");
            } else if (!Codes.DELIVERY_SUBMITTED.equals(ancestor.status)) {
                errors.add("resubmissionOfDeliveryEventId", "只能重提仍处于已提交的交付");
            }
        }
        errors.throwIfAny();
        if (ancestor != null) {
            Map<String, BigDecimal> room = outstandingByCode(ancestor);
            Map<String, BigDecimal> asked = new HashMap<>();
            for (ResolvedLine line : resolved) {
                asked.merge(line.item.deliverableCode, line.form.getSubmittedQuantity(), BigDecimal::add);
            }
            for (Map.Entry<String, BigDecimal> entry : asked.entrySet()) {
                BigDecimal allowed = room.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                if (entry.getValue().compareTo(allowed) > 0) {
                    throw new FieldErrorsException(Map.of("lines", "重提数量不能超过尚未通过的数量"));
                }
            }
        }
        DeliveryEvent event = new DeliveryEvent();
        event.projectId = form.getProjectId();
        event.contractStageId = stageId;
        event.stageTermVersionId = stageTermId;
        event.submitterCompanyId = form.getSubmitterCompanyId();
        event.submitterLegalPersonId = form.getSubmitterLegalPersonId();
        event.submitterPersonId = form.getSubmitterPersonId();
        event.submittedByEmployeeId = form.getSubmittedByEmployeeId();
        event.deliveryNumber = form.getDeliveryNumber();
        event.submittedAt = form.getSubmittedAt().truncatedTo(ChronoUnit.MINUTES);
        event.deliverableDescription = form.getDeliverableDescription();
        event.resubmissionOfDeliveryEventId = form.getResubmissionOfDeliveryEventId();
        event.status = Codes.DELIVERY_SUBMITTED;
        event.sourceDocumentId = form.getSourceDocumentId();
        event = deliveryEvents.save(event);
        for (ResolvedLine line : resolved) {
            DeliveryLine stored = new DeliveryLine();
            stored.deliveryEventId = event.id;
            stored.stageDeliverableId = line.item.id;
            stored.submittedQuantity = line.form.getSubmittedQuantity();
            stored.unit = line.item.unit;
            stored.deliveredArtifactVersion = line.form.getDeliveredArtifactVersion();
            stored.deliverableDocumentId = line.form.getDeliverableDocumentId();
            deliveryLines.save(stored);
        }
        return event.id;
    }

    @Transactional
    public long recordAcceptance(Long contractId, AcceptanceEventForm form) {
        requireContract(contractId);
        normalizeAcceptance(form);
        FieldErrors errors = new FieldErrors();
        if (form.getAcceptanceNumber() == null) {
            errors.add("acceptanceNumber", "请填写验收编号");
        } else if (form.getAcceptanceNumber().length() > 64) {
            errors.add("acceptanceNumber", "验收编号过长");
        } else if (acceptanceNumberTaken(contractId, form.getAcceptanceNumber())) {
            errors.add("acceptanceNumber", "验收编号已存在");
        }
        if (form.getAcceptedAt() == null) {
            errors.add("acceptedAt", "请填写验收时间");
        }
        if (form.getAcceptanceComments() != null && form.getAcceptanceComments().length() > 1000) {
            errors.add("acceptanceComments", "验收意见过长");
        }
        requireParty(form.getAcceptorCompanyId(), form.getAcceptorLegalPersonId(), form.getAcceptorPersonId(),
                errors, "acceptorCompanyId", "验收方");
        if (form.getAcceptedByEmployeeId() != null && form.getAcceptedByPersonId() != null) {
            Employee employee = employees.findById(form.getAcceptedByEmployeeId()).orElse(null);
            if (employee == null) {
                errors.add("acceptedByEmployeeId", "验收员工不存在");
            } else if (!form.getAcceptedByPersonId().equals(employee.personId)) {
                errors.add("acceptedByPersonId", "验收员工与外部签署人须是同一自然人");
            }
        } else {
            if (form.getAcceptedByEmployeeId() != null && employees.findById(form.getAcceptedByEmployeeId()).isEmpty()) {
                errors.add("acceptedByEmployeeId", "验收员工不存在");
            }
            if (form.getAcceptedByPersonId() != null && persons.findById(form.getAcceptedByPersonId()).isEmpty()) {
                errors.add("acceptedByPersonId", "外部签署人不存在");
            }
        }
        documents.require(form.getSourceDocumentId(), errors, "sourceDocumentId");
        DeliveryEvent delivery = form.getDeliveryEventId() == null
                ? null
                : deliveryEvents.findById(form.getDeliveryEventId()).orElse(null);
        if (delivery == null || !contractOwnsDelivery(contractId, delivery)) {
            errors.add("deliveryEventId", "交付不属于该合同");
        } else if (!Codes.DELIVERY_SUBMITTED.equals(delivery.status)) {
            errors.add("deliveryEventId", "已撤回的交付不能验收");
        }
        Long revokeId = null;
        if (form.getSupersedesAcceptanceEventId() != null) {
            AcceptanceEvent prior = acceptanceEvents.findById(form.getSupersedesAcceptanceEventId()).orElse(null);
            if (prior == null || delivery == null || !delivery.id.equals(prior.deliveryEventId)) {
                errors.add("supersedesAcceptanceEventId", "更正须指向同一次交付上的验收");
            } else if (!Codes.ACCEPTANCE_EFFECTIVE.equals(prior.status)) {
                errors.add("supersedesAcceptanceEventId", "只能更正仍有效的验收");
            } else if (acceptanceEvents.existsBySupersedesAcceptanceEventId(prior.id)) {
                errors.add("supersedesAcceptanceEventId", "该验收已被更正");
            } else {
                revokeId = prior.id;
            }
        }
        List<AcceptanceEventForm.Line> lines = new ArrayList<>();
        if (form.getLines() != null) {
            for (AcceptanceEventForm.Line line : form.getLines()) {
                if (line != null && !line.isRemoved()) {
                    lines.add(line);
                }
            }
        }
        if (lines.isEmpty()) {
            errors.add("lines", "请至少填写一条验收明细");
        }
        Set<Long> seenLines = new HashSet<>();
        List<PendingSlice> pending = new ArrayList<>();
        BigDecimal acceptedTotal = BigDecimal.ZERO;
        BigDecimal rejectedTotal = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            AcceptanceEventForm.Line line = lines.get(i);
            String prefix = "lines[" + i + "].";
            DeliveryLine deliveryLine = line.getDeliveryLineId() == null
                    ? null
                    : deliveryLines.findById(line.getDeliveryLineId()).orElse(null);
            if (deliveryLine == null || delivery == null || !delivery.id.equals(deliveryLine.deliveryEventId)) {
                errors.add(prefix + "deliveryLineId", "验收明细不属于这次交付");
                continue;
            }
            if (!seenLines.add(deliveryLine.id)) {
                errors.add(prefix + "deliveryLineId", "同一次验收不能重复同一交付明细");
                continue;
            }
            if (line.getReviewedQuantity() == null || line.getAcceptedQuantity() == null || line.getRejectedQuantity() == null) {
                errors.add(prefix + "reviewedQuantity", "请填写验收数量");
                continue;
            }
            if (line.getAcceptedQuantity().compareTo(BigDecimal.ZERO) < 0
                    || line.getRejectedQuantity().compareTo(BigDecimal.ZERO) < 0
                    || line.getReviewedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(prefix + "reviewedQuantity", "验收数量须大于 0");
                continue;
            }
            if (line.getReviewedQuantity().compareTo(line.getAcceptedQuantity().add(line.getRejectedQuantity())) != 0) {
                errors.add(prefix + "reviewedQuantity", "验收数量须等于通过数量加不通过数量");
                continue;
            }
            if (line.getRejectedQuantity().compareTo(BigDecimal.ZERO) > 0 && line.getRejectionReason() == null) {
                errors.add(prefix + "rejectionReason", "不通过时须填写原因");
            } else if (line.getRejectionReason() != null && line.getRejectionReason().length() > 500) {
                errors.add(prefix + "rejectionReason", "不通过原因过长");
            }
            pending.add(new PendingSlice(
                    deliveryLine,
                    form.getAcceptedAt(),
                    line.getReviewedQuantity(),
                    line.getAcceptedQuantity(),
                    line.getRejectedQuantity(),
                    prefix));
            acceptedTotal = acceptedTotal.add(line.getAcceptedQuantity());
            rejectedTotal = rejectedTotal.add(line.getRejectedQuantity());
        }
        errors.throwIfAny();
        Long revoked = revokeId;
        for (PendingSlice slice : pending) {
            Pool pool = settle(slice.line, List.of(slice), revoked);
            if (pool.error != null) {
                errors.add(slice.prefix + "acceptedQuantity", pool.error);
            }
        }
        errors.throwIfAny();
        assertCumulative(contractId, delivery, pending, revoked, errors);
        errors.throwIfAny();
        AcceptanceEvent event = new AcceptanceEvent();
        event.deliveryEventId = delivery.id;
        event.acceptorCompanyId = form.getAcceptorCompanyId();
        event.acceptorLegalPersonId = form.getAcceptorLegalPersonId();
        event.acceptorPersonId = form.getAcceptorPersonId();
        event.acceptedByEmployeeId = form.getAcceptedByEmployeeId();
        event.acceptedByPersonId = form.getAcceptedByPersonId();
        event.acceptanceNumber = form.getAcceptanceNumber();
        event.supersedesAcceptanceEventId = form.getSupersedesAcceptanceEventId();
        event.status = Codes.ACCEPTANCE_EFFECTIVE;
        event.acceptedAt = form.getAcceptedAt().truncatedTo(ChronoUnit.MINUTES);
        event.acceptanceResult = resultOf(acceptedTotal, rejectedTotal);
        event.acceptanceComments = form.getAcceptanceComments();
        event.sourceDocumentId = form.getSourceDocumentId();
        event = acceptanceEvents.save(event);
        for (int i = 0; i < lines.size(); i++) {
            AcceptanceEventForm.Line line = lines.get(i);
            DeliveryLine deliveryLine = deliveryLines.findById(line.getDeliveryLineId()).orElseThrow();
            AcceptanceLine stored = new AcceptanceLine();
            stored.acceptanceEventId = event.id;
            stored.deliveryLineId = deliveryLine.id;
            stored.reviewedQuantity = line.getReviewedQuantity();
            stored.acceptedQuantity = line.getAcceptedQuantity();
            stored.rejectedQuantity = line.getRejectedQuantity();
            stored.unit = deliveryLine.unit;
            stored.rejectionReason = line.getRejectionReason();
            acceptanceLines.save(stored);
        }
        if (revoked != null) {
            AcceptanceEvent prior = acceptanceEvents.findById(revoked).orElseThrow();
            prior.status = Codes.ACCEPTANCE_REVOKED;
            acceptanceEvents.save(prior);
        }
        return event.id;
    }

    @Transactional
    public void withdrawDelivery(Long contractId, Long deliveryId) {
        requireContract(contractId);
        DeliveryEvent event = deliveryEvents.findById(deliveryId).orElseThrow(NotFoundException::new);
        if (!contractOwnsDelivery(contractId, event)) {
            throw new NotFoundException();
        }
        if (acceptanceEvents.existsByDeliveryEventIdAndStatus(deliveryId, Codes.ACCEPTANCE_EFFECTIVE)) {
            throw new FieldErrorsException(Map.of("*", "已有有效验收，不能撤回"));
        }
        event.status = Codes.DELIVERY_WITHDRAWN;
        deliveryEvents.save(event);
    }

    private void assertCumulative(
            Long contractId,
            DeliveryEvent current,
            List<PendingSlice> pending,
            Long revoked,
            FieldErrors errors) {
        Map<Long, PendingSlice> pendingByLine = new HashMap<>();
        for (PendingSlice slice : pending) {
            pendingByLine.put(slice.line.id, slice);
        }
        Map<String, BigDecimal> accepted = new HashMap<>();
        Map<String, BigDecimal> delivered = new HashMap<>();
        Map<String, BigDecimal> planned = new HashMap<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            ContractStageTermVersion currentTerm = currentTerm(stage.id);
            if (currentTerm != null) {
                for (ContractStageDeliverable item : deliverables.findByStageTermVersionIdOrderByIdAsc(currentTerm.id)) {
                    planned.put(key(stage.id, item.deliverableCode), item.plannedQuantity);
                }
            }
            for (DeliveryEvent event : deliveryEvents.findByContractStageIdOrderBySubmittedAtAscIdAsc(stage.id)) {
                if (!Codes.DELIVERY_SUBMITTED.equals(event.status)) {
                    continue;
                }
                for (DeliveryLine line : deliveryLines.findByDeliveryEventIdOrderByIdAsc(event.id)) {
                    ContractStageDeliverable item = deliverables.findById(line.stageDeliverableId).orElse(null);
                    if (item == null) {
                        continue;
                    }
                    String codeKey = key(stage.id, item.deliverableCode);
                    planned.putIfAbsent(codeKey, item.plannedQuantity);
                    if (event.resubmissionOfDeliveryEventId == null) {
                        delivered.merge(codeKey, line.submittedQuantity, BigDecimal::add);
                    }
                    PendingSlice extra = event.id.equals(current.id) ? pendingByLine.get(line.id) : null;
                    Long revoke = event.id.equals(current.id) ? revoked : null;
                    Pool pool = settle(line, extra == null ? List.of() : List.of(extra), revoke);
                    accepted.merge(codeKey, pool.accepted, BigDecimal::add);
                }
            }
        }
        for (Map.Entry<String, BigDecimal> entry : accepted.entrySet()) {
            BigDecimal deliveredQuantity = delivered.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (entry.getValue().compareTo(deliveredQuantity) > 0) {
                errors.add("lines", "通过数量不能超过已交付数量");
                return;
            }
            BigDecimal plannedQuantity = planned.get(entry.getKey());
            if (plannedQuantity != null && entry.getValue().compareTo(plannedQuantity) > 0) {
                errors.add("lines", "累计通过量不得超过约定数量");
                return;
            }
        }
    }

    private Pool settle(DeliveryLine line, List<PendingSlice> pending, Long revokedEventId) {
        List<Slice> slices = new ArrayList<>();
        for (AcceptanceEvent event : acceptanceEvents.findByDeliveryEventIdOrderByAcceptedAtAscIdAsc(line.deliveryEventId)) {
            if (!Codes.ACCEPTANCE_EFFECTIVE.equals(event.status) || event.id.equals(revokedEventId)) {
                continue;
            }
            for (AcceptanceLine stored : acceptanceLines.findByAcceptanceEventIdOrderByIdAsc(event.id)) {
                if (line.id.equals(stored.deliveryLineId)) {
                    slices.add(new Slice(event.acceptedAt, event.id, stored.reviewedQuantity, stored.acceptedQuantity, stored.rejectedQuantity));
                }
            }
        }
        for (PendingSlice extra : pending) {
            if (line.id.equals(extra.line.id)) {
                slices.add(new Slice(extra.acceptedAt, null, extra.reviewed, extra.accepted, extra.rejected));
            }
        }
        slices.sort(Comparator.comparing((Slice slice) -> slice.acceptedAt == null ? LocalDateTime.MAX : slice.acceptedAt)
                .thenComparing(slice -> slice.id == null ? Long.MAX_VALUE : slice.id));
        Pool pool = new Pool(line.submittedQuantity);
        for (Slice slice : slices) {
            String error = pool.apply(slice.reviewed, slice.accepted, slice.rejected);
            if (error != null) {
                pool.error = error;
                return pool;
            }
        }
        return pool;
    }

    private Map<String, BigDecimal> outstandingByCode(DeliveryEvent ancestor) {
        Map<String, BigDecimal> totals = new HashMap<>();
        for (DeliveryLine line : deliveryLines.findByDeliveryEventIdOrderByIdAsc(ancestor.id)) {
            ContractStageDeliverable item = deliverables.findById(line.stageDeliverableId).orElse(null);
            if (item == null) {
                continue;
            }
            Pool pool = settle(line, List.of(), null);
            totals.merge(item.deliverableCode, pool.rejected, BigDecimal::add);
        }
        return totals;
    }

    private void validateBillings(Long contractId, List<BillingRow> rows, FieldErrors errors) {
        Set<Integer> numbers = new HashSet<>();
        Set<Long> pointed = new HashSet<>();
        for (BillingRow row : rows) {
            if (row.getSupersedesBillingScheduleId() != null) {
                pointed.add(row.getSupersedesBillingScheduleId());
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            BillingRow row = rows.get(i);
            String prefix = "billings[" + i + "].";
            ContractBillingSchedule stored = null;
            if (row.getId() != null) {
                stored = billings.findById(row.getId()).orElse(null);
                if (stored == null || !contractId.equals(stored.contractId)) {
                    errors.add(prefix + "id", "开票计划不属于该合同");
                    continue;
                }
            }
            if (row.getScheduleNumber() == null || row.getScheduleNumber() <= 0) {
                errors.add(prefix + "scheduleNumber", "请填写计划序号");
            } else if (!numbers.add(row.getScheduleNumber())) {
                errors.add(prefix + "scheduleNumber", "开票计划序号重复");
            }
            if (row.getBillingTrigger() == null || !Codes.BILLING_TRIGGERS.contains(row.getBillingTrigger())) {
                errors.add(prefix + "billingTrigger", "请选择开票触发");
            }
            requireAmount(row.getPlannedInvoiceGrossAmount(), errors, prefix + "plannedInvoiceGrossAmount");
            ContractTermVersion term = requireTerm(contractId, row.getContractTermVersionId(), errors, prefix + "contractTermVersionId");
            requireCurrency(row.getCurrency(), term, errors, prefix + "currency");
            requireStage(contractId, row.getContractStageId(), errors, prefix + "contractStageId");
            if (row.getBillingTrigger() != null && Codes.BILLING_TRIGGERS.contains(row.getBillingTrigger())) {
                checkTrigger(row.getBillingTrigger(), row.getContractStageId(), row.getDaysAfterTrigger(),
                        row.getPlannedInvoiceDate(), row.getTriggerDescription(), errors, prefix, "plannedInvoiceDate");
            }
            if (row.getTriggerDescription() != null && row.getTriggerDescription().length() > 500) {
                errors.add(prefix + "triggerDescription", "触发说明过长");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + "validFrom", "请填写开始日");
            } else {
                errors.add(prefix + "validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            }
            checkSupersedes(contractId, row.getId(), row.getSupersedesBillingScheduleId(), row.getValidFrom(), true, errors, prefix);
            if (stored != null && pointed.contains(stored.id) && billingChanged(stored, row)) {
                errors.add(prefix + "scheduleNumber", "已被替代的计划请新增一条并指向原计划");
            }
        }
        Set<Long> keptIds = new HashSet<>();
        for (BillingRow row : rows) {
            if (row.getId() != null) {
                keptIds.add(row.getId());
            }
        }
        for (ContractBillingSchedule existing : billings.findByContractIdOrderByScheduleNumberAsc(contractId)) {
            if (!keptIds.contains(existing.id) && pointed.contains(existing.id)) {
                errors.add("billings", "该计划已被后续计划替代，不能删除");
            }
        }
    }

    private void validatePayments(Long contractId, List<PaymentRow> rows, FieldErrors errors) {
        Set<Integer> numbers = new HashSet<>();
        Set<Long> pointed = new HashSet<>();
        for (PaymentRow row : rows) {
            if (row.getSupersedesPaymentScheduleId() != null) {
                pointed.add(row.getSupersedesPaymentScheduleId());
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            PaymentRow row = rows.get(i);
            String prefix = "payments[" + i + "].";
            ContractPaymentSchedule stored = null;
            if (row.getId() != null) {
                stored = payments.findById(row.getId()).orElse(null);
                if (stored == null || !contractId.equals(stored.contractId)) {
                    errors.add(prefix + "id", "收付款计划不属于该合同");
                    continue;
                }
            }
            if (row.getScheduleNumber() == null || row.getScheduleNumber() <= 0) {
                errors.add(prefix + "scheduleNumber", "请填写计划序号");
            } else if (!numbers.add(row.getScheduleNumber())) {
                errors.add(prefix + "scheduleNumber", "收付款计划序号重复");
            }
            if (row.getInstallmentName() == null) {
                errors.add(prefix + "installmentName", "请填写期次名称");
            } else if (row.getInstallmentName().length() > 100) {
                errors.add(prefix + "installmentName", "期次名称过长");
            }
            if (row.getPaymentPurpose() == null || !Codes.PAYMENT_PURPOSES.contains(row.getPaymentPurpose())) {
                errors.add(prefix + "paymentPurpose", "请选择款项用途");
            }
            if (row.getPaymentTrigger() == null || !Codes.PAYMENT_TRIGGERS.contains(row.getPaymentTrigger())) {
                errors.add(prefix + "paymentTrigger", "请选择收付触发");
            }
            requireAmount(row.getPlannedPaymentAmount(), errors, prefix + "plannedPaymentAmount");
            ContractTermVersion term = requireTerm(contractId, row.getContractTermVersionId(), errors, prefix + "contractTermVersionId");
            requireCurrency(row.getCurrency(), term, errors, prefix + "currency");
            requireStage(contractId, row.getContractStageId(), errors, prefix + "contractStageId");
            if (row.getPaymentTrigger() != null && Codes.PAYMENT_TRIGGERS.contains(row.getPaymentTrigger())) {
                checkTrigger(row.getPaymentTrigger(), row.getContractStageId(), row.getDaysAfterTrigger(),
                        row.getPlannedDueDate(), row.getTriggerDescription(), errors, prefix, "plannedDueDate");
            }
            if (row.getTriggerDescription() != null && row.getTriggerDescription().length() > 500) {
                errors.add(prefix + "triggerDescription", "触发说明过长");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + "validFrom", "请填写开始日");
            } else {
                errors.add(prefix + "validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            }
            checkSupersedes(contractId, row.getId(), row.getSupersedesPaymentScheduleId(), row.getValidFrom(), false, errors, prefix);
            if (stored != null && pointed.contains(stored.id) && paymentChanged(stored, row)) {
                errors.add(prefix + "scheduleNumber", "已被替代的计划请新增一条并指向原计划");
            }
        }
        Set<Long> keptIds = new HashSet<>();
        for (PaymentRow row : rows) {
            if (row.getId() != null) {
                keptIds.add(row.getId());
            }
        }
        for (ContractPaymentSchedule existing : payments.findByContractIdOrderByScheduleNumberAsc(contractId)) {
            if (!keptIds.contains(existing.id) && pointed.contains(existing.id)) {
                errors.add("payments", "该计划已被后续计划替代，不能删除");
            }
        }
    }

    private void checkSupersedes(
            Long contractId,
            Long selfId,
            Long targetId,
            LocalDate validFrom,
            boolean billing,
            FieldErrors errors,
            String prefix) {
        if (targetId == null) {
            return;
        }
        if (targetId.equals(selfId)) {
            errors.add(prefix + "supersedesBillingScheduleId", "计划不能替代自己");
            return;
        }
        if (billing) {
            ContractBillingSchedule target = billings.findById(targetId).orElse(null);
            if (target == null || !contractId.equals(target.contractId)) {
                errors.add(prefix + "supersedesBillingScheduleId", "被替代的开票计划不属于该合同");
                return;
            }
            if (validFrom != null && !validFrom.isAfter(target.validFrom)) {
                errors.add(prefix + "validFrom", "替代后的开始日须晚于原计划开始日");
            }
            if (billingCycle(selfId, targetId)) {
                errors.add(prefix + "supersedesBillingScheduleId", "替代关系不能成环");
            }
        } else {
            ContractPaymentSchedule target = payments.findById(targetId).orElse(null);
            if (target == null || !contractId.equals(target.contractId)) {
                errors.add(prefix + "supersedesPaymentScheduleId", "被替代的收付款计划不属于该合同");
                return;
            }
            if (validFrom != null && !validFrom.isAfter(target.validFrom)) {
                errors.add(prefix + "validFrom", "替代后的开始日须晚于原计划开始日");
            }
            if (paymentCycle(selfId, targetId)) {
                errors.add(prefix + "supersedesPaymentScheduleId", "替代关系不能成环");
            }
        }
    }

    private boolean billingCycle(Long selfId, Long targetId) {
        Set<Long> seen = new HashSet<>();
        Long cursor = targetId;
        while (cursor != null) {
            if (cursor.equals(selfId) || !seen.add(cursor)) {
                return true;
            }
            ContractBillingSchedule row = billings.findById(cursor).orElse(null);
            cursor = row == null ? null : row.supersedesBillingScheduleId;
        }
        return false;
    }

    private boolean paymentCycle(Long selfId, Long targetId) {
        Set<Long> seen = new HashSet<>();
        Long cursor = targetId;
        while (cursor != null) {
            if (cursor.equals(selfId) || !seen.add(cursor)) {
                return true;
            }
            ContractPaymentSchedule row = payments.findById(cursor).orElse(null);
            cursor = row == null ? null : row.supersedesPaymentScheduleId;
        }
        return false;
    }

    private void checkTrigger(
            String trigger,
            Long stageId,
            Integer days,
            LocalDate plannedDate,
            String description,
            FieldErrors errors,
            String prefix,
            String dateField) {
        if (Codes.TRIGGER_DATE.equals(trigger)) {
            if (plannedDate == null || days != null) {
                errors.add(prefix + dateField, "指定日期须填写计划日期，且不填触发后天数");
            }
            return;
        }
        if (plannedDate != null || days == null || stageId == null) {
            errors.add(prefix + "daysAfterTrigger", "相对触发须填写日历天数和关联阶段，计划日期留空");
        } else if (days < 0) {
            errors.add(prefix + "daysAfterTrigger", "日历天数不能为负");
        }
        if (Codes.TRIGGER_MANUAL.equals(trigger) && description == null) {
            errors.add(prefix + "triggerDescription", "人工确认须填写说明");
        }
    }

    private ContractTermVersion requireTerm(Long contractId, Long termId, FieldErrors errors, String field) {
        if (termId == null) {
            errors.add(field, "请选择条款版本");
            return null;
        }
        ContractTermVersion term = terms.findById(termId).orElse(null);
        if (term == null || !contractId.equals(term.contractId)) {
            errors.add(field, "条款版本不属于该合同");
            return null;
        }
        return term;
    }

    private void requireStage(Long contractId, Long stageId, FieldErrors errors, String field) {
        if (stageId == null) {
            return;
        }
        ContractStage stage = stages.findById(stageId).orElse(null);
        if (stage == null || !contractId.equals(stage.contractId)) {
            errors.add(field, "阶段不属于该合同");
        }
    }

    private void requireCurrency(String currency, ContractTermVersion term, FieldErrors errors, String field) {
        if (currency == null) {
            errors.add(field, "请填写币种");
            return;
        }
        if (currency.length() > 8) {
            errors.add(field, "币种过长");
            return;
        }
        if (term != null && !currency.equals(term.currency)) {
            errors.add(field, "币种须与所引用条款版本一致");
        }
    }

    private void requireAmount(BigDecimal amount, FieldErrors errors, String field) {
        if (amount == null) {
            errors.add(field, "请填写计划金额");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(field, "计划金额须大于 0");
        }
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

    private boolean billingChanged(ContractBillingSchedule stored, BillingRow row) {
        return !Cmp.eq(stored.contractTermVersionId, row.getContractTermVersionId())
                || !Cmp.eq(stored.contractStageId, row.getContractStageId())
                || !Cmp.eq(stored.scheduleNumber, row.getScheduleNumber())
                || !Cmp.eq(stored.billingTrigger, row.getBillingTrigger())
                || !Cmp.eq(stored.plannedInvoiceGrossAmount, row.getPlannedInvoiceGrossAmount())
                || !Cmp.eq(stored.currency, row.getCurrency())
                || !Cmp.eq(stored.plannedInvoiceDate, row.getPlannedInvoiceDate())
                || !Cmp.eq(stored.daysAfterTrigger, row.getDaysAfterTrigger())
                || !Cmp.eq(stored.triggerDescription, row.getTriggerDescription())
                || !Cmp.eq(stored.validFrom, row.getValidFrom())
                || !Cmp.eq(stored.validTo, row.getValidTo())
                || !Cmp.eq(stored.supersedesBillingScheduleId, row.getSupersedesBillingScheduleId());
    }

    private boolean paymentChanged(ContractPaymentSchedule stored, PaymentRow row) {
        return !Cmp.eq(stored.contractTermVersionId, row.getContractTermVersionId())
                || !Cmp.eq(stored.contractStageId, row.getContractStageId())
                || !Cmp.eq(stored.scheduleNumber, row.getScheduleNumber())
                || !Cmp.eq(stored.installmentName, row.getInstallmentName())
                || !Cmp.eq(stored.paymentPurpose, row.getPaymentPurpose())
                || !Cmp.eq(stored.paymentTrigger, row.getPaymentTrigger())
                || !Cmp.eq(stored.plannedPaymentAmount, row.getPlannedPaymentAmount())
                || !Cmp.eq(stored.currency, row.getCurrency())
                || !Cmp.eq(stored.plannedDueDate, row.getPlannedDueDate())
                || !Cmp.eq(stored.daysAfterTrigger, row.getDaysAfterTrigger())
                || !Cmp.eq(stored.triggerDescription, row.getTriggerDescription())
                || !Cmp.eq(stored.validFrom, row.getValidFrom())
                || !Cmp.eq(stored.validTo, row.getValidTo())
                || !Cmp.eq(stored.supersedesPaymentScheduleId, row.getSupersedesPaymentScheduleId());
    }

    private void applyBilling(ContractBillingSchedule schedule, Long contractId, BillingRow row) {
        schedule.contractId = contractId;
        schedule.contractTermVersionId = row.getContractTermVersionId();
        schedule.contractStageId = row.getContractStageId();
        schedule.scheduleNumber = row.getScheduleNumber();
        schedule.billingTrigger = row.getBillingTrigger();
        schedule.plannedInvoiceGrossAmount = row.getPlannedInvoiceGrossAmount();
        schedule.currency = row.getCurrency();
        schedule.plannedInvoiceDate = row.getPlannedInvoiceDate();
        schedule.daysAfterTrigger = row.getDaysAfterTrigger();
        schedule.triggerDescription = row.getTriggerDescription();
        schedule.validFrom = row.getValidFrom();
        schedule.validTo = row.getValidTo();
        schedule.supersedesBillingScheduleId = row.getSupersedesBillingScheduleId();
    }

    private void applyPayment(ContractPaymentSchedule schedule, Long contractId, PaymentRow row) {
        schedule.contractId = contractId;
        schedule.contractTermVersionId = row.getContractTermVersionId();
        schedule.contractStageId = row.getContractStageId();
        schedule.scheduleNumber = row.getScheduleNumber();
        schedule.installmentName = row.getInstallmentName();
        schedule.paymentPurpose = row.getPaymentPurpose();
        schedule.paymentTrigger = row.getPaymentTrigger();
        schedule.plannedPaymentAmount = row.getPlannedPaymentAmount();
        schedule.currency = row.getCurrency();
        schedule.plannedDueDate = row.getPlannedDueDate();
        schedule.daysAfterTrigger = row.getDaysAfterTrigger();
        schedule.triggerDescription = row.getTriggerDescription();
        schedule.validFrom = row.getValidFrom();
        schedule.validTo = row.getValidTo();
        schedule.supersedesPaymentScheduleId = row.getSupersedesPaymentScheduleId();
    }

    private void normalizePlans(ContractPlanForm form) {
        for (BillingRow row : form.getBillings()) {
            if (row == null) {
                continue;
            }
            row.setBillingTrigger(Text.blankToNull(row.getBillingTrigger()));
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            row.setTriggerDescription(Text.blankToNull(row.getTriggerDescription()));
        }
        for (PaymentRow row : form.getPayments()) {
            if (row == null) {
                continue;
            }
            row.setInstallmentName(Text.blankToNull(row.getInstallmentName()));
            row.setPaymentPurpose(Text.blankToNull(row.getPaymentPurpose()));
            row.setPaymentTrigger(Text.blankToNull(row.getPaymentTrigger()));
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            row.setTriggerDescription(Text.blankToNull(row.getTriggerDescription()));
        }
    }

    private void normalizeDelivery(DeliveryEventForm form) {
        form.setDeliveryNumber(Text.blankToNull(form.getDeliveryNumber()));
        form.setDeliverableDescription(Text.blankToNull(form.getDeliverableDescription()));
        if (form.getLines() == null) {
            form.setLines(new ArrayList<>());
        }
        for (DeliveryEventForm.Line line : form.getLines()) {
            if (line == null) {
                continue;
            }
            line.setUnit(Text.blankToNull(line.getUnit()));
            line.setDeliveredArtifactVersion(Text.blankToNull(line.getDeliveredArtifactVersion()));
        }
    }

    private void normalizeAcceptance(AcceptanceEventForm form) {
        form.setAcceptanceNumber(Text.blankToNull(form.getAcceptanceNumber()));
        form.setAcceptanceComments(Text.blankToNull(form.getAcceptanceComments()));
        if (form.getLines() == null) {
            form.setLines(new ArrayList<>());
        }
        for (AcceptanceEventForm.Line line : form.getLines()) {
            if (line == null) {
                continue;
            }
            line.setRejectionReason(Text.blankToNull(line.getRejectionReason()));
        }
    }

    private List<BillingRow> keptBillings(ContractPlanForm form) {
        List<BillingRow> rows = new ArrayList<>();
        for (BillingRow row : form.getBillings()) {
            if (row != null && !row.isRemoved()) {
                rows.add(row);
            }
        }
        return rows;
    }

    private List<PaymentRow> keptPayments(ContractPlanForm form) {
        List<PaymentRow> rows = new ArrayList<>();
        for (PaymentRow row : form.getPayments()) {
            if (row != null && !row.isRemoved()) {
                rows.add(row);
            }
        }
        return rows;
    }

    private boolean deliveryNumberTaken(Long contractId, String number) {
        for (DeliveryEvent event : eventsOf(contractId)) {
            if (number.equals(event.deliveryNumber)) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptanceNumberTaken(Long contractId, String number) {
        for (DeliveryEvent event : eventsOf(contractId)) {
            for (AcceptanceEvent acceptance : acceptanceEvents.findByDeliveryEventIdOrderByAcceptedAtAscIdAsc(event.id)) {
                if (number.equals(acceptance.acceptanceNumber)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<DeliveryEvent> eventsOf(Long contractId) {
        List<DeliveryEvent> events = new ArrayList<>();
        for (ContractStage stage : stages.findByContractIdOrderBySequenceNoAsc(contractId)) {
            events.addAll(deliveryEvents.findByContractStageIdOrderBySubmittedAtAscIdAsc(stage.id));
        }
        return events;
    }

    private boolean contractOwnsDelivery(Long contractId, DeliveryEvent event) {
        ContractStage stage = stages.findById(event.contractStageId).orElse(null);
        return stage != null && contractId.equals(stage.contractId);
    }

    private ContractStageTermVersion currentTerm(Long stageId) {
        ContractStageTermVersion open = null;
        ContractStageTermVersion latest = null;
        for (ContractStageTermVersion term : stageTerms.findByContractStageIdOrderByVersionNumberAsc(stageId)) {
            latest = term;
            if (term.validTo == null) {
                open = term;
            }
        }
        return open != null ? open : latest;
    }

    private void requireContract(Long contractId) {
        if (contracts.findById(contractId).isEmpty()) {
            throw new NotFoundException();
        }
    }

    private static String resultOf(BigDecimal accepted, BigDecimal rejected) {
        if (accepted.signum() > 0 && rejected.signum() == 0) {
            return "通过";
        }
        if (accepted.signum() == 0 && rejected.signum() > 0) {
            return "不通过";
        }
        return "部分通过";
    }

    private static String key(Long stageId, String code) {
        return stageId + "\n" + code;
    }

    static BigDecimal plain(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.stripTrailingZeros().toPlainString());
    }

    private static final class MutableProgress {
        private Long stageId;
        private String stageCode;
        private String deliverableCode;
        private String deliverableName;
        private String unit;
        private BigDecimal planned = BigDecimal.ZERO;
        private BigDecimal delivered = BigDecimal.ZERO;
        private BigDecimal accepted = BigDecimal.ZERO;
        private BigDecimal rejected = BigDecimal.ZERO;
    }

    private static final class Pool {
        private final BigDecimal submitted;
        private BigDecimal accepted = BigDecimal.ZERO;
        private BigDecimal rejected = BigDecimal.ZERO;
        private BigDecimal unreviewed;
        private String error;

        private Pool(BigDecimal submitted) {
            this.submitted = submitted;
            this.unreviewed = submitted;
        }

        private String apply(BigDecimal reviewed, BigDecimal acceptedNow, BigDecimal rejectedNow) {
            BigDecimal room = submitted.subtract(accepted);
            if (acceptedNow.compareTo(room) > 0) {
                return "通过数量不能超过已交付数量";
            }
            BigDecimal capacity = rejected.add(unreviewed);
            if (reviewed.compareTo(capacity) > 0) {
                return "不能重复确认已通过数量";
            }
            BigDecimal fromRejected = reviewed.min(rejected);
            BigDecimal fromUnreviewed = reviewed.subtract(fromRejected);
            rejected = rejected.subtract(fromRejected).add(rejectedNow);
            unreviewed = unreviewed.subtract(fromUnreviewed);
            accepted = accepted.add(acceptedNow);
            return null;
        }
    }

    private record Slice(LocalDateTime acceptedAt, Long id, BigDecimal reviewed, BigDecimal accepted, BigDecimal rejected) {
    }

    private record PendingSlice(
            DeliveryLine line,
            LocalDateTime acceptedAt,
            BigDecimal reviewed,
            BigDecimal accepted,
            BigDecimal rejected,
            String prefix) {
    }

    private record ResolvedLine(DeliveryEventForm.Line form, ContractStageDeliverable item) {
    }
}
