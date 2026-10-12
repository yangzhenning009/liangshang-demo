package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class LaborCostRateForm {

    private Long id;
    private Long employmentId;
    private String costBasis = "标准成本";
    private BigDecimal hourlyCostAmount;
    private String currency;
    private Integer rateVersionNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validTo;
    private String approvalStatus = "草稿";
    private Long approvedByEmployeeId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime approvedAt;
    private Long basisDocumentId;
}
