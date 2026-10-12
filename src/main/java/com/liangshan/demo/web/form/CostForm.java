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
public class CostForm {

    private Long id;
    private Long bearingCompanyId;
    private Long bearingLegalPersonId;
    private Long incurredByEmployeeId;
    private Long supplierCompanyId;
    private Long supplierLegalPersonId;
    private Long supplierPersonId;
    private Long supplierId;
    private String costNumber;
    private Long purchaseContractId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseStartDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseEndDate;
    private BigDecimal expenseNetAmount;
    private BigDecimal expenseTaxAmount;
    private BigDecimal expenseGrossAmount;
    private BigDecimal employeeAdvancedAmount;
    private String costPolicyCode;
    private String costPolicyVersion;
    private String costType;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime incurredAt;
    private BigDecimal costAmount;
    private String currency;
    private String costBasis = "实际费用";
    private String description;
    private String approvalStatus = "草稿";
    private Long approvedByEmployeeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime approvedAt;
    private String status = "有效";
    private Long sourceDocumentId;
    private List<AllocationRow> allocations = new ArrayList<>();
    private List<InvoiceRow> invoiceLines = new ArrayList<>();

    @Getter
    @Setter
    public static class AllocationRow {
        private Long id;
        private boolean removed;
        private Long projectId;
        private Long contractStageId;
        private BigDecimal allocatedAmount;
        private String currency;
        private String allocationPeriod;
        private String allocationMethod = "直接指定";
        private BigDecimal allocationBasisHours;
        private BigDecimal allocationRatio;
        private Long allocatedByEmployeeId;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime allocatedAt;
        private Long basisDocumentId;
        private String status = "有效";
    }

    @Getter
    @Setter
    public static class InvoiceRow {
        private Long id;
        private boolean removed;
        private Long invoiceLineId;
        private BigDecimal matchedExpenseGrossAmount;
        private String currency;
    }
}
