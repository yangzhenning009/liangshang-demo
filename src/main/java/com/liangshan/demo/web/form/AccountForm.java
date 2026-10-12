package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class AccountForm {

    private Long id;
    private Long ledgerId;
    private String accountCode;
    private String accountName;
    private Long parentAccountId;
    private String accountCategory = "资产";
    private String normalBalanceDirection = "借";
    private boolean postable = true;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validTo;
    private String status = "启用";
}
