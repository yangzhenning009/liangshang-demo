package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonForm {

    private Long id;
    private String name;
    private String mobilePhone;
    private String email;
    private String status = "有效";
}
