package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectDetailForm {

    private List<MemberForm> members = new ArrayList<>();
    private List<BudgetForm> budgets = new ArrayList<>();
    private List<DeliveryForm> deliveries = new ArrayList<>();
    private List<SupportForm> supports = new ArrayList<>();

    @Getter
    @Setter
    public static class MemberForm {
        private Long id;
        private boolean removed;
        private Long employeeId;
        private Long employmentId;
        private Long departmentAssignmentId;
        private String projectRole = "实施";
        private BigDecimal plannedHours;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
    }

    @Getter
    @Setter
    public static class BudgetForm {
        private Long id;
        private boolean removed;
        private Integer versionNumber;
        private String budgetCurrency;
        private BigDecimal plannedLaborHours;
        private BigDecimal laborCostBudgetAmount;
        private BigDecimal nonlaborCostBudgetAmount;
        private String budgetCostBasis;
        private String approvalStatus = "草稿";
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime submittedAt;
        private Long approvedByEmployeeId;
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime approvedAt;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }

    @Getter
    @Setter
    public static class DeliveryForm {
        private Long id;
        private boolean removed;
        private Long deliveryCompanyId;
        private Long leadDepartmentId;
        private String responsibilityDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }

    @Getter
    @Setter
    public static class SupportForm {
        private Long id;
        private boolean removed;
        private Long departmentId;
        private String responsibilityDescription;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
    }
}
