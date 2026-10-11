package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectForm {

    private Long id;
    private String projectCode;
    private String name;
    private String projectType = "客户交付";
    private String projectScopeDescription;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate actualStartAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate actualCompletedAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate closedAt;
    private String budgetCurrency;
    private Long customerCompanyId;
    private Long customerLegalPersonId;
    private Long customerPersonId;
    private Long ownerCompanyId;
    private Long leadDepartmentId;
    private Long projectManagerEmployeeId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedStartAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedEndAt;
    private String status = "筹备";
    private Long basisDocumentId;
}
