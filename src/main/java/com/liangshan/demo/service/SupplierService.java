package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.domain.SupplierContact;
import com.liangshan.demo.domain.SupplierQualification;
import com.liangshan.demo.domain.SupplierSupplyCategory;
import com.liangshan.demo.domain.SupplierVersion;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.SupplierContactRepository;
import com.liangshan.demo.repository.SupplierQualificationRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.repository.SupplierSupplyCategoryRepository;
import com.liangshan.demo.repository.SupplierVersionRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.SupplierDetailForm;
import com.liangshan.demo.web.form.SupplierDetailForm.CategoryForm;
import com.liangshan.demo.web.form.SupplierDetailForm.ContactForm;
import com.liangshan.demo.web.form.SupplierDetailForm.QualificationForm;
import com.liangshan.demo.web.form.SupplierForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository suppliers;
    private final SupplierContactRepository contacts;
    private final SupplierSupplyCategoryRepository categories;
    private final SupplierQualificationRepository qualifications;
    private final SupplierVersionRepository versions;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final SourceDocumentService documents;
    private final SupplierReferenceGuard references;

    public record Row(
            Long id,
            String supplierCode,
            String supplierName,
            String purchasingCompany,
            String supplyingObject,
            String admissionStatus,
            String cooperationStatus) {
    }

    public record Header(
            Long id,
            String supplierCode,
            String supplierName,
            String purchasingCompany,
            String supplyingObject,
            String admissionStatus,
            String cooperationStatus,
            boolean referenced) {
    }

    public List<Row> list() {
        Map<Long, Company> companyMap = companies.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, LegalPerson> legalMap = legalPersons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Person> personMap = persons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        return suppliers.findAllByOrderBySupplierCodeAsc().stream()
                .map(supplier -> new Row(
                        supplier.id,
                        supplier.supplierCode,
                        supplier.supplierName,
                        companyLabel(companyMap.get(supplier.purchasingCompanyId)),
                        supplyingLabel(supplier, companyMap, legalMap, personMap),
                        supplier.admissionStatus,
                        supplier.cooperationStatus))
                .toList();
    }

    public Header header(Long id) {
        Supplier supplier = get(id);
        Map<Long, Company> companyMap = companies.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, LegalPerson> legalMap = legalPersons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Person> personMap = persons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        return new Header(
                supplier.id,
                supplier.supplierCode,
                supplier.supplierName,
                companyLabel(companyMap.get(supplier.purchasingCompanyId)),
                supplyingLabel(supplier, companyMap, legalMap, personMap),
                supplier.admissionStatus,
                supplier.cooperationStatus,
                references.referenced(id));
    }

    public boolean referenced(Long id) {
        return id != null && references.referenced(id);
    }

    public SupplierForm form(Long id) {
        Supplier supplier = get(id);
        SupplierForm form = new SupplierForm();
        form.setId(supplier.id);
        form.setSupplierCode(supplier.supplierCode);
        form.setSupplierName(supplier.supplierName);
        form.setPurchasingCompanyId(supplier.purchasingCompanyId);
        form.setCompanyId(supplier.companyId);
        form.setLegalPersonId(supplier.legalPersonId);
        form.setPersonId(supplier.personId);
        form.setProcurementOwnerEmployeeId(supplier.procurementOwnerEmployeeId);
        form.setAdmissionStatus(supplier.admissionStatus);
        form.setAdmissionSubmittedAt(supplier.admissionSubmittedAt);
        form.setAdmissionReviewedByEmployeeId(supplier.admissionReviewedByEmployeeId);
        form.setAdmissionReviewedAt(supplier.admissionReviewedAt);
        form.setAdmissionRejectionReason(supplier.admissionRejectionReason);
        form.setAdmissionDocumentId(supplier.admissionDocumentId);
        form.setCooperationStatus(supplier.cooperationStatus);
        form.setCooperationStartedOn(supplier.cooperationStartedOn);
        form.setCooperationEndedOn(supplier.cooperationEndedOn);
        form.setCooperationStatusEffectiveAt(supplier.cooperationStatusEffectiveAt);
        form.setCooperationStatusReason(supplier.cooperationStatusReason);
        form.setDefaultSettlementCurrency(supplier.defaultSettlementCurrency);
        form.setDefaultPaymentDueBasis(supplier.defaultPaymentDueBasis);
        form.setDefaultPaymentTermDays(supplier.defaultPaymentTermDays);
        form.setDefaultPaymentDayOfMonth(supplier.defaultPaymentDayOfMonth);
        versions.findBySupplierIdOrderByVersionNumberAsc(id).stream()
                .filter(version -> version.validTo == null)
                .findFirst()
                .ifPresent(version -> {
                    form.setChangedByEmployeeId(version.changedByEmployeeId);
                    form.setChangeReason(version.changeReason);
                    form.setBasisDocumentId(version.basisDocumentId);
                });
        return form;
    }

    public SupplierDetailForm detailForm(Long id) {
        get(id);
        SupplierDetailForm form = new SupplierDetailForm();
        form.setContacts(new java.util.ArrayList<>(contacts.findBySupplierIdOrderByIdAsc(id).stream().map(this::toContact).toList()));
        form.setCategories(new java.util.ArrayList<>(categories.findBySupplierIdOrderByIdAsc(id).stream().map(this::toCategory).toList()));
        form.setQualifications(new java.util.ArrayList<>(qualifications.findBySupplierIdOrderByIdAsc(id).stream().map(this::toQualification).toList()));
        return form;
    }

    public List<SupplierVersion> versionHistory(Long id) {
        get(id);
        return versions.findBySupplierIdOrderByVersionNumberAsc(id);
    }

    @Transactional
    public long save(SupplierForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        String previousCooperation = null;
        if (form.getId() != null) {
            previousCooperation = get(form.getId()).cooperationStatus;
        }
        validateHeader(form, previousCooperation, errors);
        errors.throwIfAny();

        Supplier supplier = form.getId() == null ? new Supplier() : get(form.getId());
        applyHeader(supplier, form);
        supplier = suppliers.save(supplier);
        recordVersion(supplier, form);
        return supplier.id;
    }

    @Transactional
    public void saveDetails(Long supplierId, SupplierDetailForm form) {
        get(supplierId);
        normalizeDetails(form);
        FieldErrors errors = new FieldErrors();
        validateDetails(form, errors);
        errors.throwIfAny();
        syncContacts(supplierId, form.getContacts());
        syncCategories(supplierId, form.getCategories());
        syncQualifications(supplierId, form.getQualifications());
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (references.referenced(id)) {
            throw new FieldErrorsException(Map.of("*", "该供应商已被业务引用，不能删除"));
        }
        contacts.findBySupplierIdOrderByIdAsc(id).forEach(contacts::delete);
        categories.findBySupplierIdOrderByIdAsc(id).forEach(categories::delete);
        qualifications.findBySupplierIdOrderByIdAsc(id).forEach(qualifications::delete);
        versions.findBySupplierIdOrderByVersionNumberAsc(id).forEach(versions::delete);
        suppliers.deleteById(id);
    }

    private void validateHeader(SupplierForm form, String previousCooperation, FieldErrors errors) {
        if (form.getSupplierCode() == null) {
            errors.add("supplierCode", "请填写供应商编号");
        }
        if (form.getSupplierName() == null) {
            errors.add("supplierName", "请填写显示名称");
        }
        if (form.getPurchasingCompanyId() == null) {
            errors.add("purchasingCompanyId", "请选择采购公司");
        } else if (companies.findById(form.getPurchasingCompanyId()).isEmpty()) {
            errors.add("purchasingCompanyId", "采购公司不存在");
        }
        int objects = 0;
        if (form.getCompanyId() != null) {
            objects++;
            if (companies.findById(form.getCompanyId()).isEmpty()) {
                errors.add("companyId", "供货公司不存在");
            }
        }
        if (form.getLegalPersonId() != null) {
            objects++;
            if (legalPersons.findById(form.getLegalPersonId()).isEmpty()) {
                errors.add("legalPersonId", "供货法人不存在");
            }
        }
        if (form.getPersonId() != null) {
            objects++;
            if (persons.findById(form.getPersonId()).isEmpty()) {
                errors.add("personId", "供货自然人不存在");
            }
        }
        if (objects != 1) {
            errors.add("companyId", "确认档案时，供货对象须在公司、法人、自然人中恰好选择一个");
        }
        if (form.getId() != null && references.referenced(form.getId())) {
            Supplier stored = get(form.getId());
            if (!Cmp.eq(stored.purchasingCompanyId, form.getPurchasingCompanyId())
                    || !Cmp.eq(stored.companyId, form.getCompanyId())
                    || !Cmp.eq(stored.legalPersonId, form.getLegalPersonId())
                    || !Cmp.eq(stored.personId, form.getPersonId())) {
                errors.add("purchasingCompanyId", "该供应商已被业务引用，采购公司和供货对象不能更换");
            }
        }
        if (form.getPurchasingCompanyId() != null && objects == 1) {
            assertUniqueArchive(form, errors);
        }
        requireEmployee(form.getProcurementOwnerEmployeeId(), errors, "procurementOwnerEmployeeId", "采购负责人");
        requireEmployee(form.getAdmissionReviewedByEmployeeId(), errors, "admissionReviewedByEmployeeId", "准入审核人");
        requireEmployee(form.getChangedByEmployeeId(), errors, "changedByEmployeeId", "变更人");
        documents.require(form.getAdmissionDocumentId(), errors, "admissionDocumentId");
        documents.require(form.getBasisDocumentId(), errors, "basisDocumentId");
        validateAdmission(form, errors);
        validateCooperation(form, previousCooperation, errors);
        validatePayment(form, errors);
    }

    private void assertUniqueArchive(SupplierForm form, FieldErrors errors) {
        List<Supplier> sameBuyer = suppliers.findByPurchasingCompanyId(form.getPurchasingCompanyId());
        for (Supplier other : sameBuyer) {
            if (other.id.equals(form.getId())) {
                continue;
            }
            if (form.getSupplierCode() != null && form.getSupplierCode().equals(other.supplierCode)) {
                errors.add("supplierCode", "供应商编号在该采购公司内已存在");
            }
            if (Cmp.eq(form.getCompanyId(), other.companyId)
                    && Cmp.eq(form.getLegalPersonId(), other.legalPersonId)
                    && Cmp.eq(form.getPersonId(), other.personId)) {
                errors.add("companyId", "同一采购公司与同一供货对象只维护一份档案");
            }
        }
        if (form.getCompanyId() != null) {
            Company company = companies.findById(form.getCompanyId()).orElse(null);
            if (company != null && company.legalPersonId != null) {
                for (Supplier other : sameBuyer) {
                    if (!other.id.equals(form.getId()) && company.legalPersonId.equals(other.legalPersonId)) {
                        errors.add("legalPersonId", "公司供应商不能再以其对应法人重复建档");
                    }
                }
            }
        }
        if (form.getLegalPersonId() != null) {
            for (Company company : companies.findByLegalPersonId(form.getLegalPersonId())) {
                for (Supplier other : sameBuyer) {
                    if (!other.id.equals(form.getId()) && company.id.equals(other.companyId)) {
                        errors.add("companyId", "公司供应商不能再以其对应法人重复建档");
                    }
                }
            }
        }
    }

    private void validateAdmission(SupplierForm form, FieldErrors errors) {
        String status = form.getAdmissionStatus();
        if (status == null || !Codes.ADMISSION_STATUS.contains(status)) {
            errors.add("admissionStatus", "请选择准入状态");
            return;
        }
        boolean submitted = "审核中".equals(status) || "通过".equals(status) || "驳回".equals(status);
        if (submitted && form.getAdmissionSubmittedAt() == null) {
            errors.add("admissionSubmittedAt", "准入进入审核后须填写提交时间");
        }
        boolean decided = "通过".equals(status) || "驳回".equals(status);
        if (decided) {
            if (form.getAdmissionReviewedByEmployeeId() == null || form.getAdmissionReviewedAt() == null) {
                errors.add("admissionReviewedByEmployeeId", "准入审核完成时须填写审核人和审核时间");
            }
        }
        if ("通过".equals(status)) {
            if (form.getProcurementOwnerEmployeeId() == null) {
                errors.add("procurementOwnerEmployeeId", "准入通过时须填写采购负责人");
            }
            if (form.getAdmissionRejectionReason() != null) {
                errors.add("admissionRejectionReason", "准入通过时驳回原因留空");
            }
        }
        if ("驳回".equals(status) && form.getAdmissionRejectionReason() == null) {
            errors.add("admissionRejectionReason", "准入驳回时须填写原因");
        }
        if (!"驳回".equals(status) && form.getAdmissionRejectionReason() != null) {
            errors.add("admissionRejectionReason", "只有驳回时填写驳回原因");
        }
    }

    private void validateCooperation(SupplierForm form, String previousCooperation, FieldErrors errors) {
        String status = form.getCooperationStatus();
        if (status == null || !Codes.COOPERATION_STATUS.contains(status)) {
            errors.add("cooperationStatus", "请选择合作状态");
            return;
        }
        if (form.getCooperationStatusEffectiveAt() == null) {
            errors.add("cooperationStatusEffectiveAt", "请填写合作状态生效时间");
        }
        boolean active = "合作中".equals(status) || "暂停".equals(status) || "终止".equals(status);
        if (active && form.getCooperationStartedOn() == null) {
            errors.add("cooperationStartedOn", "合作中、暂停或终止时须填写合作开始日");
        }
        if ("终止".equals(status)) {
            if (form.getCooperationEndedOn() == null) {
                errors.add("cooperationEndedOn", "终止时须填写结束日");
            } else if (form.getCooperationStartedOn() != null
                    && form.getCooperationEndedOn().isBefore(form.getCooperationStartedOn())) {
                errors.add("cooperationEndedOn", "结束日不能早于开始日");
            }
        } else if (form.getCooperationEndedOn() != null) {
            errors.add("cooperationEndedOn", "尚未终止时结束日留空");
        }
        if ("未启用".equals(status)) {
            if (form.getCooperationStartedOn() != null || form.getCooperationStatusReason() != null) {
                errors.add("cooperationStatus", "未启用时合作起止和原因留空");
            }
        }
        boolean resume = "暂停".equals(previousCooperation) && "合作中".equals(status);
        if (("暂停".equals(status) || "终止".equals(status) || resume) && form.getCooperationStatusReason() == null) {
            errors.add("cooperationStatusReason", "暂停、终止或从暂停恢复合作时须填写原因");
        }
    }

    private void validatePayment(SupplierForm form, FieldErrors errors) {
        String basis = form.getDefaultPaymentDueBasis();
        Integer days = form.getDefaultPaymentTermDays();
        Integer day = form.getDefaultPaymentDayOfMonth();
        if (basis != null && !Codes.PAYMENT_DUE_BASIS.contains(basis)) {
            errors.add("defaultPaymentDueBasis", "请选择默认付款到期规则");
            return;
        }
        if (basis == null || "合同另定".equals(basis)) {
            if (days != null || day != null) {
                errors.add("defaultPaymentTermDays", "未选择基准或合同另定时，账期天数和固定日都留空");
            }
            return;
        }
        if ("发票确认日".equals(basis) || "验收日".equals(basis)) {
            if (days == null || days < 0 || day != null) {
                errors.add("defaultPaymentTermDays", "采用发票确认日或验收日时，须填写不小于 0 的账期天数，固定日留空");
            }
            return;
        }
        if (day == null || day < 1 || day > 31 || days != null) {
            errors.add("defaultPaymentDayOfMonth", "采用约定固定日时，须填写 1 到 31 的日期，账期天数留空");
        }
    }

    private void validateDetails(SupplierDetailForm form, FieldErrors errors) {
        List<ContactForm> contactRows = form.getContacts().stream().filter(row -> !row.isRemoved()).toList();
        for (int i = 0; i < form.getContacts().size(); i++) {
            ContactForm row = form.getContacts().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "contacts[" + i + "]";
            if (row.getPersonId() == null) {
                errors.add(prefix + ".personId", "请选择联系人");
            } else if (persons.findById(row.getPersonId()).isEmpty()) {
                errors.add(prefix + ".personId", "联系自然人不存在");
            }
            if (row.getContactRole() == null || !Codes.CONTACT_ROLES.contains(row.getContactRole())) {
                errors.add(prefix + ".contactRole", "请选择联系人角色");
            }
            if (row.getStatus() == null || !Codes.CONTACT_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择联系人状态");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写联系人生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            if (!Text.emailOk(row.getBusinessEmail())) {
                errors.add(prefix + ".businessEmail", "邮箱格式不正确");
            }
            if ("有效".equals(row.getStatus()) && row.getBusinessPhone() == null && row.getBusinessEmail() == null) {
                errors.add(prefix + ".businessPhone", "有效联系人至少填写电话或邮箱");
            }
        }
        for (int i = 0; i < contactRows.size(); i++) {
            ContactForm left = contactRows.get(i);
            if (!Boolean.TRUE.equals(left.getPrimary()) || !"有效".equals(left.getStatus())) {
                continue;
            }
            for (int j = i + 1; j < contactRows.size(); j++) {
                ContactForm right = contactRows.get(j);
                if (!Boolean.TRUE.equals(right.getPrimary()) || !"有效".equals(right.getStatus())) {
                    continue;
                }
                if (Cmp.eq(left.getContactRole(), right.getContactRole())
                        && Periods.overlaps(left.getValidFrom(), left.getValidTo(), right.getValidFrom(), right.getValidTo())) {
                    errors.add("contacts", "同一角色在重叠期间只能有一个主联系人");
                }
            }
        }
        for (int i = 0; i < form.getCategories().size(); i++) {
            CategoryForm row = form.getCategories().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "categories[" + i + "]";
            if (row.getSupplyCategoryCode() == null) {
                errors.add(prefix + ".supplyCategoryCode", "请填写供货类别编号");
            }
            if (row.getSupplyCategoryName() == null) {
                errors.add(prefix + ".supplyCategoryName", "请填写供货类别名称");
            }
            if (row.getSupplyScopeDescription() == null) {
                errors.add(prefix + ".supplyScopeDescription", "请填写供货范围");
            }
            if (row.getValidFrom() == null) {
                errors.add(prefix + ".validFrom", "请填写类别生效日");
            }
            errors.add(prefix + ".validTo", Periods.orderError(row.getValidFrom(), row.getValidTo()));
            if (row.getStatus() == null || !Codes.SUPPLY_CATEGORY_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择类别状态");
            }
        }
        for (int i = 0; i < form.getQualifications().size(); i++) {
            QualificationForm row = form.getQualifications().get(i);
            if (row.isRemoved()) {
                continue;
            }
            String prefix = "qualifications[" + i + "]";
            if (row.getQualificationName() == null) {
                errors.add(prefix + ".qualificationName", "请填写资质名称");
            }
            if (row.getNoExpiry() == null) {
                errors.add(prefix + ".noExpiry", "请标明资质是否长期有效");
            } else if (Boolean.TRUE.equals(row.getNoExpiry()) && row.getExpiresOn() != null) {
                errors.add(prefix + ".expiresOn", "标明长期有效时，到期日必须留空");
            } else if (Boolean.FALSE.equals(row.getNoExpiry())
                    && row.getExpiresOn() != null
                    && row.getIssuedOn() != null
                    && row.getExpiresOn().isBefore(row.getIssuedOn())) {
                errors.add(prefix + ".expiresOn", "到期日不能早于发证日");
            }
            if (row.getQualificationDocumentId() == null) {
                errors.add(prefix + ".qualificationDocumentId", "请选择资质材料");
            } else {
                documents.require(row.getQualificationDocumentId(), errors, prefix + ".qualificationDocumentId");
            }
            if (row.getReviewStatus() == null || !Codes.QUALIFICATION_REVIEW_STATUS.contains(row.getReviewStatus())) {
                errors.add(prefix + ".reviewStatus", "请选择资质审核状态");
            } else if ("通过".equals(row.getReviewStatus()) || "驳回".equals(row.getReviewStatus())) {
                if (row.getReviewedByEmployeeId() == null || row.getReviewedAt() == null) {
                    errors.add(prefix + ".reviewedByEmployeeId", "资质审核完成时须填写审核人和审核时间");
                }
            }
            requireEmployee(row.getReviewedByEmployeeId(), errors, prefix + ".reviewedByEmployeeId", "资质审核人");
            if ("驳回".equals(row.getReviewStatus()) && row.getRejectionReason() == null) {
                errors.add(prefix + ".rejectionReason", "资质驳回时须填写原因");
            }
            if (!"驳回".equals(row.getReviewStatus()) && row.getRejectionReason() != null) {
                errors.add(prefix + ".rejectionReason", "只有驳回时填写驳回原因");
            }
            if (row.getStatus() == null || !Codes.QUALIFICATION_STATUS.contains(row.getStatus())) {
                errors.add(prefix + ".status", "请选择资质状态");
            }
        }
    }

    private void applyHeader(Supplier supplier, SupplierForm form) {
        supplier.supplierCode = form.getSupplierCode();
        supplier.supplierName = form.getSupplierName();
        supplier.purchasingCompanyId = form.getPurchasingCompanyId();
        supplier.companyId = form.getCompanyId();
        supplier.legalPersonId = form.getLegalPersonId();
        supplier.personId = form.getPersonId();
        supplier.procurementOwnerEmployeeId = form.getProcurementOwnerEmployeeId();
        supplier.admissionStatus = form.getAdmissionStatus();
        supplier.admissionSubmittedAt = minute(form.getAdmissionSubmittedAt());
        supplier.admissionReviewedByEmployeeId = form.getAdmissionReviewedByEmployeeId();
        supplier.admissionReviewedAt = minute(form.getAdmissionReviewedAt());
        supplier.admissionRejectionReason = form.getAdmissionRejectionReason();
        supplier.admissionDocumentId = form.getAdmissionDocumentId();
        supplier.cooperationStatus = form.getCooperationStatus();
        supplier.cooperationStartedOn = form.getCooperationStartedOn();
        supplier.cooperationEndedOn = form.getCooperationEndedOn();
        supplier.cooperationStatusEffectiveAt = minute(form.getCooperationStatusEffectiveAt());
        supplier.cooperationStatusReason = form.getCooperationStatusReason();
        supplier.defaultSettlementCurrency = form.getDefaultSettlementCurrency();
        supplier.defaultPaymentDueBasis = form.getDefaultPaymentDueBasis();
        supplier.defaultPaymentTermDays = form.getDefaultPaymentTermDays();
        supplier.defaultPaymentDayOfMonth = form.getDefaultPaymentDayOfMonth();
    }

    private void recordVersion(Supplier supplier, SupplierForm form) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDate today = LocalDate.now();
        List<SupplierVersion> existing = versions.findBySupplierIdOrderByVersionNumberAsc(supplier.id);
        SupplierVersion open = existing.stream().filter(version -> version.validTo == null).findFirst().orElse(null);
        if (open == null) {
            SupplierVersion created = new SupplierVersion();
            created.supplierId = supplier.id;
            created.versionNumber = nextNumber(existing);
            copySnapshot(supplier, created);
            created.validFrom = supplier.cooperationStatusEffectiveAt.toLocalDate();
            created.recordedAt = now;
            applyMeta(created, form);
            versions.save(created);
            return;
        }
        if (!snapshotChanged(open, supplier)) {
            applyMeta(open, form);
            versions.save(open);
            return;
        }
        if (open.validFrom != null && open.validFrom.isBefore(today)) {
            open.validTo = today;
            versions.save(open);
            SupplierVersion next = new SupplierVersion();
            next.supplierId = supplier.id;
            next.versionNumber = nextNumber(existing);
            copySnapshot(supplier, next);
            next.validFrom = today;
            next.recordedAt = now;
            applyMeta(next, form);
            versions.save(next);
            return;
        }
        copySnapshot(supplier, open);
        open.recordedAt = now;
        applyMeta(open, form);
        versions.save(open);
    }

    private int nextNumber(List<SupplierVersion> existing) {
        int max = 0;
        for (SupplierVersion version : existing) {
            if (version.versionNumber != null) {
                max = Math.max(max, version.versionNumber);
            }
        }
        return max + 1;
    }

    private void applyMeta(SupplierVersion version, SupplierForm form) {
        version.changedByEmployeeId = form.getChangedByEmployeeId();
        version.changeReason = form.getChangeReason();
        version.basisDocumentId = form.getBasisDocumentId();
    }

    private boolean snapshotChanged(SupplierVersion version, Supplier supplier) {
        return !Cmp.eq(version.supplierName, supplier.supplierName)
                || !Cmp.eq(version.procurementOwnerEmployeeId, supplier.procurementOwnerEmployeeId)
                || !Cmp.eq(version.admissionStatus, supplier.admissionStatus)
                || !Cmp.eq(version.admissionSubmittedAt, supplier.admissionSubmittedAt)
                || !Cmp.eq(version.admissionReviewedByEmployeeId, supplier.admissionReviewedByEmployeeId)
                || !Cmp.eq(version.admissionReviewedAt, supplier.admissionReviewedAt)
                || !Cmp.eq(version.admissionRejectionReason, supplier.admissionRejectionReason)
                || !Cmp.eq(version.admissionDocumentId, supplier.admissionDocumentId)
                || !Cmp.eq(version.cooperationStatus, supplier.cooperationStatus)
                || !Cmp.eq(version.cooperationStartedOn, supplier.cooperationStartedOn)
                || !Cmp.eq(version.cooperationEndedOn, supplier.cooperationEndedOn)
                || !Cmp.eq(version.cooperationStatusEffectiveAt, supplier.cooperationStatusEffectiveAt)
                || !Cmp.eq(version.cooperationStatusReason, supplier.cooperationStatusReason)
                || !Cmp.eq(version.defaultSettlementCurrency, supplier.defaultSettlementCurrency)
                || !Cmp.eq(version.defaultPaymentDueBasis, supplier.defaultPaymentDueBasis)
                || !Cmp.eq(version.defaultPaymentTermDays, supplier.defaultPaymentTermDays)
                || !Cmp.eq(version.defaultPaymentDayOfMonth, supplier.defaultPaymentDayOfMonth);
    }

    private void copySnapshot(Supplier supplier, SupplierVersion version) {
        version.supplierName = supplier.supplierName;
        version.procurementOwnerEmployeeId = supplier.procurementOwnerEmployeeId;
        version.admissionStatus = supplier.admissionStatus;
        version.admissionSubmittedAt = supplier.admissionSubmittedAt;
        version.admissionReviewedByEmployeeId = supplier.admissionReviewedByEmployeeId;
        version.admissionReviewedAt = supplier.admissionReviewedAt;
        version.admissionRejectionReason = supplier.admissionRejectionReason;
        version.admissionDocumentId = supplier.admissionDocumentId;
        version.cooperationStatus = supplier.cooperationStatus;
        version.cooperationStartedOn = supplier.cooperationStartedOn;
        version.cooperationEndedOn = supplier.cooperationEndedOn;
        version.cooperationStatusEffectiveAt = supplier.cooperationStatusEffectiveAt;
        version.cooperationStatusReason = supplier.cooperationStatusReason;
        version.defaultSettlementCurrency = supplier.defaultSettlementCurrency;
        version.defaultPaymentDueBasis = supplier.defaultPaymentDueBasis;
        version.defaultPaymentTermDays = supplier.defaultPaymentTermDays;
        version.defaultPaymentDayOfMonth = supplier.defaultPaymentDayOfMonth;
    }

    private void syncContacts(Long supplierId, List<ContactForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (ContactForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            SupplierContact entity = loadContact(supplierId, row.getId());
            entity.supplierId = supplierId;
            entity.personId = row.getPersonId();
            entity.contactRole = row.getContactRole();
            entity.businessPhone = row.getBusinessPhone();
            entity.businessEmail = row.getBusinessEmail();
            entity.primary = Boolean.TRUE.equals(row.getPrimary());
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.status = row.getStatus();
            keep.add(contacts.save(entity).id);
        }
        for (SupplierContact existing : contacts.findBySupplierIdOrderByIdAsc(supplierId)) {
            if (!keep.contains(existing.id)) {
                contacts.delete(existing);
            }
        }
    }

    private void syncCategories(Long supplierId, List<CategoryForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (CategoryForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            SupplierSupplyCategory entity = loadCategory(supplierId, row.getId());
            entity.supplierId = supplierId;
            entity.supplyCategoryCode = row.getSupplyCategoryCode();
            entity.supplyCategoryName = row.getSupplyCategoryName();
            entity.supplyScopeDescription = row.getSupplyScopeDescription();
            entity.validFrom = row.getValidFrom();
            entity.validTo = row.getValidTo();
            entity.status = row.getStatus();
            keep.add(categories.save(entity).id);
        }
        for (SupplierSupplyCategory existing : categories.findBySupplierIdOrderByIdAsc(supplierId)) {
            if (!keep.contains(existing.id)) {
                categories.delete(existing);
            }
        }
    }

    private void syncQualifications(Long supplierId, List<QualificationForm> rows) {
        Set<Long> keep = new HashSet<>();
        for (QualificationForm row : rows) {
            if (row.isRemoved()) {
                continue;
            }
            SupplierQualification entity = loadQualification(supplierId, row.getId());
            entity.supplierId = supplierId;
            entity.qualificationName = row.getQualificationName();
            entity.certificateNumber = row.getCertificateNumber();
            entity.issuingAuthority = row.getIssuingAuthority();
            entity.issuedOn = row.getIssuedOn();
            entity.expiresOn = row.getExpiresOn();
            entity.noExpiry = Boolean.TRUE.equals(row.getNoExpiry());
            entity.qualificationDocumentId = row.getQualificationDocumentId();
            entity.reviewStatus = row.getReviewStatus();
            entity.reviewedByEmployeeId = row.getReviewedByEmployeeId();
            entity.reviewedAt = minute(row.getReviewedAt());
            entity.rejectionReason = row.getRejectionReason();
            entity.status = row.getStatus();
            keep.add(qualifications.save(entity).id);
        }
        for (SupplierQualification existing : qualifications.findBySupplierIdOrderByIdAsc(supplierId)) {
            if (!keep.contains(existing.id)) {
                qualifications.delete(existing);
            }
        }
    }

    private SupplierContact loadContact(Long supplierId, Long id) {
        if (id == null) {
            return new SupplierContact();
        }
        SupplierContact entity = contacts.findById(id).orElseThrow(() ->
                new FieldErrorsException(Map.of("*", "联系人不存在")));
        if (!supplierId.equals(entity.supplierId)) {
            throw new FieldErrorsException(Map.of("*", "联系人不属于当前供应商"));
        }
        return entity;
    }

    private SupplierSupplyCategory loadCategory(Long supplierId, Long id) {
        if (id == null) {
            return new SupplierSupplyCategory();
        }
        SupplierSupplyCategory entity = categories.findById(id).orElseThrow(() ->
                new FieldErrorsException(Map.of("*", "供货类别不存在")));
        if (!supplierId.equals(entity.supplierId)) {
            throw new FieldErrorsException(Map.of("*", "供货类别不属于当前供应商"));
        }
        return entity;
    }

    private SupplierQualification loadQualification(Long supplierId, Long id) {
        if (id == null) {
            return new SupplierQualification();
        }
        SupplierQualification entity = qualifications.findById(id).orElseThrow(() ->
                new FieldErrorsException(Map.of("*", "资质不存在")));
        if (!supplierId.equals(entity.supplierId)) {
            throw new FieldErrorsException(Map.of("*", "资质不属于当前供应商"));
        }
        return entity;
    }

    private ContactForm toContact(SupplierContact row) {
        ContactForm form = new ContactForm();
        form.setId(row.id);
        form.setPersonId(row.personId);
        form.setContactRole(row.contactRole);
        form.setBusinessPhone(row.businessPhone);
        form.setBusinessEmail(row.businessEmail);
        form.setPrimary(row.primary);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setStatus(row.status);
        return form;
    }

    private CategoryForm toCategory(SupplierSupplyCategory row) {
        CategoryForm form = new CategoryForm();
        form.setId(row.id);
        form.setSupplyCategoryCode(row.supplyCategoryCode);
        form.setSupplyCategoryName(row.supplyCategoryName);
        form.setSupplyScopeDescription(row.supplyScopeDescription);
        form.setValidFrom(row.validFrom);
        form.setValidTo(row.validTo);
        form.setStatus(row.status);
        return form;
    }

    private QualificationForm toQualification(SupplierQualification row) {
        QualificationForm form = new QualificationForm();
        form.setId(row.id);
        form.setQualificationName(row.qualificationName);
        form.setCertificateNumber(row.certificateNumber);
        form.setIssuingAuthority(row.issuingAuthority);
        form.setIssuedOn(row.issuedOn);
        form.setExpiresOn(row.expiresOn);
        form.setNoExpiry(row.noExpiry);
        form.setQualificationDocumentId(row.qualificationDocumentId);
        form.setReviewStatus(row.reviewStatus);
        form.setReviewedByEmployeeId(row.reviewedByEmployeeId);
        form.setReviewedAt(row.reviewedAt);
        form.setRejectionReason(row.rejectionReason);
        form.setStatus(row.status);
        return form;
    }

    private void normalize(SupplierForm form) {
        form.setSupplierCode(Text.blankToNull(form.getSupplierCode()));
        form.setSupplierName(Text.blankToNull(form.getSupplierName()));
        form.setAdmissionStatus(Text.blankToNull(form.getAdmissionStatus()));
        form.setAdmissionRejectionReason(Text.blankToNull(form.getAdmissionRejectionReason()));
        form.setCooperationStatus(Text.blankToNull(form.getCooperationStatus()));
        form.setCooperationStatusReason(Text.blankToNull(form.getCooperationStatusReason()));
        form.setDefaultSettlementCurrency(Text.blankToNull(form.getDefaultSettlementCurrency()));
        form.setDefaultPaymentDueBasis(Text.blankToNull(form.getDefaultPaymentDueBasis()));
        form.setChangeReason(Text.blankToNull(form.getChangeReason()));
    }

    private void normalizeDetails(SupplierDetailForm form) {
        if (form.getContacts() == null) {
            form.setContacts(List.of());
        }
        if (form.getCategories() == null) {
            form.setCategories(List.of());
        }
        if (form.getQualifications() == null) {
            form.setQualifications(List.of());
        }
        for (ContactForm row : form.getContacts()) {
            row.setContactRole(Text.blankToNull(row.getContactRole()));
            row.setBusinessPhone(Text.blankToNull(row.getBusinessPhone()));
            row.setBusinessEmail(Text.blankToNull(row.getBusinessEmail()));
            row.setStatus(Text.blankToNull(row.getStatus()));
        }
        for (CategoryForm row : form.getCategories()) {
            row.setSupplyCategoryCode(Text.blankToNull(row.getSupplyCategoryCode()));
            row.setSupplyCategoryName(Text.blankToNull(row.getSupplyCategoryName()));
            row.setSupplyScopeDescription(Text.blankToNull(row.getSupplyScopeDescription()));
            row.setStatus(Text.blankToNull(row.getStatus()));
        }
        for (QualificationForm row : form.getQualifications()) {
            row.setQualificationName(Text.blankToNull(row.getQualificationName()));
            row.setCertificateNumber(Text.blankToNull(row.getCertificateNumber()));
            row.setIssuingAuthority(Text.blankToNull(row.getIssuingAuthority()));
            row.setReviewStatus(Text.blankToNull(row.getReviewStatus()));
            row.setRejectionReason(Text.blankToNull(row.getRejectionReason()));
            row.setStatus(Text.blankToNull(row.getStatus()));
        }
    }

    private void requireEmployee(Long id, FieldErrors errors, String field, String label) {
        if (id != null && employees.findById(id).isEmpty()) {
            errors.add(field, label + "不存在");
        }
    }

    private Supplier get(Long id) {
        return suppliers.findById(id).orElseThrow(NotFoundException::new);
    }

    private String companyLabel(Company company) {
        return company == null ? "未找到公司" : company.companyCode + " " + company.name;
    }

    private String supplyingLabel(
            Supplier supplier,
            Map<Long, Company> companies,
            Map<Long, LegalPerson> legalPersons,
            Map<Long, Person> persons) {
        if (supplier.companyId != null) {
            Company company = companies.get(supplier.companyId);
            return company == null ? "公司 #" + supplier.companyId : "公司 " + company.companyCode + " " + company.name;
        }
        if (supplier.legalPersonId != null) {
            LegalPerson legalPerson = legalPersons.get(supplier.legalPersonId);
            return legalPerson == null ? "法人 #" + supplier.legalPersonId : "法人 " + legalPerson.legalName;
        }
        if (supplier.personId != null) {
            Person person = persons.get(supplier.personId);
            return person == null ? "自然人 #" + supplier.personId : "自然人 " + person.name;
        }
        return "未指定";
    }

    private LocalDateTime minute(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }
}
