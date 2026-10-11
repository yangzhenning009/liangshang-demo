package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SupplierForm {

    private Long id;
    private String supplierCode;
    private String supplierName;
    private Long purchasingCompanyId;
    private Long companyId;
    private Long legalPersonId;
    private Long personId;
    private Long procurementOwnerEmployeeId;
    private String admissionStatus = "待提交";
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime admissionSubmittedAt;
    private Long admissionReviewedByEmployeeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime admissionReviewedAt;
    private String admissionRejectionReason;
    private Long admissionDocumentId;
    private String cooperationStatus = "未启用";
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cooperationStartedOn;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cooperationEndedOn;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime cooperationStatusEffectiveAt;
    private String cooperationStatusReason;
    private String defaultSettlementCurrency;
    private String defaultPaymentDueBasis;
    private Integer defaultPaymentTermDays;
    private Integer defaultPaymentDayOfMonth;
    private Long changedByEmployeeId;
    private String changeReason;
    private Long basisDocumentId;
}
