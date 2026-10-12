package com.liangshan.demo.web.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LedgerForm {

    private Long id;
    private Long legalPersonId;
    private Long scopeCompanyId;
    private String ledgerCode;
    private String name;
    private String ledgerType = "公司账簿";
    private String baseCurrency;
    private String status = "启用";
}
