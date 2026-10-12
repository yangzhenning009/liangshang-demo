package com.liangshan.demo.web;

import com.liangshan.demo.domain.AccountingAccount;
import com.liangshan.demo.domain.AccountingPeriod;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.ContractTermVersion;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.Department;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.Ledger;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.Payment;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ReceivableItem;
import com.liangshan.demo.domain.RevenueRecognition;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.AccountingAccountRepository;
import com.liangshan.demo.repository.AccountingPeriodRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.DepartmentRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.LedgerRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.PaymentRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.repository.RevenueRecognitionRepository;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.JournalService;
import com.liangshan.demo.service.LedgerService;
import com.liangshan.demo.service.MonthEndService;
import com.liangshan.demo.service.RevenueRecognitionService;
import com.liangshan.demo.web.form.AccountForm;
import com.liangshan.demo.web.form.JournalForm;
import com.liangshan.demo.web.form.LedgerForm;
import com.liangshan.demo.web.form.PeriodForm;
import com.liangshan.demo.web.form.RevenueForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AccountingController {

    private final LedgerService ledgers;
    private final JournalService journals;
    private final RevenueRecognitionService revenues;
    private final MonthEndService monthEnd;
    private final LedgerRepository ledgerRepository;
    private final AccountingAccountRepository accountRepository;
    private final AccountingPeriodRepository periodRepository;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final ProjectRepository projects;
    private final ContractRepository contracts;
    private final ContractTermVersionRepository terms;
    private final ContractStageRepository stages;
    private final AcceptanceEventRepository acceptances;
    private final CostOccurrenceRepository costs;
    private final ReceivableItemRepository receivables;
    private final PayableItemRepository payables;
    private final PaymentRepository payments;
    private final RevenueRecognitionRepository recognitionRepository;

    public AccountingController(
            LedgerService ledgers,
            JournalService journals,
            RevenueRecognitionService revenues,
            MonthEndService monthEnd,
            LedgerRepository ledgerRepository,
            AccountingAccountRepository accountRepository,
            AccountingPeriodRepository periodRepository,
            CompanyRepository companies,
            LegalPersonRepository legalPersons,
            PersonRepository persons,
            EmployeeRepository employees,
            DepartmentRepository departments,
            ProjectRepository projects,
            ContractRepository contracts,
            ContractTermVersionRepository terms,
            ContractStageRepository stages,
            AcceptanceEventRepository acceptances,
            CostOccurrenceRepository costs,
            ReceivableItemRepository receivables,
            PayableItemRepository payables,
            PaymentRepository payments,
            RevenueRecognitionRepository recognitionRepository) {
        this.ledgers = ledgers;
        this.journals = journals;
        this.revenues = revenues;
        this.monthEnd = monthEnd;
        this.ledgerRepository = ledgerRepository;
        this.accountRepository = accountRepository;
        this.periodRepository = periodRepository;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
        this.departments = departments;
        this.projects = projects;
        this.contracts = contracts;
        this.terms = terms;
        this.stages = stages;
        this.acceptances = acceptances;
        this.costs = costs;
        this.receivables = receivables;
        this.payables = payables;
        this.payments = payments;
        this.recognitionRepository = recognitionRepository;
    }

    public record Choice(Long id, String label) {
    }

    @GetMapping("/ledgers")
    public String ledgers(Model model) {
        model.addAttribute("activeNav", "ledgers");
        model.addAttribute("items", ledgers.listLedgers());
        return "ledger/list";
    }

    @GetMapping("/ledgers/new")
    public String newLedger(Model model) {
        model.addAttribute("activeNav", "ledgers");
        model.addAttribute("form", new LedgerForm());
        return "ledger/form";
    }

    @GetMapping("/ledgers/{id}/edit")
    public String editLedger(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "ledgers");
        model.addAttribute("form", ledgers.ledgerForm(id));
        return "ledger/form";
    }

    @PostMapping("/ledgers")
    public String createLedger(@ModelAttribute("form") LedgerForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return saveLedger(form, binding, redirect, model);
    }

    @PostMapping("/ledgers/{id}")
    public String updateLedger(
            @PathVariable Long id, @ModelAttribute("form") LedgerForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        form.setId(id);
        return saveLedger(form, binding, redirect, model);
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("activeNav", "accounts");
        model.addAttribute("items", ledgers.listAccounts());
        return "account/list";
    }

    @GetMapping("/accounts/new")
    public String newAccount(Model model) {
        model.addAttribute("activeNav", "accounts");
        AccountForm form = new AccountForm();
        form.setValidFrom(LocalDate.now());
        model.addAttribute("form", form);
        return "account/form";
    }

    @GetMapping("/accounts/{id}/edit")
    public String editAccount(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "accounts");
        model.addAttribute("form", ledgers.accountForm(id));
        return "account/form";
    }

    @PostMapping("/accounts")
    public String createAccount(@ModelAttribute("form") AccountForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return saveAccount(form, binding, redirect, model);
    }

    @PostMapping("/accounts/{id}")
    public String updateAccount(
            @PathVariable Long id, @ModelAttribute("form") AccountForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        form.setId(id);
        return saveAccount(form, binding, redirect, model);
    }

    @GetMapping("/accounting-periods")
    public String periods(Model model) {
        model.addAttribute("activeNav", "periods");
        model.addAttribute("items", ledgers.listPeriods());
        model.addAttribute("employeeChoices", employeeChoices());
        return "period/list";
    }

    @GetMapping("/accounting-periods/new")
    public String newPeriod(Model model) {
        model.addAttribute("activeNav", "periods");
        model.addAttribute("form", new PeriodForm());
        return "period/form";
    }

    @GetMapping("/accounting-periods/{id}/edit")
    public String editPeriod(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "periods");
        model.addAttribute("form", ledgers.periodForm(id));
        return "period/form";
    }

    @PostMapping("/accounting-periods")
    public String createPeriod(@ModelAttribute("form") PeriodForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return savePeriod(form, binding, redirect, model);
    }

    @PostMapping("/accounting-periods/{id}")
    public String updatePeriod(
            @PathVariable Long id, @ModelAttribute("form") PeriodForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        form.setId(id);
        return savePeriod(form, binding, redirect, model);
    }

    @PostMapping("/accounting-periods/{id}/close")
    public String closePeriod(@PathVariable Long id, @RequestParam(required = false) Long closedByEmployeeId, RedirectAttributes redirect) {
        try {
            ledgers.closePeriod(id, closedByEmployeeId);
            redirect.addFlashAttribute("message", "期间已关闭");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/accounting-periods";
    }

    @PostMapping("/accounting-periods/{id}/open")
    public String openPeriod(@PathVariable Long id, RedirectAttributes redirect) {
        ledgers.openPeriod(id);
        redirect.addFlashAttribute("message", "期间已重新开放");
        return "redirect:/accounting-periods";
    }

    @GetMapping("/journal-entries")
    public String journals(Model model) {
        model.addAttribute("activeNav", "journals");
        model.addAttribute("items", journals.list());
        return "journal/list";
    }

    @GetMapping("/journal-entries/new")
    public String newJournal(Model model) {
        model.addAttribute("activeNav", "journals");
        JournalForm form = new JournalForm();
        form.setVoucherDate(LocalDate.now());
        model.addAttribute("form", form);
        journalChoices(model);
        return "journal/form";
    }

    @GetMapping("/journal-entries/{id}/edit")
    public String editJournal(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "journals");
        model.addAttribute("form", journals.form(id));
        journalChoices(model);
        return "journal/form";
    }

    @PostMapping("/journal-entries")
    public String createJournal(@ModelAttribute("form") JournalForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return saveJournal(form, binding, redirect, model);
    }

    @PostMapping("/journal-entries/{id}")
    public String updateJournal(
            @PathVariable Long id, @ModelAttribute("form") JournalForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        form.setId(id);
        return saveJournal(form, binding, redirect, model);
    }

    @PostMapping("/journal-entries/{id}/post")
    public String postJournal(@PathVariable Long id, @RequestParam(required = false) Long postedByEmployeeId, RedirectAttributes redirect) {
        try {
            journals.post(id, postedByEmployeeId);
            redirect.addFlashAttribute("message", "凭证已过账");
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/journal-entries/" + id + "/edit";
    }

    @PostMapping("/journal-entries/{id}/reverse")
    public String reverseJournal(
            @PathVariable Long id,
            @RequestParam(required = false) Long preparedByEmployeeId,
            @RequestParam(required = false) String adjustmentReason,
            RedirectAttributes redirect) {
        try {
            long reversalId = journals.reverse(id, preparedByEmployeeId, adjustmentReason);
            redirect.addFlashAttribute("message", "已生成冲销凭证");
            return "redirect:/journal-entries/" + reversalId + "/edit";
        } catch (FieldErrorsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/journal-entries/" + id + "/edit";
        }
    }

    @GetMapping("/revenue-recognitions")
    public String revenues(Model model) {
        model.addAttribute("activeNav", "revenue");
        model.addAttribute("items", revenues.list());
        return "revenue/list";
    }

    @GetMapping("/revenue-recognitions/new")
    public String newRevenue(Model model) {
        model.addAttribute("activeNav", "revenue");
        RevenueForm form = new RevenueForm();
        form.setRecognitionDate(LocalDate.now());
        model.addAttribute("form", form);
        revenueChoices(model);
        return "revenue/form";
    }

    @GetMapping("/revenue-recognitions/{id}/edit")
    public String editRevenue(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "revenue");
        model.addAttribute("form", revenues.form(id));
        revenueChoices(model);
        return "revenue/form";
    }

    @PostMapping("/revenue-recognitions")
    public String createRevenue(@ModelAttribute("form") RevenueForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return saveRevenue(form, binding, redirect, model);
    }

    @PostMapping("/revenue-recognitions/{id}")
    public String updateRevenue(
            @PathVariable Long id, @ModelAttribute("form") RevenueForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        form.setId(id);
        return saveRevenue(form, binding, redirect, model);
    }

    @GetMapping("/month-end")
    public String monthEnd(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        model.addAttribute("activeNav", "month-end");
        LocalDate today = LocalDate.now();
        int selectedYear = year == null ? today.getYear() : year;
        int selectedMonth = month == null ? today.getMonthValue() : month;
        model.addAttribute("year", selectedYear);
        model.addAttribute("month", selectedMonth);
        if (selectedMonth < 1 || selectedMonth > 12) {
            model.addAttribute("error", "月份须在 1 到 12");
            model.addAttribute("rows", List.of());
            return "month-end/summary";
        }
        model.addAttribute("rows", monthEnd.summarize(selectedYear, selectedMonth));
        return "month-end/summary";
    }

    @ModelAttribute("parentAccounts")
    public List<AccountingAccount> parentAccounts() {
        return accountRepository.findAllByOrderByAccountCodeAsc();
    }

    @ModelAttribute("ledgerChoices")
    public List<Choice> ledgerChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Ledger ledger : ledgerRepository.findAllByOrderByLedgerCodeAsc()) {
            rows.add(new Choice(ledger.id, ledger.ledgerCode + " " + ledger.name));
        }
        return rows;
    }

    @ModelAttribute("companyChoices")
    public List<Choice> companyChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Company company : companies.findAllByOrderByCompanyCodeAsc()) {
            rows.add(new Choice(company.id, company.companyCode + " " + company.name));
        }
        return rows;
    }

    @ModelAttribute("legalPersonChoices")
    public List<Choice> legalPersonChoices() {
        List<Choice> rows = new ArrayList<>();
        for (LegalPerson legalPerson : legalPersons.findAllByOrderByLegalNameAsc()) {
            rows.add(new Choice(legalPerson.id, legalPerson.legalName));
        }
        return rows;
    }

    @ModelAttribute("personChoices")
    public List<Choice> personChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Person person : persons.findAllByOrderByNameAsc()) {
            rows.add(new Choice(person.id, person.name));
        }
        return rows;
    }

    @ModelAttribute("employeeChoices")
    public List<Choice> employeeChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Employee employee : employees.findAllByOrderByEmployeeCodeAsc()) {
            Person person = persons.findById(employee.personId).orElse(null);
            rows.add(new Choice(employee.id, employee.employeeCode + (person == null ? "" : " " + person.name)));
        }
        return rows;
    }

    @ModelAttribute("ledgerTypes")
    public List<String> ledgerTypes() {
        return Codes.LEDGER_TYPES;
    }

    @ModelAttribute("ledgerStatuses")
    public List<String> ledgerStatuses() {
        return Codes.LEDGER_STATUS;
    }

    @ModelAttribute("accountCategories")
    public List<String> accountCategories() {
        return Codes.ACCOUNT_CATEGORIES;
    }

    @ModelAttribute("balanceDirections")
    public List<String> balanceDirections() {
        return Codes.BALANCE_DIRECTIONS;
    }

    @ModelAttribute("revenueStatuses")
    public List<String> revenueStatuses() {
        return Codes.REVENUE_STATUS;
    }

    private void journalChoices(Model model) {
        List<Choice> accountChoices = new ArrayList<>();
        for (AccountingAccount account : accountRepository.findAllByOrderByAccountCodeAsc()) {
            accountChoices.add(new Choice(account.id, account.accountCode + " " + account.accountName));
        }
        model.addAttribute("accountChoices", accountChoices);
        List<Choice> periodChoices = new ArrayList<>();
        for (AccountingPeriod period : periodRepository.findAllByOrderByPeriodStartDateDesc()) {
            periodChoices.add(new Choice(period.id, period.fiscalYear + "年第" + period.periodNumber + "期 " + period.periodStatus));
        }
        model.addAttribute("periodChoices", periodChoices);
        List<Choice> departmentChoices = new ArrayList<>();
        for (Department department : departments.findAllByOrderByDepartmentCodeAsc()) {
            departmentChoices.add(new Choice(department.id, department.departmentCode + " " + department.name));
        }
        model.addAttribute("departmentChoices", departmentChoices);
        List<Choice> projectChoices = new ArrayList<>();
        for (Project project : projects.findAllByOrderByProjectCodeAsc()) {
            projectChoices.add(new Choice(project.id, project.projectCode + " " + project.name));
        }
        model.addAttribute("projectChoices", projectChoices);
        List<Choice> stageChoices = new ArrayList<>();
        for (ContractStage stage : stages.findAll()) {
            stageChoices.add(new Choice(stage.id, stage.stageCode + " " + stage.stageName));
        }
        model.addAttribute("stageChoices", stageChoices);
        List<Choice> costChoices = new ArrayList<>();
        for (CostOccurrence cost : costs.findAllByOrderByIncurredAtDescIdDesc()) {
            costChoices.add(new Choice(cost.id, cost.costNumber));
        }
        model.addAttribute("costChoices", costChoices);
        List<Choice> receivableChoices = new ArrayList<>();
        for (ReceivableItem item : receivables.findAllByOrderByIdDesc()) {
            receivableChoices.add(new Choice(item.id, item.receivableNumber));
        }
        model.addAttribute("receivableChoices", receivableChoices);
        List<Choice> payableChoices = new ArrayList<>();
        for (PayableItem item : payables.findAllByOrderByIdDesc()) {
            payableChoices.add(new Choice(item.id, item.payableNumber));
        }
        model.addAttribute("payableChoices", payableChoices);
        List<Choice> paymentChoices = new ArrayList<>();
        for (Payment payment : payments.findAllByOrderByPaidAtDescIdDesc()) {
            paymentChoices.add(new Choice(payment.id, payment.paymentNumber));
        }
        model.addAttribute("paymentChoices", paymentChoices);
        List<Choice> revenueChoices = new ArrayList<>();
        for (RevenueRecognition row : recognitionRepository.findAllByOrderByRecognitionDateDescIdDesc()) {
            revenueChoices.add(new Choice(row.id, row.recognitionDate + " " + row.recognizedRevenueAmount + " " + row.currency));
        }
        model.addAttribute("revenueChoices", revenueChoices);
    }

    private void revenueChoices(Model model) {
        List<Choice> contractChoices = new ArrayList<>();
        for (Contract contract : contracts.findAllByOrderByContractNumberAsc()) {
            contractChoices.add(new Choice(contract.id, contract.contractNumber + " " + contract.contractTitle));
        }
        model.addAttribute("contractChoices", contractChoices);
        List<Choice> termChoices = new ArrayList<>();
        for (ContractTermVersion term : terms.findAll()) {
            Contract contract = contracts.findById(term.contractId).orElse(null);
            String number = contract == null ? "" : contract.contractNumber + " ";
            termChoices.add(new Choice(term.id, number + "条款 v" + term.versionNumber));
        }
        model.addAttribute("termChoices", termChoices);
        List<Choice> projectChoices = new ArrayList<>();
        for (Project project : projects.findAllByOrderByProjectCodeAsc()) {
            projectChoices.add(new Choice(project.id, project.projectCode + " " + project.name));
        }
        model.addAttribute("projectChoices", projectChoices);
        List<Choice> stageChoices = new ArrayList<>();
        for (ContractStage stage : stages.findAll()) {
            stageChoices.add(new Choice(stage.id, stage.stageCode + " " + stage.stageName));
        }
        model.addAttribute("stageChoices", stageChoices);
        List<Choice> periodChoices = new ArrayList<>();
        for (AccountingPeriod period : periodRepository.findAllByOrderByPeriodStartDateDesc()) {
            periodChoices.add(new Choice(period.id, period.fiscalYear + "年第" + period.periodNumber + "期"));
        }
        model.addAttribute("periodChoices", periodChoices);
        List<Choice> acceptanceChoices = new ArrayList<>();
        acceptances.findAll().forEach(event -> acceptanceChoices.add(new Choice(event.id, event.acceptanceNumber)));
        model.addAttribute("acceptanceChoices", acceptanceChoices);
    }

    private String saveLedger(LedgerForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "ledgers");
        if (binding.hasErrors()) {
            return "ledger/form";
        }
        try {
            long id = ledgers.saveLedger(form);
            redirect.addFlashAttribute("message", "账簿已保存");
            return "redirect:/ledgers/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "ledger/form";
        }
    }

    private String saveAccount(AccountForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "accounts");
        if (binding.hasErrors()) {
            return "account/form";
        }
        try {
            long id = ledgers.saveAccount(form);
            redirect.addFlashAttribute("message", "科目已保存");
            return "redirect:/accounts/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "account/form";
        }
    }

    private String savePeriod(PeriodForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "periods");
        if (binding.hasErrors()) {
            return "period/form";
        }
        try {
            long id = ledgers.savePeriod(form);
            redirect.addFlashAttribute("message", "期间已保存");
            return "redirect:/accounting-periods/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "period/form";
        }
    }

    private String saveJournal(JournalForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "journals");
        journalChoices(model);
        if (form.getLines() == null) {
            form.setLines(new ArrayList<>());
        }
        if (form.getSources() == null) {
            form.setSources(new ArrayList<>());
        }
        if (binding.hasErrors()) {
            return "journal/form";
        }
        try {
            long id = journals.save(form);
            redirect.addFlashAttribute("message", "凭证草稿已保存");
            return "redirect:/journal-entries/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "journal/form";
        }
    }

    private String saveRevenue(RevenueForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "revenue");
        revenueChoices(model);
        if (binding.hasErrors()) {
            return "revenue/form";
        }
        try {
            long id = revenues.save(form);
            redirect.addFlashAttribute("message", "收入确认已保存");
            return "redirect:/revenue-recognitions/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "revenue/form";
        }
    }
}
