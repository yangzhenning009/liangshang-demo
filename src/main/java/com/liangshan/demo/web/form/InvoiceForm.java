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
public class InvoiceForm {

    private Long id;
    private String invoiceNumber;
    private String invoiceCode;
    private String invoiceType = "增值税普通发票";
    private String invoiceMedium = "电子";
    private String invoiceColor = "蓝字";
    private BigDecimal invoiceNetAmount;
    private BigDecimal invoiceTaxAmount;
    private BigDecimal invoiceGrossAmount;
    private Long originalInvoiceId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime invoiceVerifiedAt;
    private Long verifiedByEmployeeId;
    private String verificationResult;
    private String verificationComment;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime voidedAt;
    private String voidReason;
    private Long issuerCompanyId;
    private Long issuerLegalPersonId;
    private Long recipientCompanyId;
    private Long recipientLegalPersonId;
    private Long recipientPersonId;
    private String issuerNameSnapshot;
    private String issuerTaxpayerIdSnapshot;
    private String recipientNameSnapshot;
    private String recipientTaxpayerIdSnapshot;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedAt;
    private String currency;
    private String status = "草稿";
    private Long sourceDocumentId;
    private List<LineRow> lines = new ArrayList<>();
    private List<AllocationRow> allocations = new ArrayList<>();

    @Getter
    @Setter
    public static class LineRow {
        private Long id;
        private boolean removed;
        private Integer lineNumber;
        private String itemName;
        private String itemSpecification;
        private String taxClassificationCode;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitNetPrice;
        private BigDecimal taxRate;
        private String taxTreatment = "适用税率";
        private BigDecimal netAmount;
        private BigDecimal taxAmount;
        private BigDecimal grossAmount;
        private Long originalInvoiceLineId;
    }

    @Getter
    @Setter
    public static class AllocationRow {
        private Long id;
        private boolean removed;
        private Integer lineNumber;
        private Long contractId;
        private Long projectId;
        private Long contractStageId;
        private Long billingScheduleId;
        private Long receivableItemId;
        private Long payableItemId;
        private BigDecimal allocatedAmount;
        private String currency;
        private String allocationBasis = "含税";
    }
}
