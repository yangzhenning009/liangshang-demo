package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LegalPersonForm {

    private Long id;
    private String legalName;
    private String legalPersonType;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate establishedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate terminatedAt;
    private String status = "存续";
    private List<RegistrationForm> registrations = new ArrayList<>();
    private List<RepresentativeForm> representativeTerms = new ArrayList<>();

    @Getter
    @Setter
    public static class RegistrationForm {
        private Long id;
        private boolean removed;
        private String registrationType;
        private String unifiedSocialCreditCode;
        private String registrationCertificateNumber;
        private String taxpayerIdentificationNumber;
        private String registrationAuthority;
        private String jurisdiction;
        private String registeredName;
        private String registeredAddress;
        private String registrationStatus;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate registeredAt;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long sourceDocumentId;
    }

    @Getter
    @Setter
    public static class RepresentativeForm {
        private Long id;
        private boolean removed;
        private Long personId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }
}
