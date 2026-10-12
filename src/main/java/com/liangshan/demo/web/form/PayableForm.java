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
public class PayableForm {

    private Long id;
    private String payableNumber;
    private Long debtorCompanyId;
    private Long debtorLegalPersonId;
    private Long creditorCompanyId;
    private Long creditorLegalPersonId;
    private Long creditorPersonId;
    private Long supplierId;
    private Long contractId;
    private Long contractPaymentScheduleId;
    /** 只用于按开票计划推算到期日，不落库。 */
    private Long billingScheduleId;
    private Long projectId;
    private Long contractStageId;
    private Long costOccurrenceId;
    private Long expenseClaimId;
    private String payablePurpose = "采购款";
    private BigDecimal payableAmount;
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
    private List<ReceivableForm.AdjustmentRow> adjustments = new ArrayList<>();
}
