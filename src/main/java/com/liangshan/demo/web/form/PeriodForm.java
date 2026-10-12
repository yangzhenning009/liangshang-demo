package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PeriodForm {

    private Long id;
    private Long ledgerId;
    private Integer fiscalYear;
    private Integer periodNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodStartDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodEndDate;
    private String periodStatus = "开放";
}
