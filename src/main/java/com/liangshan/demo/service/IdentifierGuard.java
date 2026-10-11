package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.CompanyRegistration;
import com.liangshan.demo.domain.LegalPersonRegistration;
import com.liangshan.demo.repository.CompanyRegistrationRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.LegalPersonRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 登记标识去重。同一公司的当前字段与其历史登记可以共用号码；不把信用代码和税号的相同值当成两家主体。
 */
@Component
@RequiredArgsConstructor
public class IdentifierGuard {

    private final CompanyRepository companies;
    private final CompanyRegistrationRepository companyRegistrations;
    private final LegalPersonRegistrationRepository legalPersonRegistrations;

    public void checkUscc(FieldErrors errors, String field, String code, Long selfCompanyId, Long selfLegalPersonId) {
        if (code == null) {
            return;
        }
        for (Company company : companies.findByUnifiedSocialCreditCode(code)) {
            if (selfCompanyId == null || !selfCompanyId.equals(company.id)) {
                errors.add(field, "统一社会信用代码已被其他公司使用");
            }
        }
        for (CompanyRegistration registration : companyRegistrations.findByUnifiedSocialCreditCode(code)) {
            if (selfCompanyId == null || !selfCompanyId.equals(registration.companyId)) {
                errors.add(field, "统一社会信用代码已被其他公司的登记版本使用");
            }
        }
        for (LegalPersonRegistration registration : legalPersonRegistrations.findByUnifiedSocialCreditCode(code)) {
            if (selfLegalPersonId == null || !selfLegalPersonId.equals(registration.legalPersonId)) {
                errors.add(field, "统一社会信用代码已被法人登记使用");
            }
        }
    }

    public void checkBusinessLicense(FieldErrors errors, String field, String number, Long selfCompanyId) {
        if (number == null) {
            return;
        }
        for (Company company : companies.findByBusinessLicenseRegistrationNumber(number)) {
            if (selfCompanyId == null || !selfCompanyId.equals(company.id)) {
                errors.add(field, "营业执照注册号已被其他公司使用");
            }
        }
        for (CompanyRegistration registration : companyRegistrations.findByBusinessLicenseRegistrationNumber(number)) {
            if (selfCompanyId == null || !selfCompanyId.equals(registration.companyId)) {
                errors.add(field, "营业执照注册号已被其他公司的登记版本使用");
            }
        }
    }

    public void checkTin(FieldErrors errors, String field, String tin, Long selfCompanyId, Long selfLegalPersonId) {
        if (tin == null) {
            return;
        }
        for (Company company : companies.findByTaxpayerIdentificationNumber(tin)) {
            if (selfCompanyId == null || !selfCompanyId.equals(company.id)) {
                errors.add(field, "纳税人识别号已被其他公司使用");
            }
        }
        for (CompanyRegistration registration : companyRegistrations.findByTaxpayerIdentificationNumber(tin)) {
            if (selfCompanyId == null || !selfCompanyId.equals(registration.companyId)) {
                errors.add(field, "纳税人识别号已被其他公司的登记版本使用");
            }
        }
        for (LegalPersonRegistration registration : legalPersonRegistrations.findByTaxpayerIdentificationNumber(tin)) {
            if (selfLegalPersonId == null || !selfLegalPersonId.equals(registration.legalPersonId)) {
                errors.add(field, "纳税人识别号已被法人登记使用");
            }
        }
    }

    public void checkCertificate(FieldErrors errors, String field, String number, Long selfLegalPersonId) {
        if (number == null) {
            return;
        }
        for (LegalPersonRegistration registration : legalPersonRegistrations.findByRegistrationCertificateNumber(number)) {
            if (selfLegalPersonId == null || !selfLegalPersonId.equals(registration.legalPersonId)) {
                errors.add(field, "登记证书号码已被其他法人使用");
            }
        }
    }

    public static String usccFormat(String jurisdiction, String code) {
        if (code == null || !Codes.china(jurisdiction)) {
            return null;
        }
        if (!code.matches("[0-9A-Z]{18}")) {
            return "中国统一社会信用代码须为 18 位数字或大写字母";
        }
        return null;
    }

    public static String tinFormat(String jurisdiction, String tin) {
        if (tin == null || !Codes.china(jurisdiction)) {
            return null;
        }
        if (!tin.matches("[0-9A-Z]{18}")) {
            return "中国纳税人识别号须为 18 位数字或大写字母";
        }
        return null;
    }
}
