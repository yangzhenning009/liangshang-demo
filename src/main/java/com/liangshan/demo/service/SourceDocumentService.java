package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.SourceDocument;
import com.liangshan.demo.repository.CompanyRegistrationRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CompanyShareholdingRepository;
import com.liangshan.demo.repository.CompanyVersionRepository;
import com.liangshan.demo.repository.DepartmentServiceCompanyRepository;
import com.liangshan.demo.repository.DepartmentVersionRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LegalPersonRegistrationRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.LegalRepresentativeTermRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.SourceDocumentRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.SourceDocumentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SourceDocumentService {

    private final SourceDocumentRepository documents;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final CompanyRegistrationRepository companyRegistrations;
    private final CompanyShareholdingRepository shareholdings;
    private final CompanyVersionRepository companyVersions;
    private final LegalPersonRegistrationRepository legalPersonRegistrations;
    private final LegalRepresentativeTermRepository representativeTerms;
    private final DepartmentServiceCompanyRepository serviceCompanies;
    private final DepartmentVersionRepository departmentVersions;
    private final EmployeeEmploymentRepository employments;
    private final SupplierLinkGuard supplierLinks;
    private final ProjectLinkGuard projectLinks;
    private final ContractLinkGuard contractLinks;
    private final FulfillmentLinkGuard fulfillmentLinks;
    private final WorkLogLinkGuard workLogLinks;
    private final CostLinkGuard costLinks;

    public record Option(Long id, String label) {
    }

    public record Row(Long id, String documentType, String documentNumber, String issuer, String status, String issuedAt) {
    }

    public void require(Long id, FieldErrors errors, String field) {
        if (id != null && documents.findById(id).isEmpty()) {
            errors.add(field, "来源材料不存在");
        }
    }

    public List<Option> options() {
        return documents.findAllByOrderByIdDesc().stream()
                .map(document -> new Option(document.id, label(document)))
                .toList();
    }

    public List<Row> list() {
        Map<Long, Company> companyMap = companies.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, LegalPerson> legalMap = legalPersons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        Map<Long, Person> personMap = persons.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.id, item -> item));
        return documents.findAllByOrderByIdDesc().stream()
                .map(document -> new Row(
                        document.id,
                        document.documentType,
                        document.documentNumber,
                        issuerLabel(document, companyMap, legalMap, personMap),
                        document.status,
                        document.issuedAt == null ? null : document.issuedAt.toString()))
                .toList();
    }

    public SourceDocumentForm form(Long id) {
        SourceDocument document = documents.findById(id).orElseThrow(NotFoundException::new);
        SourceDocumentForm form = new SourceDocumentForm();
        form.setId(document.id);
        form.setDocumentType(document.documentType);
        form.setDocumentNumber(document.documentNumber);
        form.setIssuerCompanyId(document.issuerCompanyId);
        form.setIssuerLegalPersonId(document.issuerLegalPersonId);
        form.setIssuerPersonId(document.issuerPersonId);
        form.setPreparedByEmployeeId(document.preparedByEmployeeId);
        form.setIssuedAt(document.issuedAt);
        form.setFileStorageUri(document.fileStorageUri);
        form.setFileName(document.fileName);
        form.setMimeType(document.mimeType);
        form.setFileChecksum(document.fileChecksum);
        form.setUploadedAt(document.uploadedAt);
        form.setUploadedByEmployeeId(document.uploadedByEmployeeId);
        form.setContentSummary(document.contentSummary);
        form.setStatus(document.status);
        return form;
    }

    @Transactional
    public long save(SourceDocumentForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        int issuers = 0;
        if (form.getIssuerCompanyId() != null) {
            issuers++;
            if (companies.findById(form.getIssuerCompanyId()).isEmpty()) {
                errors.add("issuerCompanyId", "出具公司不存在");
            }
        }
        if (form.getIssuerLegalPersonId() != null) {
            issuers++;
            if (legalPersons.findById(form.getIssuerLegalPersonId()).isEmpty()) {
                errors.add("issuerLegalPersonId", "出具法人不存在");
            }
        }
        if (form.getIssuerPersonId() != null) {
            issuers++;
            if (persons.findById(form.getIssuerPersonId()).isEmpty()) {
                errors.add("issuerPersonId", "出具自然人不存在");
            }
        }
        if (issuers > 1) {
            errors.add("issuerCompanyId", "出具对象在公司、法人、自然人中最多选择一个");
        }
        if (form.getPreparedByEmployeeId() != null && employees.findById(form.getPreparedByEmployeeId()).isEmpty()) {
            errors.add("preparedByEmployeeId", "编制员工不存在");
        }
        if (form.getUploadedByEmployeeId() != null && employees.findById(form.getUploadedByEmployeeId()).isEmpty()) {
            errors.add("uploadedByEmployeeId", "上传员工不存在");
        }
        if (form.getStatus() == null || !Codes.DOCUMENT_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择材料状态");
        }
        errors.throwIfAny();

        SourceDocument document = form.getId() == null
                ? new SourceDocument()
                : documents.findById(form.getId()).orElseThrow(NotFoundException::new);
        document.documentType = form.getDocumentType();
        document.documentNumber = form.getDocumentNumber();
        document.issuerCompanyId = form.getIssuerCompanyId();
        document.issuerLegalPersonId = form.getIssuerLegalPersonId();
        document.issuerPersonId = form.getIssuerPersonId();
        document.preparedByEmployeeId = form.getPreparedByEmployeeId();
        document.issuedAt = form.getIssuedAt();
        document.fileStorageUri = form.getFileStorageUri();
        document.fileName = form.getFileName();
        document.mimeType = form.getMimeType();
        document.fileChecksum = form.getFileChecksum();
        document.uploadedAt = form.getUploadedAt();
        document.uploadedByEmployeeId = form.getUploadedByEmployeeId();
        document.contentSummary = form.getContentSummary();
        document.status = form.getStatus();
        return documents.save(document).id;
    }

    @Transactional
    public void delete(Long id) {
        documents.findById(id).orElseThrow(NotFoundException::new);
        if (referenced(id)) {
            throw new FieldErrorsException(Map.of("*", "该材料仍被业务记录引用，不能删除"));
        }
        documents.deleteById(id);
    }

    public String label(SourceDocument document) {
        String type = document.documentType == null ? "未分类" : document.documentType;
        String number = document.documentNumber == null ? "无编号" : document.documentNumber;
        return "#" + document.id + " " + type + " " + number;
    }

    private boolean referenced(Long id) {
        return companies.existsByBusinessLicenseDocumentId(id)
                || companyRegistrations.existsByBusinessLicenseDocumentId(id)
                || companyRegistrations.existsByTaxInformationSourceDocumentId(id)
                || companyRegistrations.existsBySourceDocumentId(id)
                || shareholdings.existsByBasisDocumentId(id)
                || companyVersions.existsByBasisDocumentId(id)
                || legalPersonRegistrations.existsBySourceDocumentId(id)
                || representativeTerms.existsByBasisDocumentId(id)
                || serviceCompanies.existsByBasisDocumentId(id)
                || departmentVersions.existsByBasisDocumentId(id)
                || employments.existsByEmploymentDocumentId(id)
                || supplierLinks.documentUsed(id)
                || projectLinks.documentUsed(id)
                || contractLinks.documentUsed(id)
                || fulfillmentLinks.documentUsed(id)
                || workLogLinks.documentUsed(id)
                || costLinks.documentUsed(id);
    }

    private void normalize(SourceDocumentForm form) {
        form.setDocumentType(Text.blankToNull(form.getDocumentType()));
        form.setDocumentNumber(Text.blankToNull(form.getDocumentNumber()));
        form.setFileStorageUri(Text.blankToNull(form.getFileStorageUri()));
        form.setFileName(Text.blankToNull(form.getFileName()));
        form.setMimeType(Text.blankToNull(form.getMimeType()));
        form.setFileChecksum(Text.blankToNull(form.getFileChecksum()));
        form.setContentSummary(Text.blankToNull(form.getContentSummary()));
        form.setStatus(Text.blankToNull(form.getStatus()));
    }

    private String issuerLabel(
            SourceDocument document,
            Map<Long, Company> companies,
            Map<Long, LegalPerson> legalPersons,
            Map<Long, Person> persons) {
        if (document.issuerCompanyId != null) {
            Company company = companies.get(document.issuerCompanyId);
            return company == null ? "未找到公司" : company.companyCode + " " + company.name;
        }
        if (document.issuerLegalPersonId != null) {
            LegalPerson legalPerson = legalPersons.get(document.issuerLegalPersonId);
            return legalPerson == null ? "未找到法人" : legalPerson.legalName;
        }
        if (document.issuerPersonId != null) {
            Person person = persons.get(document.issuerPersonId);
            return person == null ? "未找到自然人" : person.name;
        }
        return null;
    }

    public String employeeLabel(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        Employee employee = employees.findById(employeeId).orElse(null);
        if (employee == null) {
            return "未找到员工";
        }
        Person person = persons.findById(employee.personId).orElse(null);
        return employee.employeeCode + " " + (person == null ? "未找到自然人" : person.name);
    }
}
