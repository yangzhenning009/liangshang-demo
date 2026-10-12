package com.liangshan.demo.web;

import com.liangshan.demo.domain.AcceptanceEvent;
import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.Contract;
import com.liangshan.demo.domain.ContractBillingSchedule;
import com.liangshan.demo.domain.ContractPaymentSchedule;
import com.liangshan.demo.domain.ContractStage;
import com.liangshan.demo.domain.Employee;
import com.liangshan.demo.domain.Invoice;
import com.liangshan.demo.domain.LegalPerson;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.Person;
import com.liangshan.demo.domain.Project;
import com.liangshan.demo.domain.ReceivableItem;
import com.liangshan.demo.domain.Supplier;
import com.liangshan.demo.repository.AcceptanceEventRepository;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.ContractBillingScheduleRepository;
import com.liangshan.demo.repository.ContractPaymentScheduleRepository;
import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractStageRepository;
import com.liangshan.demo.repository.EmployeeRepository;
import com.liangshan.demo.repository.InvoiceRepository;
import com.liangshan.demo.repository.LegalPersonRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.PaymentRepository;
import com.liangshan.demo.repository.PersonRepository;
import com.liangshan.demo.repository.ProjectRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.repository.SupplierRepository;
import com.liangshan.demo.service.FieldErrorsException;
import com.liangshan.demo.service.FundsService;
import com.liangshan.demo.service.InvoiceService;
import com.liangshan.demo.web.form.InvoiceForm;
import com.liangshan.demo.web.form.PayableForm;
import com.liangshan.demo.web.form.PaymentForm;
import com.liangshan.demo.web.form.ReceivableForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FinanceController {

    private final InvoiceService invoices;
    private final FundsService funds;
    private final CompanyRepository companies;
    private final LegalPersonRepository legalPersons;
    private final PersonRepository persons;
    private final EmployeeRepository employees;
    private final SupplierRepository suppliers;
    private final ProjectRepository projects;
    private final ContractRepository contracts;
    private final ContractStageRepository stages;
    private final ContractPaymentScheduleRepository paymentSchedules;
    private final ContractBillingScheduleRepository billingSchedules;
    private final AcceptanceEventRepository acceptances;
    private final InvoiceRepository invoiceRepository;
    private final ReceivableItemRepository receivableRepository;
    private final PayableItemRepository payableRepository;
    private final PaymentRepository paymentRepository;

    public FinanceController(
            InvoiceService invoices,
            FundsService funds,
            CompanyRepository companies,
            LegalPersonRepository legalPersons,
            PersonRepository persons,
            EmployeeRepository employees,
            SupplierRepository suppliers,
            ProjectRepository projects,
            ContractRepository contracts,
            ContractStageRepository stages,
            ContractPaymentScheduleRepository paymentSchedules,
            ContractBillingScheduleRepository billingSchedules,
            AcceptanceEventRepository acceptances,
            InvoiceRepository invoiceRepository,
            ReceivableItemRepository receivableRepository,
            PayableItemRepository payableRepository,
            PaymentRepository paymentRepository) {
        this.invoices = invoices;
        this.funds = funds;
        this.companies = companies;
        this.legalPersons = legalPersons;
        this.persons = persons;
        this.employees = employees;
        this.suppliers = suppliers;
        this.projects = projects;
        this.contracts = contracts;
        this.stages = stages;
        this.paymentSchedules = paymentSchedules;
        this.billingSchedules = billingSchedules;
        this.acceptances = acceptances;
        this.invoiceRepository = invoiceRepository;
        this.receivableRepository = receivableRepository;
        this.payableRepository = payableRepository;
        this.paymentRepository = paymentRepository;
    }

    public record Choice(Long id, String label) {
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

    @ModelAttribute("supplierChoices")
    public List<Choice> supplierChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Supplier supplier : suppliers.findAllByOrderBySupplierCodeAsc()) {
            rows.add(new Choice(supplier.id, supplier.supplierCode + " " + supplier.supplierName));
        }
        return rows;
    }

    @ModelAttribute("projectChoices")
    public List<Choice> projectChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Project project : projects.findAllByOrderByProjectCodeAsc()) {
            rows.add(new Choice(project.id, project.projectCode + " " + project.name));
        }
        return rows;
    }

    @ModelAttribute("contractChoices")
    public List<Choice> contractChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Contract contract : contracts.findAllByOrderByContractNumberAsc()) {
            rows.add(new Choice(contract.id, contract.contractNumber + " " + contract.contractTitle));
        }
        return rows;
    }

    @ModelAttribute("stageChoices")
    public List<Choice> stageChoices() {
        List<Choice> rows = new ArrayList<>();
        for (ContractStage stage : stages.findAll()) {
            Contract contract = contracts.findById(stage.contractId).orElse(null);
            String prefix = contract == null ? "合同" : contract.contractNumber;
            rows.add(new Choice(stage.id, prefix + " · " + stage.stageCode + " " + stage.stageName));
        }
        return rows;
    }

    @ModelAttribute("paymentScheduleChoices")
    public List<Choice> paymentScheduleChoices() {
        List<Choice> rows = new ArrayList<>();
        for (ContractPaymentSchedule schedule : paymentSchedules.findAll()) {
            Contract contract = contracts.findById(schedule.contractId).orElse(null);
            String prefix = contract == null ? "合同" : contract.contractNumber;
            rows.add(new Choice(schedule.id, prefix + " · 第" + schedule.scheduleNumber + "期 " + schedule.plannedPaymentAmount));
        }
        return rows;
    }

    @ModelAttribute("billingScheduleChoices")
    public List<Choice> billingScheduleChoices() {
        List<Choice> rows = new ArrayList<>();
        for (ContractBillingSchedule schedule : billingSchedules.findAll()) {
            Contract contract = contracts.findById(schedule.contractId).orElse(null);
            String prefix = contract == null ? "合同" : contract.contractNumber;
            rows.add(new Choice(schedule.id, prefix + " · 开票" + schedule.scheduleNumber + " " + schedule.plannedInvoiceGrossAmount));
        }
        return rows;
    }

    @ModelAttribute("acceptanceChoices")
    public List<Choice> acceptanceChoices() {
        List<Choice> rows = new ArrayList<>();
        for (AcceptanceEvent event : acceptances.findAll()) {
            rows.add(new Choice(event.id, event.acceptanceNumber + " " + event.acceptanceResult));
        }
        return rows;
    }

    @ModelAttribute("invoiceChoices")
    public List<Choice> invoiceChoices() {
        List<Choice> rows = new ArrayList<>();
        for (Invoice invoice : invoiceRepository.findAllByOrderByIssuedAtDescIdDesc()) {
            rows.add(new Choice(invoice.id, invoice.invoiceNumber + " " + invoice.invoiceColor));
        }
        return rows;
    }

    @ModelAttribute("receivableChoices")
    public List<Choice> receivableChoices() {
        List<Choice> rows = new ArrayList<>();
        for (ReceivableItem item : receivableRepository.findAllByOrderByIdDesc()) {
            rows.add(new Choice(item.id, item.receivableNumber + " " + item.receivableAmount + " " + item.currency));
        }
        return rows;
    }

    @ModelAttribute("payableChoices")
    public List<Choice> payableChoices() {
        List<Choice> rows = new ArrayList<>();
        for (PayableItem item : payableRepository.findAllByOrderByIdDesc()) {
            rows.add(new Choice(item.id, item.payableNumber + " " + item.payableAmount + " " + item.currency));
        }
        return rows;
    }

    @ModelAttribute("paymentChoices")
    public List<Choice> paymentChoices() {
        List<Choice> rows = new ArrayList<>();
        for (var payment : paymentRepository.findAllByOrderByPaidAtDescIdDesc()) {
            rows.add(new Choice(payment.id, payment.paymentNumber + " " + payment.paymentAmount));
        }
        return rows;
    }

    @ModelAttribute("invoiceTypes")
    public List<String> invoiceTypes() {
        return Codes.INVOICE_TYPES;
    }

    @ModelAttribute("invoiceMediums")
    public List<String> invoiceMediums() {
        return Codes.INVOICE_MEDIUMS;
    }

    @ModelAttribute("invoiceColors")
    public List<String> invoiceColors() {
        return Codes.INVOICE_COLORS;
    }

    @ModelAttribute("invoiceStatuses")
    public List<String> invoiceStatuses() {
        return Codes.INVOICE_STATUS;
    }

    @ModelAttribute("taxTreatments")
    public List<String> taxTreatments() {
        return Codes.TAX_TREATMENTS;
    }

    @ModelAttribute("allocationBases")
    public List<String> allocationBases() {
        return Codes.INVOICE_ALLOCATION_BASIS;
    }

    @ModelAttribute("receivablePurposes")
    public List<String> receivablePurposes() {
        return Codes.RECEIVABLE_PURPOSES;
    }

    @ModelAttribute("payablePurposes")
    public List<String> payablePurposes() {
        return Codes.PAYABLE_PURPOSES;
    }

    @ModelAttribute("itemStatuses")
    public List<String> itemStatuses() {
        return Codes.PAYABLE_STATUS;
    }

    @ModelAttribute("adjustmentStatuses")
    public List<String> adjustmentStatuses() {
        return Codes.ADJUSTMENT_STATUS;
    }

    @ModelAttribute("paymentKinds")
    public List<String> paymentKinds() {
        return Codes.PAYMENT_KINDS;
    }

    @ModelAttribute("paymentPurposes")
    public List<String> paymentPurposes() {
        return Codes.PAYMENT_PURPOSE;
    }

    @ModelAttribute("paymentChannels")
    public List<String> paymentChannels() {
        return Codes.PAYMENT_CHANNELS;
    }

    @ModelAttribute("paymentStatuses")
    public List<String> paymentStatuses() {
        return Codes.PAYMENT_STATUS;
    }

    @ModelAttribute("settlementActions")
    public List<String> settlementActions() {
        return Codes.SETTLEMENT_ACTIONS;
    }

    @ModelAttribute("settlementStatuses")
    public List<String> settlementStatuses() {
        return Codes.SETTLEMENT_STATUS;
    }

    @GetMapping("/invoices")
    public String invoices(Model model) {
        model.addAttribute("activeNav", "invoices");
        model.addAttribute("items", invoices.list());
        return "invoice/list";
    }

    @GetMapping("/invoices/new")
    public String newInvoice(Model model) {
        model.addAttribute("activeNav", "invoices");
        InvoiceForm form = new InvoiceForm();
        form.setIssuedAt(java.time.LocalDate.now());
        model.addAttribute("form", form);
        return "invoice/form";
    }

    @GetMapping("/invoices/{id}/edit")
    public String editInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "invoices");
        model.addAttribute("form", invoices.form(id));
        return "invoice/form";
    }

    @PostMapping("/invoices")
    public String createInvoice(@ModelAttribute("form") InvoiceForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return saveInvoice(form, binding, redirect, model);
    }

    @PostMapping("/invoices/{id}")
    public String updateInvoice(
            @PathVariable Long id,
            @ModelAttribute("form") InvoiceForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return saveInvoice(form, binding, redirect, model);
    }

    @GetMapping("/receivables")
    public String receivables(Model model) {
        model.addAttribute("activeNav", "receivables");
        model.addAttribute("items", funds.receivables());
        return "receivable/list";
    }

    @GetMapping("/receivables/new")
    public String newReceivable(Model model) {
        model.addAttribute("activeNav", "receivables");
        ReceivableForm form = new ReceivableForm();
        form.setConfirmedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        model.addAttribute("form", form);
        model.addAttribute("outstanding", null);
        model.addAttribute("settled", null);
        return "receivable/form";
    }

    @GetMapping("/receivables/{id}/edit")
    public String editReceivable(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "receivables");
        model.addAttribute("form", funds.receivableForm(id));
        model.addAttribute("outstanding", funds.outstandingReceivable(id));
        model.addAttribute("settled", funds.settledReceivable(id));
        return "receivable/form";
    }

    @PostMapping("/receivables")
    public String createReceivable(
            @ModelAttribute("form") ReceivableForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return saveReceivable(form, binding, redirect, model);
    }

    @PostMapping("/receivables/{id}")
    public String updateReceivable(
            @PathVariable Long id,
            @ModelAttribute("form") ReceivableForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return saveReceivable(form, binding, redirect, model);
    }

    @GetMapping("/payables")
    public String payables(Model model) {
        model.addAttribute("activeNav", "payables");
        model.addAttribute("items", funds.payables());
        return "payable/list";
    }

    @GetMapping("/payables/new")
    public String newPayable(Model model) {
        model.addAttribute("activeNav", "payables");
        PayableForm form = new PayableForm();
        form.setConfirmedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        model.addAttribute("form", form);
        model.addAttribute("outstanding", null);
        return "payable/form";
    }

    @GetMapping("/payables/{id}/edit")
    public String editPayable(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "payables");
        model.addAttribute("form", funds.payableForm(id));
        model.addAttribute("outstanding", funds.outstandingPayable(id));
        return "payable/form";
    }

    @PostMapping("/payables")
    public String createPayable(
            @ModelAttribute("form") PayableForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return savePayable(form, binding, redirect, model);
    }

    @PostMapping("/payables/{id}")
    public String updatePayable(
            @PathVariable Long id,
            @ModelAttribute("form") PayableForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return savePayable(form, binding, redirect, model);
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("activeNav", "payments");
        model.addAttribute("items", funds.payments());
        return "payment/list";
    }

    @GetMapping("/payments/new")
    public String newPayment(Model model) {
        model.addAttribute("activeNav", "payments");
        PaymentForm form = new PaymentForm();
        form.setPaidAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        form.setConfirmedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        model.addAttribute("form", form);
        return "payment/form";
    }

    @GetMapping("/payments/{id}/edit")
    public String editPayment(@PathVariable Long id, Model model) {
        model.addAttribute("activeNav", "payments");
        model.addAttribute("form", funds.paymentForm(id));
        return "payment/form";
    }

    @PostMapping("/payments")
    public String createPayment(
            @ModelAttribute("form") PaymentForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        return savePayment(form, binding, redirect, model);
    }

    @PostMapping("/payments/{id}")
    public String updatePayment(
            @PathVariable Long id,
            @ModelAttribute("form") PaymentForm form,
            BindingResult binding,
            RedirectAttributes redirect,
            Model model) {
        form.setId(id);
        return savePayment(form, binding, redirect, model);
    }

    private String saveInvoice(InvoiceForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "invoices");
        if (binding.hasErrors()) {
            return "invoice/form";
        }
        try {
            long id = invoices.save(form);
            redirect.addFlashAttribute("message", "发票已保存");
            return "redirect:/invoices/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "invoice/form";
        }
    }

    private String saveReceivable(ReceivableForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "receivables");
        model.addAttribute("outstanding", form.getId() == null ? null : funds.outstandingReceivable(form.getId()));
        model.addAttribute("settled", form.getId() == null ? null : funds.settledReceivable(form.getId()));
        if (binding.hasErrors()) {
            return "receivable/form";
        }
        try {
            long id = funds.saveReceivable(form);
            redirect.addFlashAttribute("message", "应收已保存");
            return "redirect:/receivables/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "receivable/form";
        }
    }

    private String savePayable(PayableForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "payables");
        model.addAttribute("outstanding", form.getId() == null ? null : funds.outstandingPayable(form.getId()));
        if (binding.hasErrors()) {
            return "payable/form";
        }
        try {
            long id = funds.savePayable(form);
            redirect.addFlashAttribute("message", "应付已保存");
            return "redirect:/payables/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "payable/form";
        }
    }

    private String savePayment(PaymentForm form, BindingResult binding, RedirectAttributes redirect, Model model) {
        model.addAttribute("activeNav", "payments");
        if (binding.hasErrors()) {
            return "payment/form";
        }
        try {
            long id = funds.savePayment(form);
            redirect.addFlashAttribute("message", "收付款已保存");
            return "redirect:/payments/" + id + "/edit";
        } catch (FieldErrorsException exception) {
            Forms.reject(binding, exception);
            return "payment/form";
        }
    }
}
