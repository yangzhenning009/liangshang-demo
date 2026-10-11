package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.CompanyBankAccount;
import com.liangshan.demo.domain.CompanyRegistration;
import com.liangshan.demo.domain.CompanyShareholding;
import com.liangshan.demo.domain.CompanyVersion;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.repository.CompanyBankAccountRepository;
import com.liangshan.demo.repository.CompanyRegistrationRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CompanyShareholdingRepository;
import com.liangshan.demo.repository.CompanyVersionRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.DepartmentServiceCompanyRepository;
import com.liangshan.demo.repository.DepartmentVersionRepository;
import com.liangshan.demo.repository.LegalPersonRegistrationRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.CompanyForm;
import com.liangshan.demo.web.form.CompanyForm.BankAccountForm;
import com.liangshan.demo.web.form.CompanyForm.RegistrationForm;
import com.liangshan.demo.web.form.CompanyForm.ShareholdingForm;
import com.liangshan.demo.web.form.CompanyForm.VersionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companies;
    private final CompanyRegistrationRepository registrations;
    private final CompanyBankAccountRepository bankAccounts;
    private final CompanyShareholdingRepository shareholdings;
    private final CompanyVersionRepository versions;
    private final LegalPersonRepository legalPersons;
    private final LegalPersonRegistrationRepository legalPersonRegistrations;
    private final PersonRepository persons;
    private final DepartmentRepository departments;
    private final DepartmentVersionRepository departmentVersions;
    private final DepartmentServiceCompanyRepository serviceCompanies;
    private final IdentifierGuard identifiers;
    private final SourceDocumentService documents;

    public record CompanyRow(
            Long id,
            String companyCode,
            String name,
            String companyType,
            String status,
            String affiliation,
            String unifiedSocialCreditCode) {
    }

    public List<CompanyRow> list() {
        Map<Long, LegalPerson> legalPersonMap = legalPersons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Company> companyMap = companies.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        return companies.findAllByOrderByCompanyCodeAsc().stream().map(company -> {
            String affiliation;
            if (Codes.BRANCH.equals(company.companyType)) {
                Company head = companyMap.get(company.headCompanyId);
                affiliation = head == null ? "总公司未指定" : "总公司：" + head.name;
            } else {
                LegalPerson legalPerson = legalPersonMap.get(company.legalPersonId);
                affiliation = legalPerson == null ? "法人未指定" : "法人：" + legalPerson.legalName;
            }
            return new CompanyRow(
                    company.id,
                    company.companyCode,
                    company.name,
                    company.companyType,
                    company.status,
                    affiliation,
                    company.unifiedSocialCreditCode);
        }).toList();
    }

    public Company get(Long id) {
        return companies.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<Company> headChoices(Long selfId) {
        return companies.findByCompanyTypeOrderByCompanyCodeAsc(Codes.COMPANY).stream()
                .filter(company -> selfId == null || !selfId.equals(company.id))
                .toList();
    }

    public CompanyForm form(Long id) {
        Company company = get(id);
        CompanyForm form = new CompanyForm();
        form.setId(company.id);
        form.setCompanyCode(company.companyCode);
        form.setName(company.name);
        form.setCompanyType(company.companyType);
        form.setLegalPersonId(company.legalPersonId);
        form.setHeadCompanyId(company.headCompanyId);
        form.setCompanyForm(company.companyForm);
        form.setContactPhone(company.contactPhone);
        form.setContactEmail(company.contactEmail);
        form.setBusinessAddress(company.businessAddress);
        form.setEstablishedAt(company.establishedAt);
        form.setTerminatedAt(company.terminatedAt);
        form.setStatus(company.status);
        form.setRegistrations(registrations.findByCompanyIdOrderByVersionNumberAsc(id).stream().map(this::toRegistrationForm).toList());
        form.setBankAccounts(bankAccounts.findByCompanyIdOrderByIdAsc(id).stream().map(this::toBankForm).toList());
        form.setShareholdings(shareholdings.findByCompanyIdOrderByIdAsc(id).stream().map(this::toShareForm).toList());
        form.setVersions(versions.findByCompanyIdOrderByVersionNumberAsc(id).stream().map(this::toVersionForm).toList());
        return form;
    }

    @Transactional
    public long save(CompanyForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validate(form, errors);
        errors.throwIfAny();

        Company company = form.getId() == null ? new Company() : get(form.getId());
        applyHeader(company, form);
        company = companies.saveAndFlush(company);

        Set<Long> preservedRegistrations = splitRegistrations(company, form.getRegistrations());
        applySnapshot(company, openRegistration(form.getRegistrations()));
        if (Codes.BRANCH.equals(company.companyType)) {
            company.registeredCapitalAmount = null;
            company.registeredCapitalCurrency = null;
        }
        if (Codes.COMPANY.equals(company.companyType)) {
            company.branchManagerPersonId = null;
        }
        companies.save(company);
        assertRegistrationPeriods(company.id, form.getRegistrations(), preservedRegistrations);
        syncRegistrations(company.id, form.getRegistrations(), preservedRegistrations);
        syncBankAccounts(company.id, form.getBankAccounts());
        syncShareholdings(company.id, form.getShareholdings());
        syncVersions(company, form.getVersions());
        return company.id;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (!companies.findByHeadCompanyId(id).isEmpty() || !versions.findByHeadCompanyId(id).isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "仍有分公司或公司版本指向该公司，不能删除"));
        }
        if (!shareholdings.findByShareholderCompanyId(id).isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "该公司仍是其他公司的股东，不能删除"));
        }
        if (!departments.findByCompanyIdOrderByDepartmentCodeAsc(id).isEmpty()
                || !departmentVersions.findByCompanyId(id).isEmpty()
                || !serviceCompanies.findByServiceCompanyId(id).isEmpty()) {
            throw new FieldErrorsException(Map.of("*", "该公司仍被部门引用，不能删除"));
        }
        registrations.findByCompanyIdOrderByVersionNumberAsc(id).forEach(registrations::delete);
        bankAccounts.findByCompanyIdOrderByIdAsc(id).forEach(bankAccounts::delete);
        shareholdings.findByCompanyIdOrderByIdAsc(id).forEach(shareholdings::delete);
        versions.findByCompanyIdOrderByVersionNumberAsc(id).forEach(versions::delete);
        companies.deleteById(id);
    }

    private void normalize(CompanyForm form) {
        form.setCompanyCode(Text.blankToNull(form.getCompanyCode()));
        form.setName(Text.blankToNull(form.getName()));
        form.setCompanyType(Text.blankToNull(form.getCompanyType()));
        form.setCompanyForm(Text.blankToNull(form.getCompanyForm()));
        form.setContactPhone(Text.blankToNull(form.getContactPhone()));
        form.setContactEmail(Text.blankToNull(form.getContactEmail()));
        form.setBusinessAddress(Text.blankToNull(form.getBusinessAddress()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        if (Codes.BRANCH.equals(form.getCompanyType())) {
            form.setLegalPersonId(null);
            form.setCompanyForm(null);
        } else if (Codes.COMPANY.equals(form.getCompanyType())) {
            form.setHeadCompanyId(null);
        }

        List<RegistrationForm> regs = new ArrayList<>();
        for (RegistrationForm row : Periods.mutable(form.getRegistrations())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            normalizeRegistration(row);
            if (Codes.BRANCH.equals(form.getCompanyType())) {
                row.setRegisteredCapitalAmount(null);
                row.setRegisteredCapitalCurrency(null);
            }
            if (Codes.COMPANY.equals(form.getCompanyType())) {
                row.setBranchManagerPersonId(null);
            }
            if (row.getId() == null && isBlankRegistration(row)) {
                continue;
            }
            regs.add(row);
        }
        form.setRegistrations(regs);

        List<BankAccountForm> banks = new ArrayList<>();
        for (BankAccountForm row : Periods.mutable(form.getBankAccounts())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            row.setBankAccountName(Text.blankToNull(row.getBankAccountName()));
            row.setBankAccountNumber(Text.blankToNull(row.getBankAccountNumber()));
            row.setBankName(Text.blankToNull(row.getBankName()));
            row.setBankBranchName(Text.blankToNull(row.getBankBranchName()));
            row.setBankRoutingNumber(Text.blankToNull(row.getBankRoutingNumber()));
            row.setAccountCurrency(Text.upperOrNull(row.getAccountCurrency()));
            row.setStatus(Text.blankToNull(row.getStatus()));
            if (row.getId() == null && isBlankBank(row)) {
                continue;
            }
            banks.add(row);
        }
        form.setBankAccounts(banks);

        List<ShareholdingForm> shares = new ArrayList<>();
        for (ShareholdingForm row : Periods.mutable(form.getShareholdings())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            row.setShareClass(Text.blankToNull(row.getShareClass()));
            row.setCurrency(Text.upperOrNull(row.getCurrency()));
            if (row.getId() == null && isBlankShare(row)) {
                continue;
            }
            shares.add(row);
        }
        form.setShareholdings(shares);

        List<VersionForm> versionRows = new ArrayList<>();
        for (VersionForm row : Periods.mutable(form.getVersions())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            if (row.getId() == null && row.getVersionNumber() == null && row.getValidFrom() == null
                    && row.getValidTo() == null && row.getBasisDocumentId() == null) {
                continue;
            }
            versionRows.add(row);
        }
        form.setVersions(versionRows);
    }

    private void normalizeRegistration(RegistrationForm row) {
        row.setRegistrationType(Text.blankToNull(row.getRegistrationType()));
        row.setUnifiedSocialCreditCode(Text.upperOrNull(row.getUnifiedSocialCreditCode()));
        row.setBusinessLicenseRegistrationNumber(Text.blankToNull(row.getBusinessLicenseRegistrationNumber()));
        row.setTaxpayerIdentificationNumber(Text.upperOrNull(row.getTaxpayerIdentificationNumber()));
        row.setRegistrationAuthority(Text.blankToNull(row.getRegistrationAuthority()));
        row.setRegistrationJurisdiction(Text.blankToNull(row.getRegistrationJurisdiction()));
        row.setRegisteredName(Text.blankToNull(row.getRegisteredName()));
        row.setRegisteredAddress(Text.blankToNull(row.getRegisteredAddress()));
        row.setRegistrationStatus(Text.blankToNull(row.getRegistrationStatus()));
        row.setBusinessScope(Text.blankToNull(row.getBusinessScope()));
        row.setRegisteredCapitalCurrency(Text.upperOrNull(row.getRegisteredCapitalCurrency()));
    }

    private boolean isBlankRegistration(RegistrationForm row) {
        return row.getVersionNumber() == null
                && row.getRegistrationType() == null
                && row.getUnifiedSocialCreditCode() == null
                && row.getBusinessLicenseRegistrationNumber() == null
                && row.getTaxpayerIdentificationNumber() == null
                && row.getRegistrationAuthority() == null
                && row.getRegistrationJurisdiction() == null
                && row.getRegisteredName() == null
                && row.getRegisteredAddress() == null
                && row.getRegistrationStatus() == null
                && row.getRegisteredAt() == null
                && row.getBusinessScope() == null
                && row.getRegisteredCapitalAmount() == null
                && row.getRegisteredCapitalCurrency() == null
                && row.getBusinessTermStartDate() == null
                && row.getBusinessTermEndDate() == null
                && row.getBusinessTermIndefinite() == null
                && row.getBranchManagerPersonId() == null
                && row.getBusinessLicenseDocumentId() == null
                && row.getTaxInformationSourceDocumentId() == null
                && row.getValidFrom() == null
                && row.getValidTo() == null
                && row.getSourceDocumentId() == null;
    }

    private boolean isBlankBank(BankAccountForm row) {
        return row.getBankAccountName() == null
                && row.getBankAccountNumber() == null
                && row.getBankName() == null
                && row.getBankBranchName() == null
                && row.getBankRoutingNumber() == null
                && row.getAccountCurrency() == null
                && !row.isReceiptAccount()
                && !row.isPaymentAccount()
                && row.getValidFrom() == null
                && row.getValidTo() == null
                && row.getStatus() == null;
    }

    private boolean isBlankShare(ShareholdingForm row) {
        return row.getShareholderCompanyId() == null
                && row.getShareholderLegalPersonId() == null
                && row.getShareholderPersonId() == null
                && row.getShareClass() == null
                && row.getOwnershipRatio() == null
                && row.getSubscribedCapitalAmount() == null
                && row.getCurrency() == null
                && row.getValidFrom() == null
                && row.getValidTo() == null
                && row.getBasisDocumentId() == null;
    }

    private void validate(CompanyForm form, FieldErrors errors) {
        if (form.getCompanyCode() == null) {
            errors.add("companyCode", "请填写公司编号");
        } else if (companies.findByCompanyCode(form.getCompanyCode()).stream()
                .anyMatch(company -> form.getId() == null || !company.id.equals(form.getId()))) {
            errors.add("companyCode", "公司编号已存在");
        }
        if (form.getName() == null) {
            errors.add("name", "请填写业务名称");
        }
        if (form.getCompanyType() == null || !Codes.COMPANY_TYPES.contains(form.getCompanyType())) {
            errors.add("companyType", "公司类型只能是公司或分公司");
        }
        if (form.getStatus() == null || !Codes.COMPANY_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择公司状态");
        }
        if (!Text.emailOk(form.getContactEmail())) {
            errors.add("contactEmail", "邮箱格式不正确");
        }
        if (form.getEstablishedAt() != null && form.getTerminatedAt() != null
                && form.getTerminatedAt().isBefore(form.getEstablishedAt())) {
            errors.add("terminatedAt", "终止日期不能早于成立日期");
        }

        if (Codes.COMPANY.equals(form.getCompanyType())) {
            if (form.getLegalPersonId() == null || legalPersons.findById(form.getLegalPersonId()).isEmpty()) {
                errors.add("legalPersonId", "公司必须关联法人");
            } else {
                if (companies.findByLegalPersonId(form.getLegalPersonId()).stream()
                        .anyMatch(company -> form.getId() == null || !company.id.equals(form.getId()))) {
                    errors.add("legalPersonId", "该法人已关联其他公司，公司与法人须一对一");
                }
                if (!legalPersonRegistrations.findByLegalPersonIdOrderByValidFromAsc(form.getLegalPersonId()).isEmpty()) {
                    errors.add("legalPersonId", "该法人已有非公司登记资料。公司证照只维护在公司登记中");
                }
            }
            if (form.getCompanyForm() == null || !Codes.COMPANY_FORMS.contains(form.getCompanyForm())) {
                errors.add("companyForm", "请选择公司形式");
            }
        }
        if (Codes.BRANCH.equals(form.getCompanyType())) {
            if (form.getHeadCompanyId() == null) {
                errors.add("headCompanyId", "分公司必须指定总公司");
            } else if (form.getHeadCompanyId().equals(form.getId())) {
                errors.add("headCompanyId", "总公司不能指向自身");
            } else {
                Company head = companies.findById(form.getHeadCompanyId()).orElse(null);
                if (head == null) {
                    errors.add("headCompanyId", "总公司不存在");
                } else if (!Codes.COMPANY.equals(head.companyType)) {
                    errors.add("headCompanyId", "总公司必须是公司类型，不能是分公司");
                }
            }
            if (!form.getShareholdings().isEmpty()) {
                errors.add("shareholdings", "分公司不记录股东明细");
            }
        }

        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        Set<Integer> versionNumbers = new HashSet<>();
        int openCount = 0;
        for (int i = 0; i < form.getRegistrations().size(); i++) {
            RegistrationForm row = form.getRegistrations().get(i);
            String prefix = "registrations[" + i + "]";
            validateRegistration(form, errors, row, prefix);
            if (row.getVersionNumber() != null && !versionNumbers.add(row.getVersionNumber())) {
                errors.add(prefix + ".versionNumber", "登记版本号在本公司内不能重复");
            }
            if (row.getValidTo() == null) {
                openCount++;
            }
            froms.add(row.getValidFrom());
            tos.add(row.getValidTo());
        }
        if (openCount > 1) {
            errors.add("registrations", "同一公司同时只能有一条未结束的登记版本");
        }
        errors.add("registrations", Periods.overlapError(froms, tos, "公司登记"));

        for (int i = 0; i < form.getBankAccounts().size(); i++) {
            validateBank(errors, form.getBankAccounts().get(i), "bankAccounts[" + i + "]");
        }
        for (int i = 0; i < form.getShareholdings().size(); i++) {
            validateShare(form, errors, form.getShareholdings().get(i), "shareholdings[" + i + "]");
        }
        int openVersions = 0;
        for (int i = 0; i < form.getVersions().size(); i++) {
            VersionForm row = form.getVersions().get(i);
            String prefix = "versions[" + i + "]";
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写版本生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            if (row.getId() == null && row.getValidTo() != null) {
                errors.add(prefix, "历史公司版本由主记录变更生成");
            }
            if (row.getValidTo() == null) {
                openVersions++;
            }
            documents.require(row.getBasisDocumentId(), errors, prefix + ".basisDocumentId");
        }
        if (openVersions > 1) {
            errors.add("versions", "同时只能有一条未结束的公司版本");
        }
    }

    private void validateRegistration(CompanyForm form, FieldErrors errors, RegistrationForm row, String prefix) {
        if (row.getVersionNumber() == null || row.getVersionNumber() < 1) {
            errors.add(prefix + ".versionNumber", "请填写大于 0 的登记版本号");
        }
        if (row.getRegistrationType() == null || !Codes.REGISTRATION_TYPES.contains(row.getRegistrationType())) {
            errors.add(prefix + ".registrationType", "请选择登记类型");
        }
        if (row.getRegistrationAuthority() == null) {
            errors.add(prefix + ".registrationAuthority", "请填写登记机关");
        }
        if (row.getRegistrationJurisdiction() == null) {
            errors.add(prefix + ".registrationJurisdiction", "请填写登记国家或地区");
        }
        if (row.getRegisteredName() == null) {
            errors.add(prefix + ".registeredName", "请填写登记名称");
        }
        if (row.getRegisteredAddress() == null) {
            errors.add(prefix + ".registeredAddress", "请填写注册地址");
        }
        if (row.getRegistrationStatus() == null || !Codes.REGISTRATION_STATUS.contains(row.getRegistrationStatus())) {
            errors.add(prefix + ".registrationStatus", "请选择登记状态");
        }
        if (row.getRegisteredAt() == null) {
            errors.add(prefix + ".registeredAt", "请填写登记日期");
        }
        if (row.getValidFrom() == null) {
            errors.add(prefix + ".validFrom", "请填写登记生效日");
        }
        errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
        errors.add(prefix + ".unifiedSocialCreditCode",
                IdentifierGuard.usccFormat(row.getRegistrationJurisdiction(), row.getUnifiedSocialCreditCode()));
        errors.add(prefix + ".taxpayerIdentificationNumber",
                IdentifierGuard.tinFormat(row.getRegistrationJurisdiction(), row.getTaxpayerIdentificationNumber()));
        identifiers.checkUscc(errors, prefix + ".unifiedSocialCreditCode", row.getUnifiedSocialCreditCode(), form.getId(), null);
        identifiers.checkBusinessLicense(errors, prefix + ".businessLicenseRegistrationNumber",
                row.getBusinessLicenseRegistrationNumber(), form.getId());
        identifiers.checkTin(errors, prefix + ".taxpayerIdentificationNumber", row.getTaxpayerIdentificationNumber(), form.getId(), null);
        if (row.getRegisteredCapitalAmount() != null && row.getRegisteredCapitalCurrency() == null) {
            errors.add(prefix + ".registeredCapitalCurrency", "填写注册资本时必须填写币种");
        }
        if (Boolean.TRUE.equals(row.getBusinessTermIndefinite()) && row.getBusinessTermEndDate() != null) {
            errors.add(prefix + ".businessTermEndDate", "长期经营时营业期限结束日须留空");
        }
        if (row.getBusinessTermStartDate() != null && row.getBusinessTermEndDate() != null
                && row.getBusinessTermEndDate().isBefore(row.getBusinessTermStartDate())) {
            errors.add(prefix + ".businessTermEndDate", "营业期限结束不能早于开始");
        }
        if (row.getBranchManagerPersonId() != null && persons.findById(row.getBranchManagerPersonId()).isEmpty()) {
            errors.add(prefix + ".branchManagerPersonId", "分公司负责人不存在");
        }
        documents.require(row.getBusinessLicenseDocumentId(), errors, prefix + ".businessLicenseDocumentId");
        documents.require(row.getTaxInformationSourceDocumentId(), errors, prefix + ".taxInformationSourceDocumentId");
        documents.require(row.getSourceDocumentId(), errors, prefix + ".sourceDocumentId");
    }

    private void validateBank(FieldErrors errors, BankAccountForm row, String prefix) {
        if (row.getBankAccountName() == null) {
            errors.add(prefix + ".bankAccountName", "请填写户名");
        }
        if (row.getBankAccountNumber() == null) {
            errors.add(prefix + ".bankAccountNumber", "请填写账号");
        }
        if (row.getBankName() == null) {
            errors.add(prefix + ".bankName", "请填写开户行");
        }
        if (row.getAccountCurrency() == null) {
            errors.add(prefix + ".accountCurrency", "请填写账户币种");
        }
        if (row.getValidFrom() == null) {
            errors.add(prefix + ".validFrom", "请填写账户生效日");
        }
        errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
        if (row.getStatus() == null || !Codes.BANK_STATUS.contains(row.getStatus())) {
            errors.add(prefix + ".status", "请选择账户状态");
        }
        if (!row.isReceiptAccount() && !row.isPaymentAccount()) {
            errors.add(prefix + ".receiptAccount", "账户至少指定收款或付款用途之一");
        }
    }

    private void validateShare(CompanyForm form, FieldErrors errors, ShareholdingForm row, String prefix) {
        int selected = 0;
        if (row.getShareholderCompanyId() != null) {
            selected++;
        }
        if (row.getShareholderLegalPersonId() != null) {
            selected++;
        }
        if (row.getShareholderPersonId() != null) {
            selected++;
        }
        if (selected != 1) {
            errors.add(prefix + ".shareholderCompanyId", "股东须在公司、法人、自然人中恰好选择一个");
        }
        if (row.getShareholderCompanyId() != null) {
            Company shareholder = companies.findById(row.getShareholderCompanyId()).orElse(null);
            if (shareholder == null) {
                errors.add(prefix + ".shareholderCompanyId", "股东公司不存在");
            } else if (!Codes.COMPANY.equals(shareholder.companyType)) {
                errors.add(prefix + ".shareholderCompanyId", "公司类型股东必须是公司，不能是分公司");
            }
        }
        if (row.getShareholderLegalPersonId() != null
                && legalPersons.findById(row.getShareholderLegalPersonId()).isEmpty()) {
            errors.add(prefix + ".shareholderLegalPersonId", "股东法人不存在");
        }
        if (row.getShareholderPersonId() != null && persons.findById(row.getShareholderPersonId()).isEmpty()) {
            errors.add(prefix + ".shareholderPersonId", "股东自然人不存在");
        }
        if (row.getShareClass() == null || !Codes.SHARE_CLASSES.contains(row.getShareClass())) {
            errors.add(prefix + ".shareClass", "请选择股份类别");
        }
        if (row.getOwnershipRatio() == null
                || row.getOwnershipRatio().compareTo(BigDecimal.ZERO) < 0
                || row.getOwnershipRatio().compareTo(BigDecimal.ONE) > 0) {
            errors.add(prefix + ".ownershipRatio", "持股比例须在 0 到 1 之间");
        }
        if (row.getSubscribedCapitalAmount() != null && row.getCurrency() == null) {
            errors.add(prefix + ".currency", "填写认缴出资额时必须填写币种");
        }
        if (row.getValidFrom() == null) {
            errors.add(prefix + ".validFrom", "请填写持股生效日");
        }
        errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
        documents.require(row.getBasisDocumentId(), errors, prefix + ".basisDocumentId");
    }

    private void applyHeader(Company company, CompanyForm form) {
        company.companyCode = form.getCompanyCode();
        company.name = form.getName();
        company.companyType = form.getCompanyType();
        company.legalPersonId = form.getLegalPersonId();
        company.headCompanyId = form.getHeadCompanyId();
        company.companyForm = form.getCompanyForm();
        company.contactPhone = form.getContactPhone();
        company.contactEmail = form.getContactEmail();
        company.businessAddress = form.getBusinessAddress();
        company.establishedAt = form.getEstablishedAt();
        company.terminatedAt = form.getTerminatedAt();
        company.status = form.getStatus();
    }

    private RegistrationForm openRegistration(List<RegistrationForm> rows) {
        RegistrationForm open = null;
        for (RegistrationForm row : rows) {
            if (row.getValidTo() == null) {
                open = row;
            }
        }
        return open;
    }

    private void applySnapshot(Company company, RegistrationForm open) {
        if (open == null) {
            company.unifiedSocialCreditCode = null;
            company.businessLicenseRegistrationNumber = null;
            company.taxpayerIdentificationNumber = null;
            company.registeredName = null;
            company.registeredAddress = null;
            company.registrationJurisdiction = null;
            company.businessLicenseDocumentId = null;
            company.registeredCapitalAmount = null;
            company.registeredCapitalCurrency = null;
            company.businessTermStartDate = null;
            company.businessTermEndDate = null;
            company.businessTermIndefinite = null;
            company.branchManagerPersonId = null;
            return;
        }
        company.unifiedSocialCreditCode = open.getUnifiedSocialCreditCode();
        company.businessLicenseRegistrationNumber = open.getBusinessLicenseRegistrationNumber();
        company.taxpayerIdentificationNumber = open.getTaxpayerIdentificationNumber();
        company.registeredName = open.getRegisteredName();
        company.registeredAddress = open.getRegisteredAddress();
        company.registrationJurisdiction = open.getRegistrationJurisdiction();
        company.businessLicenseDocumentId = open.getBusinessLicenseDocumentId();
        company.registeredCapitalAmount = open.getRegisteredCapitalAmount();
        company.registeredCapitalCurrency = open.getRegisteredCapitalCurrency();
        company.businessTermStartDate = open.getBusinessTermStartDate();
        company.businessTermEndDate = open.getBusinessTermEndDate();
        company.businessTermIndefinite = open.getBusinessTermIndefinite();
        company.branchManagerPersonId = open.getBranchManagerPersonId();
    }

    private Set<Long> splitRegistrations(Company company, List<RegistrationForm> rows) {
        Set<Long> preserved = new HashSet<>();
        LocalDate today = LocalDate.now();
        int maxNumber = rows.stream().filter(row -> row.getVersionNumber() != null)
                .mapToInt(RegistrationForm::getVersionNumber).max().orElse(0);
        for (CompanyRegistration existing : registrations.findByCompanyIdOrderByVersionNumberAsc(company.id)) {
            maxNumber = Math.max(maxNumber, existing.versionNumber);
        }
        for (RegistrationForm row : rows) {
            if (row.getId() == null || row.getValidTo() != null) {
                continue;
            }
            CompanyRegistration existing = registrations.findById(row.getId()).orElse(null);
            if (existing == null || !company.id.equals(existing.companyId)) {
                continue;
            }
            if (existing.validFrom != null && existing.validFrom.isBefore(today) && registrationChanged(existing, row)) {
                existing.validTo = today;
                registrations.save(existing);
                preserved.add(existing.id);
                row.setId(null);
                row.setValidFrom(today);
                row.setVersionNumber(maxNumber + 1);
            }
        }
        return preserved;
    }

    private boolean registrationChanged(CompanyRegistration existing, RegistrationForm row) {
        return !Cmp.eq(existing.registrationType, row.getRegistrationType())
                || !Cmp.eq(existing.unifiedSocialCreditCode, row.getUnifiedSocialCreditCode())
                || !Cmp.eq(existing.businessLicenseRegistrationNumber, row.getBusinessLicenseRegistrationNumber())
                || !Cmp.eq(existing.taxpayerIdentificationNumber, row.getTaxpayerIdentificationNumber())
                || !Cmp.eq(existing.registrationAuthority, row.getRegistrationAuthority())
                || !Cmp.eq(existing.registrationJurisdiction, row.getRegistrationJurisdiction())
                || !Cmp.eq(existing.registeredName, row.getRegisteredName())
                || !Cmp.eq(existing.registeredAddress, row.getRegisteredAddress())
                || !Cmp.eq(existing.registrationStatus, row.getRegistrationStatus())
                || !Cmp.eq(existing.registeredAt, row.getRegisteredAt())
                || !Cmp.eq(existing.businessScope, row.getBusinessScope())
                || !Cmp.eq(existing.registeredCapitalAmount, row.getRegisteredCapitalAmount())
                || !Cmp.eq(existing.registeredCapitalCurrency, row.getRegisteredCapitalCurrency())
                || !Cmp.eq(existing.businessTermStartDate, row.getBusinessTermStartDate())
                || !Cmp.eq(existing.businessTermEndDate, row.getBusinessTermEndDate())
                || !Cmp.eq(existing.businessTermIndefinite, row.getBusinessTermIndefinite())
                || !Cmp.eq(existing.branchManagerPersonId, row.getBranchManagerPersonId())
                || !Cmp.eq(existing.businessLicenseDocumentId, row.getBusinessLicenseDocumentId())
                || !Cmp.eq(existing.taxInformationSourceDocumentId, row.getTaxInformationSourceDocumentId())
                || !Cmp.eq(existing.sourceDocumentId, row.getSourceDocumentId());
    }

    private void assertRegistrationPeriods(Long companyId, List<RegistrationForm> rows, Set<Long> preserved) {
        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        for (RegistrationForm row : rows) {
            froms.add(row.getValidFrom());
            tos.add(row.getValidTo());
        }
        for (Long id : preserved) {
            registrations.findById(id).ifPresent(existing -> {
                froms.add(existing.validFrom);
                tos.add(existing.validTo);
            });
        }
        String message = Periods.overlapError(froms, tos, "公司登记");
        if (message != null) {
            throw new FieldErrorsException(Map.of("registrations", message));
        }
        Set<Integer> numbers = new HashSet<>();
        for (RegistrationForm row : rows) {
            if (row.getVersionNumber() != null && !numbers.add(row.getVersionNumber())) {
                throw new FieldErrorsException(Map.of("registrations", "登记版本号重复"));
            }
        }
        for (Long id : preserved) {
            registrations.findById(id).ifPresent(existing -> {
                if (!numbers.add(existing.versionNumber)) {
                    throw new FieldErrorsException(Map.of("registrations", "登记版本号与保留的历史版本重复"));
                }
            });
        }
    }

    private void syncRegistrations(Long companyId, List<RegistrationForm> rows, Set<Long> preserved) {
        Set<Long> keep = new HashSet<>(preserved);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (RegistrationForm row : rows) {
            CompanyRegistration entity;
            if (row.getId() != null) {
                entity = registrations.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "公司登记不存在")));
                if (!companyId.equals(entity.companyId)) {
                    throw new FieldErrorsException(Map.of("*", "公司登记不属于当前公司"));
                }
            } else {
                entity = new CompanyRegistration();
                entity.companyId = companyId;
                entity.recordedAt = now;
            }
            copyRegistration(row, entity);
            keep.add(registrations.save(entity).id);
        }
        for (CompanyRegistration existing : registrations.findByCompanyIdOrderByVersionNumberAsc(companyId)) {
            if (!keep.contains(existing.id)) {
                registrations.delete(existing);
            }
        }
    }

    private void copyRegistration(RegistrationForm row, CompanyRegistration entity) {
        entity.versionNumber = row.getVersionNumber();
        entity.registrationType = row.getRegistrationType();
        entity.unifiedSocialCreditCode = row.getUnifiedSocialCreditCode();
        entity.businessLicenseRegistrationNumber = row.getBusinessLicenseRegistrationNumber();
        entity.taxpayerIdentificationNumber = row.getTaxpayerIdentificationNumber();
        entity.registrationAuthority = row.getRegistrationAuthority();
        entity.registrationJurisdiction = row.getRegistrationJurisdiction();
        entity.registeredName = row.getRegisteredName();
        entity.registeredAddress = row.getRegisteredAddress();
        entity.registrationStatus = row.getRegistrationStatus();
        entity.registeredAt = row.getRegisteredAt();
        entity.businessScope = row.getBusinessScope();
        entity.registeredCapitalAmount = row.getRegisteredCapitalAmount();
        entity.registeredCapitalCurrency = row.getRegisteredCapitalCurrency();
        entity.businessTermStartDate = row.getBusinessTermStartDate();
        entity.businessTermEndDate = row.getBusinessTermEndDate();
        entity.businessTermIndefinite = row.getBusinessTermIndefinite();
        entity.branchManagerPersonId = row.getBranchManagerPersonId();
        entity.businessLicenseDocumentId = row.getBusinessLicenseDocumentId();
        entity.taxInformationSourceDocumentId = row.getTaxInformationSourceDocumentId();
        entity.validFrom = row.getValidFrom();
        entity.validTo = row.getValidTo();
        entity.sourceDocumentId = row.getSourceDocumentId();
        if (entity.recordedAt == null) {
            entity.recordedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }

    private void syncBankAccounts(Long companyId, List<BankAccountForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (BankAccountForm row : rows) {
            CompanyBankAccount entity;
            if (row.getId() != null) {
                entity = bankAccounts.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "银行账户不存在")));
                if (!companyId.equals(entity.companyId)) {
                    throw new FieldErrorsException(Map.of("*", "银行账户不属于当前公司"));
                }
            } else {
                entity = new CompanyBankAccount();
                entity.companyId = companyId;
            }
            entity.bankAccountName = row.getBankAccountName();
            entity.bankAccountNumber = row.getBankAccountNumber();
            entity.bankName = row.getBankName();
            entity.bankBranchName = row.getBankBranchName();
            entity.bankRoutingNumber = row.getBankRoutingNumber();
            entity.accountCurrency = row.getAccountCurrency();
            entity.receiptAccount = row.isReceiptAccount();
            entity.paymentAccount = row.isPaymentAccount();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.status = row.getStatus();
            keep.add(bankAccounts.save(entity).id);
        }
        for (CompanyBankAccount existing : bankAccounts.findByCompanyIdOrderByIdAsc(companyId)) {
            if (!keep.contains(existing.id)) {
                bankAccounts.delete(existing);
            }
        }
    }

    private void syncShareholdings(Long companyId, List<ShareholdingForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (ShareholdingForm row : rows) {
            CompanyShareholding entity;
            if (row.getId() != null) {
                entity = shareholdings.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "股东明细不存在")));
                if (!companyId.equals(entity.companyId)) {
                    throw new FieldErrorsException(Map.of("*", "股东明细不属于当前公司"));
                }
            } else {
                entity = new CompanyShareholding();
                entity.companyId = companyId;
            }
            entity.shareholderCompanyId = row.getShareholderCompanyId();
            entity.shareholderLegalPersonId = row.getShareholderLegalPersonId();
            entity.shareholderPersonId = row.getShareholderPersonId();
            entity.shareClass = row.getShareClass();
            entity.ownershipRatio = row.getOwnershipRatio();
            entity.subscribedCapitalAmount = row.getSubscribedCapitalAmount();
            entity.currency = row.getCurrency();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.basisDocumentId = row.getBasisDocumentId();
            keep.add(shareholdings.save(entity).id);
        }
        for (CompanyShareholding existing : shareholdings.findByCompanyIdOrderByIdAsc(companyId)) {
            if (!keep.contains(existing.id)) {
                shareholdings.delete(existing);
            }
        }
    }

    private void syncVersions(Company company, List<VersionForm> rows) {
        List<VersionForm> working = new ArrayList<>(rows);
        List<CompanyVersion> existing = versions.findByCompanyIdOrderByVersionNumberAsc(company.id);
        CompanyVersion dbOpen = existing.stream().filter(version -> version.validTo == null).findFirst().orElse(null);
        LocalDate today = LocalDate.now();
        boolean split = dbOpen != null && !sameCore(dbOpen, company)
                && dbOpen.validFrom != null && dbOpen.validFrom.isBefore(today);
        if (split) {
            VersionForm matched = working.stream().filter(row -> dbOpen.id.equals(row.getId())).findFirst().orElse(null);
            Long newBasis = matched == null ? dbOpen.basisDocumentId : matched.getBasisDocumentId();
            if (matched != null) {
                matched.setValidTo(today);
                matched.setValidFrom(dbOpen.validFrom);
                matched.setBasisDocumentId(dbOpen.basisDocumentId);
            } else {
                VersionForm closed = new VersionForm();
                closed.setId(dbOpen.id);
                closed.setVersionNumber(dbOpen.versionNumber);
                closed.setValidFrom(dbOpen.validFrom);
                closed.setValidTo(today);
                closed.setBasisDocumentId(dbOpen.basisDocumentId);
                working.add(closed);
            }
            working.removeIf(row -> row.getId() == null && row.getValidTo() == null);
            VersionForm fresh = new VersionForm();
            fresh.setVersionNumber(nextVersionNumber(existing, working));
            fresh.setValidFrom(today);
            fresh.setBasisDocumentId(newBasis);
            working.add(fresh);
        }
        if (working.stream().noneMatch(row -> row.getValidTo() == null)) {
            if (dbOpen != null && !split) {
                working.removeIf(row -> dbOpen.id.equals(row.getId()));
                VersionForm keep = new VersionForm();
                keep.setId(dbOpen.id);
                keep.setVersionNumber(dbOpen.versionNumber);
                keep.setValidFrom(dbOpen.validFrom);
                keep.setBasisDocumentId(dbOpen.basisDocumentId);
                working.add(keep);
            } else {
                VersionForm fresh = new VersionForm();
                fresh.setVersionNumber(nextVersionNumber(existing, working));
                fresh.setValidFrom(company.establishedAt != null ? company.establishedAt : today);
                working.add(fresh);
            }
        }
        assertVersionPeriods(working);
        Set<Long> keep = new HashSet<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (VersionForm row : working) {
            CompanyVersion entity;
            if (row.getId() != null) {
                entity = versions.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(Map.of("*", "公司版本不存在")));
                if (!company.id.equals(entity.companyId)) {
                    throw new FieldErrorsException(Map.of("*", "公司版本不属于当前公司"));
                }
                entity.validFrom = row.getValidFrom();
                entity.validTo = row.getValidTo();
                entity.basisDocumentId = row.getBasisDocumentId();
                if (row.getValidTo() == null) {
                    copyCore(company, entity);
                }
            } else {
                entity = new CompanyVersion();
                entity.companyId = company.id;
                entity.versionNumber = row.getVersionNumber() == null ? nextVersionNumber(existing, working) : row.getVersionNumber();
                entity.validFrom = row.getValidFrom();
                entity.validTo = row.getValidTo();
                entity.basisDocumentId = row.getBasisDocumentId();
                entity.recordedAt = now;
                copyCore(company, entity);
            }
            keep.add(versions.save(entity).id);
        }
        for (CompanyVersion version : versions.findByCompanyIdOrderByVersionNumberAsc(company.id)) {
            if (!keep.contains(version.id)) {
                versions.delete(version);
            }
        }
    }

    private void assertVersionPeriods(List<VersionForm> rows) {
        long open = rows.stream().filter(row -> row.getValidTo() == null).count();
        if (open != 1) {
            throw new FieldErrorsException(Map.of("versions", "须保留一条当前公司版本"));
        }
        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        for (VersionForm row : rows) {
            if (row.getValidFrom() == null) {
                throw new FieldErrorsException(Map.of("versions", "公司版本缺少生效日"));
            }
            String order = Periods.orderError(row.getValidFrom(), row.getValidTo());
            if (order != null) {
                throw new FieldErrorsException(Map.of("versions", order));
            }
            froms.add(row.getValidFrom());
            tos.add(row.getValidTo());
        }
        String overlap = Periods.overlapError(froms, tos, "公司版本");
        if (overlap != null) {
            throw new FieldErrorsException(Map.of("versions", overlap));
        }
    }

    private int nextVersionNumber(List<CompanyVersion> existing, List<VersionForm> forms) {
        int max = 0;
        for (CompanyVersion version : existing) {
            max = Math.max(max, version.versionNumber);
        }
        for (VersionForm form : forms) {
            if (form.getVersionNumber() != null) {
                max = Math.max(max, form.getVersionNumber());
            }
        }
        return max + 1;
    }

    private boolean sameCore(CompanyVersion version, Company company) {
        return Cmp.eq(version.companyCode, company.companyCode)
                && Cmp.eq(version.name, company.name)
                && Cmp.eq(version.companyType, company.companyType)
                && Cmp.eq(version.legalPersonId, company.legalPersonId)
                && Cmp.eq(version.headCompanyId, company.headCompanyId)
                && Cmp.eq(version.companyForm, company.companyForm)
                && Cmp.eq(version.contactPhone, company.contactPhone)
                && Cmp.eq(version.contactEmail, company.contactEmail)
                && Cmp.eq(version.businessAddress, company.businessAddress)
                && Cmp.eq(version.establishedAt, company.establishedAt)
                && Cmp.eq(version.terminatedAt, company.terminatedAt)
                && Cmp.eq(version.status, company.status);
    }

    private void copyCore(Company company, CompanyVersion version) {
        version.companyCode = company.companyCode;
        version.name = company.name;
        version.companyType = company.companyType;
        version.legalPersonId = company.legalPersonId;
        version.headCompanyId = company.headCompanyId;
        version.companyForm = company.companyForm;
        version.contactPhone = company.contactPhone;
        version.contactEmail = company.contactEmail;
        version.businessAddress = company.businessAddress;
        version.establishedAt = company.establishedAt;
        version.terminatedAt = company.terminatedAt;
        version.status = company.status;
    }

    private RegistrationForm toRegistrationForm(CompanyRegistration row) {
        RegistrationForm form = new RegistrationForm();
        form.setId(row.id);
        form.setVersionNumber(row.versionNumber);
        form.setRegistrationType(row.registrationType);
        form.setUnifiedSocialCreditCode(row.unifiedSocialCreditCode);
        form.setBusinessLicenseRegistrationNumber(row.businessLicenseRegistrationNumber);
        form.setTaxpayerIdentificationNumber(row.taxpayerIdentificationNumber);
        form.setRegistrationAuthority(row.registrationAuthority);
        form.setRegistrationJurisdiction(row.registrationJurisdiction);
        form.setRegisteredName(row.registeredName);
        form.setRegisteredAddress(row.registeredAddress);
        form.setRegistrationStatus(row.registrationStatus);
        form.setRegisteredAt(row.registeredAt);
        form.setBusinessScope(row.businessScope);
        form.setRegisteredCapitalAmount(row.registeredCapitalAmount);
        form.setRegisteredCapitalCurrency(row.registeredCapitalCurrency);
        form.setBusinessTermStartDate(row.businessTermStartDate);
        form.setBusinessTermEndDate(row.businessTermEndDate);
        form.setBusinessTermIndefinite(row.businessTermIndefinite);
        form.setBranchManagerPersonId(row.branchManagerPersonId);
        form.setBusinessLicenseDocumentId(row.businessLicenseDocumentId);
        form.setTaxInformationSourceDocumentId(row.taxInformationSourceDocumentId);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setSourceDocumentId(row.sourceDocumentId);
        return form;
    }

    private BankAccountForm toBankForm(CompanyBankAccount row) {
        BankAccountForm form = new BankAccountForm();
        form.setId(row.id);
        form.setBankAccountName(row.bankAccountName);
        form.setBankAccountNumber(row.bankAccountNumber);
        form.setBankName(row.bankName);
        form.setBankBranchName(row.bankBranchName);
        form.setBankRoutingNumber(row.bankRoutingNumber);
        form.setAccountCurrency(row.accountCurrency);
        form.setReceiptAccount(row.receiptAccount);
        form.setPaymentAccount(row.paymentAccount);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setStatus(row.status);
        return form;
    }

    private ShareholdingForm toShareForm(CompanyShareholding row) {
        ShareholdingForm form = new ShareholdingForm();
        form.setId(row.id);
        form.setShareholderCompanyId(row.shareholderCompanyId);
        form.setShareholderLegalPersonId(row.shareholderLegalPersonId);
        form.setShareholderPersonId(row.shareholderPersonId);
        form.setShareClass(row.shareClass);
        form.setOwnershipRatio(row.ownershipRatio);
        form.setSubscribedCapitalAmount(row.subscribedCapitalAmount);
        form.setCurrency(row.currency);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setBasisDocumentId(row.basisDocumentId);
        return form;
    }

    private VersionForm toVersionForm(CompanyVersion row) {
        VersionForm form = new VersionForm();
        form.setId(row.id);
        form.setVersionNumber(row.versionNumber);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setBasisDocumentId(row.basisDocumentId);
        return form;
    }
}
