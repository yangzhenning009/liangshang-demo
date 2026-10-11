package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeForm {

    private Long id;
    private Long personId;
    private String employeeCode;
    private String workEmail;
    private String workPhone;
    private String status = "有效";
}
