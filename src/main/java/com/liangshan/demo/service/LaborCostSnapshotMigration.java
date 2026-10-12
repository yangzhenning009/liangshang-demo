package com.liangshan.demo.service;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.domain.CostAllocation;
import com.liangshan.demo.domain.CostOccurrence;
import com.liangshan.demo.domain.EmployeeEmployment;
import com.liangshan.demo.repository.CostAllocationRepository;
import com.liangshan.demo.repository.CostOccurrenceRepository;
import com.liangshan.demo.repository.EmployeeEmploymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 旧库把人工成本快照写在 work_log 上。启动时抄到 cost_occurrence，之后页面只读成本行。
 * 测试库按当前实体建表，没有这些列时直接跳过。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LaborCostSnapshotMigration implements ApplicationRunner {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final JdbcTemplate jdbc;
    private final CostOccurrenceRepository costs;
    private final CostAllocationRepository allocations;
    private final EmployeeEmploymentRepository employments;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Integer present = jdbc.queryForObject(
                "select count(*) from pragma_table_info('work_log') where name = 'labor_cost_amount'",
                Integer.class);
        if (present == null || present == 0) {
            return;
        }
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select id, employee_id, employment_id, project_id, contract_stage_id, hours,
                       labor_cost_amount, labor_cost_currency, labor_cost_rate_id,
                       hourly_cost_amount_snapshot, approved_by_employee_id, approved_at,
                       work_description, work_date, approval_status, record_status
                from work_log
                where labor_cost_amount is not null and approval_status = '通过'
                """);
        int copied = 0;
        for (Map<String, Object> row : rows) {
            if (copy(row)) {
                copied++;
            }
        }
        if (copied > 0) {
            log.info("copied {} labor cost snapshots from work_log onto cost_occurrence", copied);
        }
    }

    private boolean copy(Map<String, Object> row) {
        Long workLogId = longValue(row.get("id"));
        if (workLogId == null || !costs.findByWorkLogIdOrderByIdAsc(workLogId).isEmpty()) {
            return false;
        }
        EmployeeEmployment employment = employments.findById(longValue(row.get("employment_id"))).orElse(null);
        LocalDateTime approvedAt = timestamp(row.get("approved_at"));
        LocalDateTime workDate = timestamp(row.get("work_date"));
        BigDecimal amount = decimal(row.get("labor_cost_amount"));
        String currency = row.get("labor_cost_currency") == null ? null : row.get("labor_cost_currency").toString();
        CostOccurrence cost = new CostOccurrence();
        if (employment != null) {
            cost.bearingCompanyId = employment.employerCompanyId;
            cost.bearingLegalPersonId = employment.employerLegalPersonId;
        }
        cost.incurredByEmployeeId = longValue(row.get("employee_id"));
        cost.costNumber = "LC-" + workLogId;
        cost.costType = "人工成本";
        cost.incurredAt = approvedAt == null ? LocalDateTime.now() : approvedAt;
        cost.costAmount = amount;
        cost.currency = currency;
        cost.costBasis = Codes.COST_BASIS_STANDARD_LABOR;
        cost.workLogId = workLogId;
        cost.laborCostRateId = longValue(row.get("labor_cost_rate_id"));
        cost.costedHours = decimal(row.get("hours"));
        cost.hourlyCostAmountSnapshot = decimal(row.get("hourly_cost_amount_snapshot"));
        cost.approvalStatus = Codes.RATE_APPROVED;
        cost.approvedByEmployeeId = longValue(row.get("approved_by_employee_id"));
        cost.approvedAt = approvedAt;
        cost.description = row.get("work_description") == null ? null : row.get("work_description").toString();
        String recordStatus = row.get("record_status") == null ? Codes.COST_EFFECTIVE : row.get("record_status").toString();
        cost.status = Codes.COST_RECORD_STATUS.contains(recordStatus) ? recordStatus : Codes.COST_EFFECTIVE;
        cost = costs.save(cost);
        CostAllocation allocation = new CostAllocation();
        allocation.costOccurrenceId = cost.id;
        allocation.projectId = longValue(row.get("project_id"));
        allocation.contractStageId = longValue(row.get("contract_stage_id"));
        allocation.allocatedAmount = amount;
        allocation.currency = currency;
        allocation.allocationPeriod = workDate == null ? cost.incurredAt.toLocalDate().toString().substring(0, 7) : workDate.toLocalDate().toString().substring(0, 7);
        allocation.allocationMethod = "按工时";
        allocation.allocationBasisHours = cost.costedHours;
        allocation.allocatedByEmployeeId = cost.approvedByEmployeeId;
        allocation.allocatedAt = cost.incurredAt;
        allocation.status = Codes.COST_EFFECTIVE.equals(cost.status) ? Codes.ALLOCATION_EFFECTIVE : "已替代";
        allocations.save(allocation);
        return true;
    }

    private static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : Long.valueOf(text);
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    private static LocalDateTime timestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Number number) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), SHANGHAI);
        }
        String text = value.toString().trim();
        if (text.chars().allMatch(Character::isDigit)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), SHANGHAI);
        }
        return LocalDateTime.parse(text.replace(' ', 'T').substring(0, 19));
    }
}
