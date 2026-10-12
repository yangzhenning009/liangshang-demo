package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.domain.LaborCostRate;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LaborCostRateRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.WorkLogRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.LaborCostRateForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LaborCostRateService {

    private final LaborCostRateRepository rates;
    private final WorkLogRepository logs;
    private final EmployeeEmploymentRepository employments;
    private final EmployeeRepository employees;
    private final PersonRepository persons;
    private final SourceDocumentService documents;

    public record Row(
            Long id,
            String employmentLabel,
            String costBasis,
            BigDecimal hourlyCostAmount,
            String currency,
            Integer rateVersionNumber,
            java.time.LocalDate validFrom,
            java.time.LocalDate validTo,
            String approvalStatus) {
    }

    public List<Row> list() {
        List<Row> rows = new ArrayList<>();
        for (LaborCostRate rate : rates.findAllByOrderByIdDesc()) {
            rows.add(new Row(
                    rate.id,
                    employmentLabel(rate.employmentId),
                    rate.costBasis,
                    plain(rate.hourlyCostAmount),
                    rate.currency,
                    rate.rateVersionNumber,
                    rate.validFrom,
                    rate.validTo,
                    rate.approvalStatus));
        }
        return rows;
    }

    public LaborCostRate get(Long id) {
        return rates.findById(id).orElseThrow(NotFoundException::new);
    }

    public LaborCostRateForm form(Long id) {
        LaborCostRate rate = get(id);
        LaborCostRateForm form = new LaborCostRateForm();
        form.setId(rate.id);
        form.setEmploymentId(rate.employmentId);
        form.setCostBasis(rate.costBasis);
        form.setHourlyCostAmount(rate.hourlyCostAmount);
        form.setCurrency(rate.currency);
        form.setRateVersionNumber(rate.rateVersionNumber);
        form.setValidFrom(rate.validFrom);
        form.setValidTo(rate.validTo);
        form.setApprovalStatus(rate.approvalStatus);
        form.setApprovedByEmployeeId(rate.approvedByEmployeeId);
        form.setApprovedAt(rate.approvedAt);
        form.setBasisDocumentId(rate.basisDocumentId);
        return form;
    }

    public String employmentLabel(Long employmentId) {
        EmployeeEmployment employment = employments.findById(employmentId).orElse(null);
        if (employment == null) {
            return "雇佣 #" + employmentId;
        }
        Employee employee = employees.findById(employment.employeeId).orElse(null);
        String who = "员工";
        if (employee != null) {
            Person person = persons.findById(employee.personId).orElse(null);
            who = employee.employeeCode + (person == null ? "" : " " + person.name);
        }
        return who + " · " + employment.employeeNumber;
    }

    @Transactional
    public long save(LaborCostRateForm form) {
        normalize(form);
        FieldErrors errors = new FieldErrors();
        EmployeeEmployment employment = form.getEmploymentId() == null
                ? null
                : employments.findById(form.getEmploymentId()).orElse(null);
        if (employment == null) {
            errors.add("employmentId", "请选择雇佣");
        }
        if (form.getCostBasis() == null || !Codes.LABOR_COST_BASIS.contains(form.getCostBasis())) {
            errors.add("costBasis", "请选择成本口径");
        }
        if (form.getHourlyCostAmount() == null) {
            errors.add("hourlyCostAmount", "请填写每小时成本");
        } else if (form.getHourlyCostAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("hourlyCostAmount", "每小时成本须大于 0");
        }
        if (form.getCurrency() == null) {
            errors.add("currency", "请填写币种");
        } else if (form.getCurrency().length() > 8) {
            errors.add("currency", "币种过长");
        }
        if (form.getValidFrom() == null) {
            errors.add("validFrom", "请填写开始日");
        } else {
            errors.add("validTo", Periods.orderError(form.getValidFrom(), form.getValidTo()));
        }
        if (form.getApprovalStatus() == null || !Codes.RATE_APPROVAL_STATUS.contains(form.getApprovalStatus())) {
            errors.add("approvalStatus", "请选择审批状态");
        } else if (Codes.RATE_APPROVED.equals(form.getApprovalStatus())) {
            if (form.getApprovedByEmployeeId() == null || employees.findById(form.getApprovedByEmployeeId()).isEmpty()) {
                errors.add("approvedByEmployeeId", "已批准须填写审批人");
            }
            if (form.getApprovedAt() == null) {
                errors.add("approvedAt", "已批准须填写审批时间");
            }
        } else if (form.getApprovedByEmployeeId() != null || form.getApprovedAt() != null) {
            errors.add("approvedByEmployeeId", "未批准时审批人和时间留空");
        }
        documents.require(form.getBasisDocumentId(), errors, "basisDocumentId");
        LaborCostRate stored = null;
        if (form.getId() != null) {
            stored = rates.findById(form.getId()).orElse(null);
            if (stored == null) {
                errors.add("id", "成本率不存在");
            }
        }
        if (stored != null && logs.existsByLaborCostRateId(stored.id)
                && (!Cmp.eq(stored.hourlyCostAmount, form.getHourlyCostAmount()) || !Cmp.eq(stored.currency, form.getCurrency()))) {
            errors.add("hourlyCostAmount", "该成本率已被工时引用，单价请另记一个版本");
        }
        if (form.getRateVersionNumber() != null && form.getRateVersionNumber() <= 0) {
            errors.add("rateVersionNumber", "版本号须大于 0");
        }
        errors.throwIfAny();
        if (form.getRateVersionNumber() == null) {
            int next = 1;
            for (LaborCostRate other : rates.findByEmploymentIdAndCostBasisOrderByRateVersionNumberAsc(
                    form.getEmploymentId(), form.getCostBasis())) {
                if (other.rateVersionNumber != null && other.rateVersionNumber >= next && !other.id.equals(form.getId())) {
                    next = other.rateVersionNumber + 1;
                }
            }
            form.setRateVersionNumber(next);
        }
        for (LaborCostRate other : rates.findByEmploymentIdAndCostBasisOrderByRateVersionNumberAsc(
                form.getEmploymentId(), form.getCostBasis())) {
            if (other.id.equals(form.getId())) {
                continue;
            }
            if (form.getRateVersionNumber().equals(other.rateVersionNumber)) {
                throw new FieldErrorsException(java.util.Map.of("rateVersionNumber", "该雇佣和口径下版本号重复"));
            }
        }
        List<LaborCostRate> toClose = new ArrayList<>();
        if (!"驳回".equals(form.getApprovalStatus())) {
            for (LaborCostRate other : rates.findByEmploymentIdAndCostBasisOrderByRateVersionNumberAsc(
                    form.getEmploymentId(), form.getCostBasis())) {
                if (other.id.equals(form.getId()) || "驳回".equals(other.approvalStatus)) {
                    continue;
                }
                if (!Periods.overlaps(form.getValidFrom(), form.getValidTo(), other.validFrom, other.validTo)) {
                    continue;
                }
                if (other.validTo == null && form.getValidFrom().isAfter(other.validFrom)) {
                    toClose.add(other);
                    continue;
                }
                throw new FieldErrorsException(java.util.Map.of("validFrom", "同一雇佣和成本口径的有效期间不能重叠"));
            }
        }
        LaborCostRate rate = stored == null ? new LaborCostRate() : stored;
        rate.employmentId = form.getEmploymentId();
        rate.costBasis = form.getCostBasis();
        rate.hourlyCostAmount = form.getHourlyCostAmount();
        rate.currency = form.getCurrency();
        rate.rateVersionNumber = form.getRateVersionNumber();
        rate.validFrom = form.getValidFrom();
        rate.validTo = form.getValidTo();
        rate.approvalStatus = form.getApprovalStatus();
        rate.approvedByEmployeeId = form.getApprovedByEmployeeId();
        rate.approvedAt = form.getApprovedAt() == null ? null : form.getApprovedAt().truncatedTo(ChronoUnit.MINUTES);
        rate.basisDocumentId = form.getBasisDocumentId();
        rate = rates.save(rate);
        for (LaborCostRate prior : toClose) {
            prior.validTo = form.getValidFrom();
            rates.save(prior);
        }
        return rate.id;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (logs.existsByLaborCostRateId(id)) {
            throw new FieldErrorsException(java.util.Map.of("*", "该成本率已被工时引用，不能删除"));
        }
        rates.deleteById(id);
    }

    private void normalize(LaborCostRateForm form) {
        form.setCostBasis(Text.blankToNull(form.getCostBasis()));
        form.setCurrency(Text.upperOrNull(form.getCurrency()));
        form.setApprovalStatus(Text.blankToNull(form.getApprovalStatus()));
        if (form.getApprovedAt() != null) {
            form.setApprovedAt(form.getApprovedAt().truncatedTo(ChronoUnit.MINUTES));
        }
    }

    static BigDecimal plain(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.stripTrailingZeros().toPlainString());
    }

    static LocalDateTime minute(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }
}
