package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReceivableForm {

    private Long id;
    private String receivableNumber;
    private Long creditorCompanyId;
    private Long creditorLegalPersonId;
    private Long debtorCompanyId;
    private Long debtorLegalPersonId;
    private Long debtorPersonId;
    private Long contractId;
    private Long contractPaymentScheduleId;
    /** 只用于按开票计划推算到期日，不落库。 */
    private Long billingScheduleId;
    private Long projectId;
    private Long contractStageId;
    private String receivablePurpose = "阶段款";
    private BigDecimal receivableAmount;
    private String currency;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime confirmedAt;
    private Long confirmedByEmployeeId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;
    private Long triggerAcceptanceEventId;
    private Long triggerInvoiceId;
    private Long sourceDocumentId;
    private String status = "草稿";
    private List<AdjustmentRow> adjustments = new ArrayList<>();

    @Getter
    @Setter
    public static class AdjustmentRow {
        private Long id;
        private boolean removed;
        private BigDecimal increaseAmount;
        private BigDecimal decreaseAmount;
        private String currency;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime adjustedAt;
        private String adjustmentReason;
        private Long sourceDocumentId;
        private Long approvedByEmployeeId;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime approvedAt;
        private String status = "草稿";
    }
}
