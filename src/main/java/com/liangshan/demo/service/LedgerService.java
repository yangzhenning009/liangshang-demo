package com.liangshan.demo.service;

import com.liangshan.demo.domain.AccountingAccount;
import com.liangshan.demo.domain.AccountingPeriod;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Ledger;
import com.liangshan.demo.repository.AccountingAccountRepository;
import com.liangshan.demo.repository.AccountingPeriodRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.JournalEntryRepository;
import com.liangshan.demo.repository.LedgerRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.web.NotFoundException;
import com.liangshan.demo.web.form.AccountForm;
import com.liangshan.demo.web.form.LedgerForm;
import com.liangshan.demo.web.form.PeriodForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgers;
    private final AccountingAccountRepository accounts;
    private final AccountingPeriodRepository periods;
    private final JournalEntryRepository journals;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final EmployeeRepository employees;

    public List<Ledger> listLedgers() {
        return ledgers.findAllByOrderByLedgerCodeAsc();
    }

    public List<AccountingAccount> listAccounts() {
        return accounts.findAllByOrderByAccountCodeAsc();
    }

    public List<AccountingPeriod> listPeriods() {
        return periods.findAllByOrderByPeriodStartDateDesc();
    }

    public Ledger ledger(Long id) {
        return ledgers.findById(id).orElseThrow(NotFoundException::new);
    }

    public AccountingAccount account(Long id) {
        return accounts.findById(id).orElseThrow(NotFoundException::new);
    }

    public AccountingPeriod period(Long id) {
        return periods.findById(id).orElseThrow(NotFoundException::new);
    }

    public LedgerForm ledgerForm(Long id) {
        Ledger ledger = ledger(id);
        LedgerForm form = new LedgerForm();
        form.setId(ledger.id);
        form.setLegalPersonId(ledger.legalPersonId);
        form.setScopeCompanyId(ledger.scopeCompanyId);
        form.setLedgerCode(ledger.ledgerCode);
        form.setName(ledger.name);
        form.setLedgerType(ledger.ledgerType);
        form.setBaseCurrency(ledger.baseCurrency);
        form.setStatus(ledger.status);
        return form;
    }

    public AccountForm accountForm(Long id) {
        AccountingAccount account = account(id);
        AccountForm form = new AccountForm();
        form.setId(account.id);
        form.setLedgerId(account.ledgerId);
        form.setAccountCode(account.accountCode);
        form.setAccountName(account.accountName);
        form.setParentAccountId(account.parentAccountId);
        form.setAccountCategory(account.accountCategory);
        form.setNormalBalanceDirection(account.normalBalanceDirection);
        form.setPostable(account.postable);
        form.setValidFrom(account.validFrom);
        form.setValidTo(account.validTo);
        form.setStatus(account.status);
        return form;
    }

    public PeriodForm periodForm(Long id) {
        AccountingPeriod period = period(id);
        PeriodForm form = new PeriodForm();
        form.setId(period.id);
        form.setLedgerId(period.ledgerId);
        form.setFiscalYear(period.fiscalYear);
        form.setPeriodNumber(period.periodNumber);
        form.setPeriodStartDate(period.periodStartDate);
        form.setPeriodEndDate(period.periodEndDate);
        form.setPeriodStatus(period.periodStatus);
        return form;
    }

    @Transactional
    public long saveLedger(LedgerForm form) {
        form.setLedgerCode(Text.blankToNull(form.getLedgerCode()));
        form.setName(Text.blankToNull(form.getName()));
        form.setLedgerType(Text.blankToNull(form.getLedgerType()));
        form.setBaseCurrency(Text.upperOrNull(form.getBaseCurrency()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        FieldErrors errors = new FieldErrors();
        if (form.getId() != null && ledgers.findById(form.getId()).isEmpty()) {
            errors.add("id", "账簿不存在");
        }
        if (form.getLegalPersonId() == null || legalPersons.findById(form.getLegalPersonId()).isEmpty()) {
            errors.add("legalPersonId", "请选择归属法人");
        }
        if (form.getLedgerCode() == null) {
            errors.add("ledgerCode", "请填写账簿编号");
        }
        if (form.getName() == null) {
            errors.add("name", "请填写账簿名称");
        }
        if (form.getLedgerType() == null || !Codes.LEDGER_TYPES.contains(form.getLedgerType())) {
            errors.add("ledgerType", "请选择账簿类型");
        }
        if (form.getBaseCurrency() == null) {
            errors.add("baseCurrency", "请填写本位币");
        }
        if (form.getStatus() == null || !Codes.LEDGER_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择账簿状态");
        }
        if (form.getScopeCompanyId() == null) {
            if ("公司账簿".equals(form.getLedgerType())) {
                errors.add("scopeCompanyId", "公司账簿须选择范围公司");
            }
        } else {
            Company company = companies.findById(form.getScopeCompanyId()).orElse(null);
            if (company == null) {
                errors.add("scopeCompanyId", "范围公司不存在");
            } else if ("法人账簿".equals(form.getLedgerType())) {
                errors.add("scopeCompanyId", "法人账簿不要填写范围公司");
            } else {
                Long expected = expectedLegalPerson(company, errors);
                if (expected != null && form.getLegalPersonId() != null && !expected.equals(form.getLegalPersonId())) {
                    errors.add("legalPersonId", "账簿归属法人须与范围公司一致");
                }
            }
        }
        if (form.getLedgerCode() != null) {
            for (Ledger other : ledgers.findByLedgerCode(form.getLedgerCode())) {
                if (other.id.equals(form.getId()) || !Codes.LEDGER_ACTIVE.equals(other.status) && "停用".equals(other.status)) {
                    continue;
                }
                if (Cmp.eq(other.legalPersonId, form.getLegalPersonId())
                        && Cmp.eq(other.scopeCompanyId, form.getScopeCompanyId())) {
                    errors.add("ledgerCode", "同一法人和范围内账簿编号已存在");
                }
            }
        }
        errors.throwIfAny();
        Ledger ledger = form.getId() == null ? new Ledger() : ledgers.findById(form.getId()).orElseThrow();
        ledger.legalPersonId = form.getLegalPersonId();
        ledger.scopeCompanyId = form.getScopeCompanyId();
        ledger.ledgerCode = form.getLedgerCode();
        ledger.name = form.getName();
        ledger.ledgerType = form.getLedgerType();
        ledger.baseCurrency = form.getBaseCurrency();
        ledger.status = form.getStatus();
        return ledgers.save(ledger).id;
    }

    @Transactional
    public long saveAccount(AccountForm form) {
        form.setAccountCode(Text.blankToNull(form.getAccountCode()));
        form.setAccountName(Text.blankToNull(form.getAccountName()));
        form.setAccountCategory(Text.blankToNull(form.getAccountCategory()));
        form.setNormalBalanceDirection(Text.blankToNull(form.getNormalBalanceDirection()));
        form.setStatus(Text.blankToNull(form.getStatus()));
        FieldErrors errors = new FieldErrors();
        AccountingAccount stored = form.getId() == null ? null : accounts.findById(form.getId()).orElse(null);
        if (form.getId() != null && stored == null) {
            errors.add("id", "科目不存在");
        }
        Ledger ledger = form.getLedgerId() == null ? null : ledgers.findById(form.getLedgerId()).orElse(null);
        if (ledger == null) {
            errors.add("ledgerId", "请选择账簿");
        }
        if (form.getAccountCode() == null) {
            errors.add("accountCode", "请填写科目代码");
        }
        if (form.getAccountName() == null) {
            errors.add("accountName", "请填写科目名称");
        }
        if (form.getAccountCategory() == null || !Codes.ACCOUNT_CATEGORIES.contains(form.getAccountCategory())) {
            errors.add("accountCategory", "请选择科目类别");
        }
        if (form.getNormalBalanceDirection() == null || !Codes.BALANCE_DIRECTIONS.contains(form.getNormalBalanceDirection())) {
            errors.add("normalBalanceDirection", "请选择余额方向");
        }
        if (form.getValidFrom() == null) {
            errors.add("validFrom", "请填写开始日");
        }
        errors.add("validTo", Periods.orderError(form.getValidFrom(), form.getValidTo()));
        if (form.getStatus() == null || !Codes.ACCOUNT_STATUS.contains(form.getStatus())) {
            errors.add("status", "请选择科目状态");
        }
        if (ledger != null && form.getAccountCode() != null) {
            for (AccountingAccount other : accounts.findByLedgerIdOrderByAccountCodeAsc(ledger.id)) {
                if (!other.id.equals(form.getId()) && form.getAccountCode().equals(other.accountCode)) {
                    errors.add("accountCode", "同一账簿内科目代码已存在");
                }
            }
        }
        if (form.getParentAccountId() != null) {
            AccountingAccount parent = accounts.findById(form.getParentAccountId()).orElse(null);
            if (parent == null || ledger == null || !ledger.id.equals(parent.ledgerId)) {
                errors.add("parentAccountId", "上级科目须属于同一账簿");
            } else if (form.getId() != null && createsCycle(form.getId(), parent)) {
                errors.add("parentAccountId", "上级科目不能形成循环");
            }
        }
        errors.throwIfAny();
        AccountingAccount account = stored == null ? new AccountingAccount() : stored;
        account.ledgerId = form.getLedgerId();
        account.accountCode = form.getAccountCode();
        account.accountName = form.getAccountName();
        account.parentAccountId = form.getParentAccountId();
        account.accountCategory = form.getAccountCategory();
        account.normalBalanceDirection = form.getNormalBalanceDirection();
        account.postable = form.isPostable();
        account.validFrom = form.getValidFrom();
        account.validTo = form.getValidTo();
        account.status = form.getStatus();
        return accounts.save(account).id;
    }

    @Transactional
    public long savePeriod(PeriodForm form) {
        FieldErrors errors = new FieldErrors();
        AccountingPeriod stored = form.getId() == null ? null : periods.findById(form.getId()).orElse(null);
        if (form.getId() != null && stored == null) {
            errors.add("id", "期间不存在");
        }
        if (stored != null && Codes.PERIOD_CLOSED.equals(stored.periodStatus)) {
            errors.add("periodStatus", "已关闭期间不能修改");
        }
        Ledger ledger = form.getLedgerId() == null ? null : ledgers.findById(form.getLedgerId()).orElse(null);
        if (ledger == null) {
            errors.add("ledgerId", "请选择账簿");
        }
        if (form.getFiscalYear() == null || form.getFiscalYear() < 1900) {
            errors.add("fiscalYear", "请填写会计年度");
        }
        if (form.getPeriodNumber() == null || form.getPeriodNumber() <= 0) {
            errors.add("periodNumber", "请填写期间序号");
        }
        if (form.getPeriodStartDate() == null || form.getPeriodEndDate() == null) {
            errors.add("periodStartDate", "请填写期间起止日");
        } else if (form.getPeriodEndDate().isBefore(form.getPeriodStartDate())) {
            errors.add("periodEndDate", "结束日不能早于开始日");
        }
        if (form.getPeriodStatus() == null || !Codes.PERIOD_STATUS.contains(form.getPeriodStatus())) {
            errors.add("periodStatus", "请选择期间状态");
        }
        if (Codes.PERIOD_CLOSED.equals(form.getPeriodStatus())) {
            errors.add("periodStatus", "请在列表上关闭期间");
        }
        if (ledger != null && form.getFiscalYear() != null && form.getPeriodNumber() != null
                && form.getPeriodStartDate() != null && form.getPeriodEndDate() != null) {
            for (AccountingPeriod other : periods.findByLedgerIdOrderByPeriodStartDateAsc(ledger.id)) {
                if (other.id.equals(form.getId())) {
                    continue;
                }
                if (form.getFiscalYear().equals(other.fiscalYear) && form.getPeriodNumber().equals(other.periodNumber)) {
                    errors.add("periodNumber", "同一账簿的年度期间序号已存在");
                }
                if (!form.getPeriodEndDate().isBefore(other.periodStartDate)
                        && !other.periodEndDate.isBefore(form.getPeriodStartDate())) {
                    errors.add("periodStartDate", "期间与已有期间重叠");
                }
            }
        }
        errors.throwIfAny();
        AccountingPeriod period = stored == null ? new AccountingPeriod() : stored;
        period.ledgerId = form.getLedgerId();
        period.fiscalYear = form.getFiscalYear();
        period.periodNumber = form.getPeriodNumber();
        period.periodStartDate = form.getPeriodStartDate();
        period.periodEndDate = form.getPeriodEndDate();
        period.periodStatus = Codes.PERIOD_OPEN;
        return periods.save(period).id;
    }

    @Transactional
    public void closePeriod(Long id, Long employeeId) {
        AccountingPeriod period = period(id);
        FieldErrors errors = new FieldErrors();
        if (!Codes.PERIOD_OPEN.equals(period.periodStatus)) {
            errors.add("periodStatus", "期间已经关闭");
        }
        if (employeeId == null || employees.findById(employeeId).isEmpty()) {
            errors.add("closedByEmployeeId", "请选择关闭人");
        }
        errors.throwIfAny();
        period.periodStatus = Codes.PERIOD_CLOSED;
        period.closedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        period.closedByEmployeeId = employeeId;
        periods.save(period);
    }

    @Transactional
    public void openPeriod(Long id) {
        AccountingPeriod period = period(id);
        if (journals.findAll().stream().anyMatch(entry -> period.id.equals(entry.accountingPeriodId)
                && Codes.JOURNAL_POSTED.equals(entry.status))) {
            // Reopening a period that already has postings is allowed; new postings follow the open flag.
        }
        period.periodStatus = Codes.PERIOD_OPEN;
        period.closedAt = null;
        period.closedByEmployeeId = null;
        periods.save(period);
    }

    public boolean accountUsable(AccountingAccount account, LocalDate day) {
        if (account == null || !account.postable || !Codes.LEDGER_ACTIVE.equals(account.status)) {
            return false;
        }
        if (day == null || account.validFrom == null || day.isBefore(account.validFrom)) {
            return false;
        }
        return account.validTo == null || day.isBefore(account.validTo);
    }

    private Long expectedLegalPerson(Company company, FieldErrors errors) {
        if (!Codes.BRANCH.equals(company.companyType)) {
            return company.legalPersonId;
        }
        Company head = company.headCompanyId == null ? null : companies.findById(company.headCompanyId).orElse(null);
        if (head == null || head.legalPersonId == null) {
            errors.add("scopeCompanyId", "分公司账簿须能追溯到总公司的法人");
            return null;
        }
        return head.legalPersonId;
    }

    private boolean createsCycle(Long accountId, AccountingAccount parent) {
        Set<Long> seen = new HashSet<>();
        AccountingAccount cursor = parent;
        while (cursor != null && seen.add(cursor.id)) {
            if (accountId.equals(cursor.id)) {
                return true;
            }
            cursor = cursor.parentAccountId == null ? null : accounts.findById(cursor.parentAccountId).orElse(null);
        }
        return false;
    }
}
