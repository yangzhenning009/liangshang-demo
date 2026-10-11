package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ContractPlanForm {

    private List<BillingRow> billings = new ArrayList<>();
    private List<PaymentRow> payments = new ArrayList<>();

    @Getter
    @Setter
    public static class BillingRow {
        private Long id;
        private Long contractTermVersionId;
        private Long contractStageId;
        private Integer scheduleNumber;
        private String billingTrigger;
        private BigDecimal plannedInvoiceGrossAmount;
        private String currency;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate plannedInvoiceDate;
        private Integer daysAfterTrigger;
        private String triggerDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long supersedesBillingScheduleId;
        private boolean removed;
    }

    @Getter
    @Setter
    public static class PaymentRow {
        private Long id;
        private Long contractTermVersionId;
        private Long contractStageId;
        private Integer scheduleNumber;
        private String installmentName;
        private String paymentPurpose;
        private String paymentTrigger;
        private BigDecimal plannedPaymentAmount;
        private String currency;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate plannedDueDate;
        private Integer daysAfterTrigger;
        private String triggerDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long supersedesPaymentScheduleId;
        private boolean removed;
    }
}
