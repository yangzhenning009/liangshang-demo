package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompanyForm {

    private Long id;
    private String companyCode;
    private String name;
    private String companyType = "company";
    private Long legalPersonId;
    private Long headCompanyId;
    private String companyForm;
    private String contactPhone;
    private String contactEmail;
    private String businessAddress;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate establishedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate terminatedAt;
    private String status = "存续";

    private List<RegistrationForm> registrations = new ArrayList<>();
    private List<BankAccountForm> bankAccounts = new ArrayList<>();
    private List<ShareholdingForm> shareholdings = new ArrayList<>();
    private List<VersionForm> versions = new ArrayList<>();

    @Getter
    @Setter
    public static class RegistrationForm {
        private Long id;
        private boolean removed;
        private Integer versionNumber;
        private String registrationType;
        private String unifiedSocialCreditCode;
        private String businessLicenseRegistrationNumber;
        private String taxpayerIdentificationNumber;
        private String registrationAuthority;
        private String registrationJurisdiction;
        private String registeredName;
        private String registeredAddress;
        private String registrationStatus;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate registeredAt;
        private String businessScope;
        private BigDecimal registeredCapitalAmount;
        private String registeredCapitalCurrency;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate businessTermStartDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate businessTermEndDate;
        private Boolean businessTermIndefinite;
        private Long branchManagerPersonId;
        private Long businessLicenseDocumentId;
        private Long taxInformationSourceDocumentId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long sourceDocumentId;
    }

    @Getter
    @Setter
    public static class BankAccountForm {
        private Long id;
        private boolean removed;
        private String bankAccountName;
        private String bankAccountNumber;
        private String bankName;
        private String bankBranchName;
        private String bankRoutingNumber;
        private String accountCurrency;
        private boolean receiptAccount;
        private boolean paymentAccount;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private String status;
    }

    @Getter
    @Setter
    public static class ShareholdingForm {
        private Long id;
        private boolean removed;
        private Long shareholderCompanyId;
        private Long shareholderLegalPersonId;
        private Long shareholderPersonId;
        private String shareClass;
        private BigDecimal ownershipRatio;
        private BigDecimal subscribedCapitalAmount;
        private String currency;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }

    @Getter
    @Setter
    public static class VersionForm {
        private Long id;
        private boolean removed;
        private Integer versionNumber;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }
}
