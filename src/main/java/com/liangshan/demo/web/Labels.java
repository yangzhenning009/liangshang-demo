package com.liangshan.demo.web;

import com.liangshan.demo.domain.Codes;
import com.liangshan.demo.service.ProjectService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("labels")
public class Labels {

    public String companyType(String code) {
        return Codes.companyTypeLabel(code);
    }

    public String dash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public String budgetTotal(BigDecimal labor, BigDecimal nonlabor) {
        BigDecimal total = ProjectService.budgetTotal(labor, nonlabor);
        return total == null ? "未编制" : total.toPlainString();
    }
}
