package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.Company;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.Ledger;
import com.liangshan.demo.domain.PayableItem;
import com.liangshan.demo.domain.ReceivableItem;
import com.liangshan.demo.domain.RevenueRecognition;
import com.liangshan.demo.repository.CompanyRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.LedgerRepository;
import com.liangshan.demo.repository.PayableItemRepository;
import com.liangshan.demo.repository.ReceivableItemRepository;
import com.liangshan.demo.repository.RevenueRecognitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthEndService {

    private static final String CONFIRMED = "已确认";

    private final RevenueRecognitionRepository recognitions;
    private final LedgerRepository ledgers;
    private final CostOccurrenceRepository costs;
    private final ReceivableItemRepository receivables;
    private final PayableItemRepository payables;
    private final CompanyRepository companies;

    public record SummaryRow(
            String label,
            Long companyId,
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal receivables,
            BigDecimal payables) {
    }

    public List<SummaryRow> summarize(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate next = start.plusMonths(1);
        Map<Long, Mutable> branches = new LinkedHashMap<>();
        for (Company company : companies.findByCompanyTypeOrderByCompanyCodeAsc(Codes.BRANCH)) {
            branches.put(company.id, new Mutable(company.name));
        }
        Mutable group = new Mutable("集团");
        Map<Long, Long> scopeByLedger = new LinkedHashMap<>();
        for (Ledger ledger : ledgers.findAll()) {
            scopeByLedger.put(ledger.id, ledger.scopeCompanyId);
        }
        for (RevenueRecognition row : recognitions.findAll()) {
            if (!Codes.REVENUE_APPROVED.equals(row.status) || !inMonth(row.recognitionDate, start, next)) {
                continue;
            }
            add(group, branches, scopeByLedger.get(row.ledgerId), row.recognizedRevenueAmount, Kind.REVENUE);
        }
        for (CostOccurrence row : costs.findAll()) {
            if (!Codes.COST_EFFECTIVE.equals(row.status) || !inMonth(row.incurredAt, start, next)) {
                continue;
            }
            add(group, branches, row.bearingCompanyId, row.costAmount, Kind.COST);
        }
        for (ReceivableItem row : receivables.findAll()) {
            if (!CONFIRMED.equals(row.status) || !inMonth(row.confirmedAt, start, next)) {
                continue;
            }
            add(group, branches, row.creditorCompanyId, row.receivableAmount, Kind.RECEIVABLE);
        }
        for (PayableItem row : payables.findAll()) {
            if (!CONFIRMED.equals(row.status) || !inMonth(row.confirmedAt, start, next)) {
                continue;
            }
            add(group, branches, row.debtorCompanyId, row.payableAmount, Kind.PAYABLE);
        }
        List<SummaryRow> rows = new ArrayList<>();
        rows.add(group.freeze(null));
        for (Map.Entry<Long, Mutable> entry : branches.entrySet()) {
            rows.add(entry.getValue().freeze(entry.getKey()));
        }
        return rows;
    }

    private static void add(Mutable group, Map<Long, Mutable> branches, Long companyId, BigDecimal amount, Kind kind) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        group.add(kind, value);
        Mutable branch = companyId == null ? null : branches.get(companyId);
        if (branch != null) {
            branch.add(kind, value);
        }
    }

    private static boolean inMonth(LocalDate day, LocalDate start, LocalDate next) {
        return day != null && !day.isBefore(start) && day.isBefore(next);
    }

    private static boolean inMonth(LocalDateTime moment, LocalDate start, LocalDate next) {
        return moment != null && inMonth(moment.toLocalDate(), start, next);
    }

    private enum Kind {
        REVENUE, COST, RECEIVABLE, PAYABLE
    }

    private static final class Mutable {
        private final String label;
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal cost = BigDecimal.ZERO;
        private BigDecimal receivables = BigDecimal.ZERO;
        private BigDecimal payables = BigDecimal.ZERO;

        private Mutable(String label) {
            this.label = label;
        }

        private void add(Kind kind, BigDecimal amount) {
            switch (kind) {
                case REVENUE -> revenue = revenue.add(amount);
                case COST -> cost = cost.add(amount);
                case RECEIVABLE -> receivables = receivables.add(amount);
                case PAYABLE -> payables = payables.add(amount);
            }
        }

        private SummaryRow freeze(Long companyId) {
            return new SummaryRow(label, companyId, money(revenue), money(cost), money(receivables), money(payables));
        }

        private static BigDecimal money(BigDecimal amount) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
