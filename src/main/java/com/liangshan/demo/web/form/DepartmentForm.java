package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DepartmentForm {

    private Long id;
    private Long companyId;
    private Long parentDepartmentId;
    private String departmentCode;
    private String name;
    private String departmentType = "部门";
    private Long departmentHeadEmployeeId;
    private String departmentContactPhone;
    private String departmentContactEmail;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validTo;
    private String status = "有效";
    private List<VersionForm> versions = new ArrayList<>();
    private List<ServiceCompanyForm> serviceCompanies = new ArrayList<>();

    @Getter
    @Setter
    public static class VersionForm {
        private Long id;
        private boolean removed;
        private Integer versionNumber;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }

    @Getter
    @Setter
    public static class ServiceCompanyForm {
        private Long id;
        private boolean removed;
        private Long serviceCompanyId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validFrom;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate validTo;
        private Long basisDocumentId;
    }
}
