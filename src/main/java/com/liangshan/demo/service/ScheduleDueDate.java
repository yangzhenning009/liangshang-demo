package com.liangshan.demo.service;

import com.liangshan.demo.domain.AcceptanceEvent;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.ContractBillingSchedule;
import com.liangshan.demo.domain.ContractPaymentSchedule;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.DeliveryEvent;
import com.liangshan.demo.domain.Invoice;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractPaymentScheduleRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.DeliveryEventRepository;
import com.liangshan.demo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 相对触发的开票计划或收付款计划，在确认应收或应付时算出实际到期日。
 * 人工确认不解析说明文字。指定日期直接采用计划日期。
 */
@Service
@RequiredArgsConstructor
public class ScheduleDueDate {

    private final ContractPaymentScheduleRepository paymentSchedules;
    private final ContractBillingScheduleRepository billingSchedules;
    private final AcceptanceEventRepository acceptances;
    private final DeliveryEventRepository deliveries;
    private final InvoiceRepository invoices;
    private final ContractTermVersionRepository terms;

    public LocalDate fromPaymentSchedule(Long scheduleId, Long acceptanceEventId, Long invoiceId, FieldErrors errors) {
        ContractPaymentSchedule schedule = paymentSchedules.findById(scheduleId).orElse(null);
        if (schedule == null) {
            errors.add("contractPaymentScheduleId", "收付款计划不存在");
            return null;
        }
        return compute(
                schedule.paymentTrigger,
                schedule.daysAfterTrigger,
                schedule.plannedDueDate,
                schedule.contractStageId,
                schedule.contractTermVersionId,
                acceptanceEventId,
                invoiceId,
                errors);
    }

    public LocalDate fromBillingSchedule(Long scheduleId, Long acceptanceEventId, FieldErrors errors) {
        ContractBillingSchedule schedule = billingSchedules.findById(scheduleId).orElse(null);
        if (schedule == null) {
            errors.add("billingScheduleId", "开票计划不存在");
            return null;
        }
        return compute(
                schedule.billingTrigger,
                schedule.daysAfterTrigger,
                schedule.plannedInvoiceDate,
                schedule.contractStageId,
                schedule.contractTermVersionId,
                acceptanceEventId,
                null,
                errors);
    }

    private LocalDate compute(
            String trigger,
            Integer days,
            LocalDate planned,
            Long stageId,
            Long termId,
            Long acceptanceEventId,
            Long invoiceId,
            FieldErrors errors) {
        if (Codes.TRIGGER_MANUAL.equals(trigger)) {
            return null;
        }
        if (Codes.TRIGGER_DATE.equals(trigger)) {
            if (planned == null) {
                errors.add("dueDate", "指定日期的计划没有计划日期");
            }
            return planned;
        }
        if (days == null || days < 0) {
            errors.add("dueDate", "相对触发须有非负的日历天数");
            return null;
        }
        LocalDate eventDate = null;
        if ("阶段验收".equals(trigger)) {
            eventDate = acceptanceDate(acceptanceEventId, stageId, errors);
        } else if ("签约".equals(trigger)) {
            eventDate = signedDate(termId, errors);
        } else if ("开票".equals(trigger)) {
            eventDate = invoiceDate(invoiceId, errors);
        } else {
            errors.add("dueDate", "无法识别的触发方式");
        }
        if (eventDate == null) {
            return null;
        }
        return eventDate.plusDays(days);
    }

    private LocalDate acceptanceDate(Long acceptanceEventId, Long stageId, FieldErrors errors) {
        if (acceptanceEventId == null) {
            errors.add("triggerAcceptanceEventId", "阶段验收触发须选择验收");
            return null;
        }
        AcceptanceEvent acceptance = acceptances.findById(acceptanceEventId).orElse(null);
        if (acceptance == null || !Codes.ACCEPTANCE_EFFECTIVE.equals(acceptance.status)) {
            errors.add("triggerAcceptanceEventId", "验收不存在或已撤销");
            return null;
        }
        DeliveryEvent delivery = deliveries.findById(acceptance.deliveryEventId).orElse(null);
        if (delivery == null || stageId == null || !stageId.equals(delivery.contractStageId)) {
            errors.add("triggerAcceptanceEventId", "验收不属于该计划的阶段");
            return null;
        }
        if (acceptance.acceptedAt == null) {
            errors.add("triggerAcceptanceEventId", "验收没有日期");
            return null;
        }
        return acceptance.acceptedAt.toLocalDate();
    }

    private LocalDate signedDate(Long termId, FieldErrors errors) {
        ContractTermVersion term = termId == null ? null : terms.findById(termId).orElse(null);
        if (term == null) {
            errors.add("dueDate", "签约触发找不到条款版本");
            return null;
        }
        if (term.signedAt != null) {
            return term.signedAt;
        }
        errors.add("dueDate", "条款版本没有签约日");
        return null;
    }

    private LocalDate invoiceDate(Long invoiceId, FieldErrors errors) {
        if (invoiceId == null) {
            errors.add("triggerInvoiceId", "开票触发须选择发票");
            return null;
        }
        Invoice invoice = invoices.findById(invoiceId).orElse(null);
        if (invoice == null || invoice.issuedAt == null) {
            errors.add("triggerInvoiceId", "发票不存在或没有开票日");
            return null;
        }
        return invoice.issuedAt;
    }
}
