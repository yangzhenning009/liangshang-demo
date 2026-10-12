package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class WorkLogForm {

    private Long id;
    private Long employeeId;
    private Long employmentId;
    private Long departmentAssignmentId;
    private Long projectId;
    private Long contractStageId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate workDate;
    private BigDecimal hours;
    private String workDescription;
    private boolean billable;
    private Long supersedesWorkLogId;
    private boolean submitForApproval;
}
