package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.LegalPersonRegistration;
import com.liangshan.demo.domain.LegalRepresentativeTerm;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CompanyShareholdingRepository;
import com.liangshan.demo.repository.CompanyVersionRepository;
import com.liangshan.demo.repository.LegalPersonRegistrationRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.LegalRepresentativeTermRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.LegalPersonForm;
import com.liangshan.demo.web.form.LegalPersonForm.RegistrationForm;
import com.liangshan.demo.web.form.LegalPersonForm.RepresentativeForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LegalPersonService {

    private final LegalPersonRepository legalPersons;
    private final LegalPersonRegistrationRepository registrations;
    private final LegalRepresentativeTermRepository terms;
    private final PersonRepository persons;
    private final CompanyRepository companies;
    private final CompanyVersionRepository companyVersions;
    private final CompanyShareholdingRepository shareholdings;
    private final IdentifierGuard identifiers;
    private final SourceDocumentService documents;
    private final SupplierLinkGuard supplierLinks;
    private final ProjectLinkGuard projectLinks;
    private final ContractLinkGuard contractLinks;

    public List<LegalPerson> list() {
        return legalPersons.findAllByOrderByLegalNameAsc();
    }

    public LegalPerson get(Long id) {
        return legalPersons.findById(id).orElseThrow(NotFoundException::new);
    }

    public LegalPersonForm form(Long id) {
        LegalPerson legalPerson = get(id);
        LegalPersonForm form = new LegalPersonForm();
        form.setId(legalPerson.id);
        form.setLegalName(legalPerson.legalName);
        form.setLegalPersonType(legalPerson.legalPersonType);
        form.setEstablishedAt(legalPerson.establishedAt);
        form.setTerminatedAt(legalPerson.terminatedAt);
        form.setStatus(legalPerson.status);
        form.setRegistrations(registrations.findByLegalPersonIdOrderByValidFromAsc(id).stream().map(row -> {
            RegistrationForm item = new RegistrationForm();
            item.setId(row.id);
            item.setRegistrationType(row.registrationType);
            item.setUnifiedSocialCreditCode(row.unifiedSocialCreditCode);
            item.setRegistrationCertificateNumber(row.registrationCertificateNumber);
            item.setTaxpayerIdentificationNumber(row.taxpayerIdentificationNumber);
            item.setRegistrationAuthority(row.registrationAuthority);
            item.setJurisdiction(row.jurisdiction);
            item.setRegisteredName(row.registeredName);
            item.setRegisteredAddress(row.registeredAddress);
            item.setRegistrationStatus(row.registrationStatus);
            item.setRegisteredAt(row.registeredAt);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setSourceDocumentId(row.sourceDocumentId);
            return item;
        }).toList());
        form.setRepresentativeTerms(terms.findByLegalPersonIdOrderByValidFromAsc(id).stream().map(row -> {
            RepresentativeForm item = new RepresentativeForm();
            item.setId(row.id);
            item.setPersonId(row.personId);
            item.setValidFrom(row.validFrom);
            item.setValidTo(row.validTo);
            item.setBasisDocumentId(row.basisDocumentId);
            return item;
        }).toList());
        return form;
    }

    @Transactional
    public long save(LegalPersonForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        validate(form, errors);
        errors.throwIfAny();

        LegalPerson legalPerson = form.getId() == null ? new LegalPerson() : get(form.getId());
        legalPerson.legalName = form.getLegalName();
        legalPerson.legalPersonType = form.getLegalPersonType();
        legalPerson.establishedAt = form.getEstablishedAt();
        legalPerson.terminatedAt = form.getTerminatedAt();
        legalPerson.status = form.getStatus();
        legalPerson = legalPersons.saveAndFlush(legalPerson);

        Set<Long> preserved = splitRegistrations(legalPerson.id, form.getRegistrations());
        assertPeriods(form.getRegistrations(), preserved, "法人登记");
        syncRegistrations(legalPerson.id, form.getRegistrations(), preserved);
        syncTerms(legalPerson.id, form.getRepresentativeTerms());
        return legalPerson.id;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (!companies.findByLegalPersonId(id).isEmpty() || !companyVersions.findByLegalPersonId(id).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("*", "该法人仍被公司引用，不能删除"));
        }
        if (!shareholdings.findByShareholderLegalPersonId(id).isEmpty()) {
            throw new FieldErrorsException(java.util.Map.of("*", "该法人仍是股东，不能删除"));
        }
        if (supplierLinks.legalPersonUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该法人仍被供应商档案引用，不能删除"));
        }
        if (projectLinks.legalPersonUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该法人仍被项目引用，不能删除"));
        }
        if (contractLinks.legalPersonUsed(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该法人仍被合同引用，不能删除"));
        }
        registrations.findByLegalPersonIdOrderByValidFromAsc(id).forEach(registrations::delete);
        terms.findByLegalPersonIdOrderByValidFromAsc(id).forEach(terms::delete);
        legalPersons.deleteById(id);
    }

    private void normalize(LegalPersonForm form) {
        form.setLegalName(Text.blankToNull(form.getLegalName()));
        form.setLegalPersonType(Text.blankToNull(form.getLegalPersonType()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        List<RegistrationForm> regs = new ArrayList<>();
        for (RegistrationForm row : Periods.mutable(form.getRegistrations())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            row.setRegistrationType(Text.blankToNull(row.getRegistrationType()));
            row.setUnifiedSocialCreditCode(Text.upperOrNull(row.getUnifiedSocialCreditCode()));
            row.setRegistrationCertificateNumber(Text.blankToNull(row.getRegistrationCertificateNumber()));
            row.setTaxpayerIdentificationNumber(Text.upperOrNull(row.getTaxpayerIdentificationNumber()));
            row.setRegistrationAuthority(Text.blankToNull(row.getRegistrationAuthority()));
            row.setJurisdiction(Text.blankToNull(row.getJurisdiction()));
            row.setRegisteredName(Text.blankToNull(row.getRegisteredName()));
            row.setRegisteredAddress(Text.blankToNull(row.getRegisteredAddress()));
            row.setRegistrationStatus(Text.blankToNull(row.getRegistrationStatus()));
            if (row.getId() == null && isBlankRegistration(row)) {
                continue;
            }
            regs.add(row);
        }
        form.setRegistrations(regs);

        List<RepresentativeForm> termRows = new ArrayList<>();
        for (RepresentativeForm row : Periods.mutable(form.getRepresentativeTerms())) {
            if (row == null || row.isRemoved()) {
                continue;
            }
            if (row.getId() == null && row.getPersonId() == null && row.getValidFrom() == null
                    && row.getValidTo() == null && row.getBasisDocumentId() == null) {
                continue;
            }
            termRows.add(row);
        }
        form.setRepresentativeTerms(termRows);
    }

    private boolean isBlankRegistration(RegistrationForm row) {
        return row.getRegistrationType() == null
                && row.getUnifiedSocialCreditCode() == null
                && row.getRegistrationCertificateNumber() == null
                && row.getTaxpayerIdentificationNumber() == null
                && row.getRegistrationAuthority() == null
                && row.getJurisdiction() == null
                && row.getRegisteredName() == null
                && row.getRegisteredAddress() == null
                && row.getRegistrationStatus() == null
                && row.getRegisteredAt() == null
                && row.getValidFrom() == null
                && row.getValidTo() == null
                && row.getSourceDocumentId() == null;
    }

    private void validate(LegalPersonForm form, FieldErrors errors) {
        if (form.getLegalName() == null) {
            errors.add("legalName", "请填写法律名称");
        }
        if (form.getLegalPersonType() == null || !Codes.LEGAL_PERSON_TYPES.contains(form.getLegalPersonType())) {
            errors.add("legalPersonType", "请选择法人类型");
        }
        if (form.getStatus() == null || !Codes.LEGAL_PERSON_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择法人状态");
        }
        if (form.getEstablishedAt() != null && form.getTerminatedAt() != null
                && form.getTerminatedAt().isBefore(form.getEstablishedAt())) {
            errors.add("terminatedAt", "终止日期不能早于成立日期");
        }
        if (form.getId() != null && !form.getRegistrations().isEmpty()
                && !companies.findByLegalPersonId(form.getId()).isEmpty()) {
            errors.add("registrations", "该公司法人的证照由公司登记维护，法人登记集合只用于非公司法人");
        }
        List<LocalDate> froms = new ArrayList<>();
        List<LocalDate> tos = new ArrayList<>();
        for (int i = 0; i < form.getRegistrations().size(); i++) {
            RegistrationForm row = form.getRegistrations().get(i);
            String prefix = "registrations[" + i + "]";
            if (row.getRegistrationType() == null || !Codes.REGISTRATION_TYPES.contains(row.getRegistrationType())) {
                errors.add(prefix + ".registrationType", "请选择登记类型");
            }
            if (row.getRegistrationAuthority() == null) {
                errors.add(prefix + ".registrationAuthority", "请填写登记机关");
            }
            if (row.getJurisdiction() == null) {
                errors.add(prefix + ".jurisdiction", "请填写登记辖区");
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
                errors.add(prefix + ".validFrom", "请填写有效期开始日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            errors.add(prefix + ".unifiedSocialCreditCode",
                    IdentifierGuard.usccFormat(row.getJurisdiction(), row.getUnifiedSocialCreditCode()));
            errors.add(prefix + ".taxpayerIdentificationNumber",
                    IdentifierGuard.tinFormat(row.getJurisdiction(), row.getTaxpayerIdentificationNumber()));
            identifiers.checkUscc(errors, prefix + ".unifiedSocialCreditCode", row.getUnifiedSocialCreditCode(), null, form.getId());
            identifiers.checkTin(errors, prefix + ".taxpayerIdentificationNumber", row.getTaxpayerIdentificationNumber(), null, form.getId());
            identifiers.checkCertificate(errors, prefix + ".registrationCertificateNumber", row.getRegistrationCertificateNumber(), form.getId());
            documents.require(row.getSourceDocumentId(), errors, prefix + ".sourceDocumentId");
            froms.add(row.getValidFrom());
            tos.add(row.getValidTo());
        }
        errors.add("registrations", Periods.overlapError(froms, tos, "法人登记"));

        List<LocalDate> termFrom = new ArrayList<>();
        List<LocalDate> termTo = new ArrayList<>();
        for (int i = 0; i < form.getRepresentativeTerms().size(); i++) {
            RepresentativeForm row = form.getRepresentativeTerms().get(i);
            String prefix = "representativeTerms[" + i + "]";
            if (row.getPersonId() == null || persons.findById(row.getPersonId()).isEmpty()) {
                errors.add(prefix + ".personId", "请选择担任法定代表人的自然人");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写任期开始日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            documents.require(row.getBasisDocumentId(), errors, prefix + ".basisDocumentId");
            termFrom.add(row.getValidFrom());
            termTo.add(row.getValidTo());
        }
        errors.add("representativeTerms", Periods.overlapError(termFrom, termTo, "法定代表人任期"));
    }

    private Set<Long> splitRegistrations(Long legalPersonId, List<RegistrationForm> rows) {
        Set<Long> preserved = new HashSet<>();
        LocalDate today = LocalDate.now();
        for (RegistrationForm row : rows) {
            if (row.getId() == null || row.getValidTo() != null) {
                continue;
            }
            LegalPersonRegistration existing = registrations.findById(row.getId()).orElse(null);
            if (existing == null || !legalPersonId.equals(existing.legalPersonId)) {
                continue;
            }
            if (existing.validFrom != null && existing.validFrom.isBefore(today) && registrationChanged(existing, row)) {
                existing.validTo = today;
                registrations.save(existing);
                preserved.add(existing.id);
                row.setId(null);
                row.setValidFrom(today);
            }
        }
        return preserved;
    }

    private boolean registrationChanged(LegalPersonRegistration existing, RegistrationForm row) {
        return !Cmp.eq(existing.registrationType, row.getRegistrationType())
                || !Cmp.eq(existing.unifiedSocialCreditCode, row.getUnifiedSocialCreditCode())
                || !Cmp.eq(existing.registrationCertificateNumber, row.getRegistrationCertificateNumber())
                || !Cmp.eq(existing.taxpayerIdentificationNumber, row.getTaxpayerIdentificationNumber())
                || !Cmp.eq(existing.registrationAuthority, row.getRegistrationAuthority())
                || !Cmp.eq(existing.jurisdiction, row.getJurisdiction())
                || !Cmp.eq(existing.registeredName, row.getRegisteredName())
                || !Cmp.eq(existing.registeredAddress, row.getRegisteredAddress())
                || !Cmp.eq(existing.registrationStatus, row.getRegistrationStatus())
                || !Cmp.eq(existing.registeredAt, row.getRegisteredAt())
                || !Cmp.eq(existing.sourceDocumentId, row.getSourceDocumentId());
    }

    private void assertPeriods(List<RegistrationForm> rows, Set<Long> preserved, String label) {
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
        String message = Periods.overlapError(froms, tos, label);
        if (message != null) {
            throw new FieldErrorsException(java.util.Map.of("registrations", message));
        }
    }

    private void syncRegistrations(Long legalPersonId, List<RegistrationForm> rows, Set<Long> preserved) {
        Set<Long> keep = new HashSet<>(preserved);
        for (RegistrationForm row : rows) {
            LegalPersonRegistration entity;
            if (row.getId() != null) {
                entity = registrations.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(java.util.Map.of("*", "法人登记不存在")));
                if (!legalPersonId.equals(entity.legalPersonId)) {
                    throw new FieldErrorsException(java.util.Map.of("*", "法人登记不属于当前法人"));
                }
            } else {
                entity = new LegalPersonRegistration();
                entity.legalPersonId = legalPersonId;
            }
            entity.registrationType = row.getRegistrationType();
            entity.unifiedSocialCreditCode = row.getUnifiedSocialCreditCode();
            entity.registrationCertificateNumber = row.getRegistrationCertificateNumber();
            entity.taxpayerIdentificationNumber = row.getTaxpayerIdentificationNumber();
            entity.registrationAuthority = row.getRegistrationAuthority();
            entity.jurisdiction = row.getJurisdiction();
            entity.registeredName = row.getRegisteredName();
            entity.registeredAddress = row.getRegisteredAddress();
            entity.registrationStatus = row.getRegistrationStatus();
            entity.registeredAt = row.getRegisteredAt();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.sourceDocumentId = row.getSourceDocumentId();
            keep.add(registrations.save(entity).id);
        }
        for (LegalPersonRegistration existing : registrations.findByLegalPersonIdOrderByValidFromAsc(legalPersonId)) {
            if (!keep.contains(existing.id)) {
                registrations.delete(existing);
            }
        }
    }

    private void syncTerms(Long legalPersonId, List<RepresentativeForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (RepresentativeForm row : rows) {
            LegalRepresentativeTerm entity;
            if (row.getId() != null) {
                entity = terms.findById(row.getId()).orElseThrow(() ->
                        new FieldErrorsException(java.util.Map.of("*", "法定代表人任期不存在")));
                if (!legalPersonId.equals(entity.legalPersonId)) {
                    throw new FieldErrorsException(java.util.Map.of("*", "法定代表人任期不属于当前法人"));
                }
            } else {
                entity = new LegalRepresentativeTerm();
                entity.legalPersonId = legalPersonId;
            }
            entity.personId = row.getPersonId();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.basisDocumentId = row.getBasisDocumentId();
            keep.add(terms.save(entity).id);
        }
        for (LegalRepresentativeTerm existing : terms.findByLegalPersonIdOrderByValidFromAsc(legalPersonId)) {
            if (!keep.contains(existing.id)) {
                terms.delete(existing);
            }
        }
    }
}
