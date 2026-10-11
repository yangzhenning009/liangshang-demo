package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EmployeeOrgForm {

    private Long employeeId;
    private List<EmploymentForm> employments = new ArrayList<>();
    private List<AssignmentForm> assignments = new ArrayList<>();
    private List<ReportingForm> reportingLines = new ArrayList<>();

    @Getter
    @Setter
    public static class EmploymentForm {
        private Long id;
        private String rowKey;
        private boolean removed;
        private Long employerCompanyId;
        private Long employerLegalPersonId;
        private String employeeNumber;
        private String employmentType = "全职";
        private String employmentContractNumber;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate employmentStartDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate employmentEndDate;
        private String terminationReason;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate employmentContractStartDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate employmentContractEndDate;
        private LocalDateTime recordedAt;
        private String status = "在职";
        private Long employmentDocumentId;
        private String displayLabel;
    }

    @Getter
    @Setter
    public static class AssignmentForm {
        private Long id;
        private String rowKey;
        private boolean removed;
        private String employmentKey;
        private Long departmentId;
        private String assignmentType = "主岗";
        private String positionTitle;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private String status = "有效";
        private String displayLabel;
    }

    @Getter
    @Setter
    public static class ReportingForm {
        private Long id;
        private boolean removed;
        private String assignmentKey;
        private String managerAssignmentKey;
        private String reportingType = "行政";
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
    }
}
