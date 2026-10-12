package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RevenueForm {

    private Long id;
    private Long ledgerId;
    private Long contractId;
    private Long contractTermVersionId;
    private Long projectId;
    private Long contractStageId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recognitionDate;
    private Long accountingPeriodId;
    private BigDecimal recognizedRevenueAmount;
    private String currency;
    private String revenuePolicyCode;
    private String revenuePolicyVersion;
    private Long acceptanceEventId;
    private Long sourceDocumentId;
    private Long approvedByEmployeeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime approvedAt;
    private String status = "草稿";
    private Long reversalOfRecognitionId;
}
