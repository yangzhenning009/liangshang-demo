package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ContractForm {

    private Long id;
    private String contractNumber;
    private String contractType = "销售";
    private String contractTitle;
    private Long managingCompanyId;
    private Long procurementCompanyId;
    private Long supplierId;
    private Long contractOwnerEmployeeId;
    private Long buyerSignatoryPersonId;
    private Long sellerSignatoryPersonId;
    private String buyerNameSnapshot;
    private String sellerNameSnapshot;
    private String buyerTaxpayerIdSnapshot;
    private String sellerTaxpayerIdSnapshot;
    private Long buyerContactPersonId;
    private Long sellerContactPersonId;
    private Long buyerCompanyId;
    private Long buyerLegalPersonId;
    private Long buyerPersonId;
    private Long sellerCompanyId;
    private Long sellerLegalPersonId;
    private Long sellerPersonId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate signedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validTo;
    private String currency;
    private String pricingMethod = "固定总价";
    private BigDecimal contractNetAmount;
    private BigDecimal contractTaxAmount;
    private BigDecimal contractGrossAmount;
    private String approvalStatus = "草稿";
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime contractApprovedAt;
    private Long contractApprovedByEmployeeId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate terminationDate;
    private String terminationReason;
    private String status = "未生效";
    private Long sourceDocumentId;
}
