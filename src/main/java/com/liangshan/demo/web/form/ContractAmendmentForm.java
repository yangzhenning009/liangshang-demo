package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ContractAmendmentForm {

    private String amendmentNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate signedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate effectiveAt;
    private Long sourceDocumentId;
    private String summary;
    private ContractForm terms = new ContractForm();
}
