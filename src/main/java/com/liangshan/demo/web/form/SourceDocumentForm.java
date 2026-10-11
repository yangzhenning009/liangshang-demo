package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SourceDocumentForm {

    private Long id;
    private String documentType;
    private String documentNumber;
    private Long issuerCompanyId;
    private Long issuerLegalPersonId;
    private Long issuerPersonId;
    private Long preparedByEmployeeId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedAt;
    private String fileStorageUri;
    private String fileName;
    private String mimeType;
    private String fileChecksum;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime uploadedAt;
    private Long uploadedByEmployeeId;
    private String contentSummary;
    private String status = "有效";
}
