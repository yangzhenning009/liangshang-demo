package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SupplierDetailForm {

    private List<ContactForm> contacts = new ArrayList<>();
    private List<CategoryForm> categories = new ArrayList<>();
    private List<QualificationForm> qualifications = new ArrayList<>();

    @Getter
    @Setter
    public static class ContactForm {
        private Long id;
        private boolean removed;
        private Long personId;
        private String contactRole = "商务";
        private String businessPhone;
        private String businessEmail;
        private Boolean primary;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private String status = "有效";
    }

    @Getter
    @Setter
    public static class CategoryForm {
        private Long id;
        private boolean removed;
        private String supplyCategoryCode;
        private String supplyCategoryName;
        private String supplyScopeDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private String status = "有效";
    }

    @Getter
    @Setter
    public static class QualificationForm {
        private Long id;
        private boolean removed;
        private String qualificationName;
        private String certificateNumber;
        private String issuingAuthority;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate issuedOn;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate expiresOn;
        private Boolean noExpiry = Boolean.FALSE;
        private Long qualificationDocumentId;
        private String reviewStatus = "待审核";
        private Long reviewedByEmployeeId;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime reviewedAt;
        private String rejectionReason;
        private String status = "有效";
    }
}
