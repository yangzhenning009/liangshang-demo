package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import org.springframework.stereotype.Component;

@Component("labels")
public class Labels {

    public String companyType(String code) {
        return Codes.companyTypeLabel(code);
    }

    public String dash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
