package com.liangshan.demo.domain;

import java.util.List;

/**
 * 应用层允许的分类取值。数据库只存普通字符串，不做枚举或 CHECK。
 */
public final class Codes {

    public static final String COMPANY = "company";
    public static final String BRANCH = "branch";

    public static final List<String> COMPANY_TYPES = List.of(COMPANY, BRANCH);
    public static final List<String> COMPANY_STATUS = List.of("筹建", "存续", "停业", "注销");
    public static final List<String> COMPANY_FORMS = List.of("有限责任公司", "股份有限公司", "其他");
    public static final List<String> LEGAL_PERSON_TYPES = List.of(
            "企业法人", "事业单位法人", "社会团体法人", "机关法人", "其他法人");
    public static final List<String> LEGAL_PERSON_STATUS = List.of("存续", "注销", "吊销");
    public static final List<String> PERSON_STATUS = List.of("有效", "停用");
    public static final List<String> EMPLOYEE_STATUS = List.of("有效", "停用");
    public static final List<String> DEPARTMENT_TYPES = List.of("部门", "事业部", "职能中心", "团队");
    public static final List<String> DEPARTMENT_STATUS = List.of("有效", "停用");
    public static final List<String> REGISTRATION_TYPES = List.of("设立登记", "变更登记", "注销登记", "备案");
    public static final List<String> REGISTRATION_STATUS = List.of("存续", "注销", "迁出", "吊销");
    public static final List<String> BANK_STATUS = List.of("有效", "停用", "冻结");
    public static final List<String> SHARE_CLASSES = List.of("普通股", "优先股", "其他");
    public static final List<String> EMPLOYMENT_TYPES = List.of("全职", "兼职", "其他");
    public static final List<String> EMPLOYMENT_STATUS = List.of("在职", "离职");
    public static final List<String> ASSIGNMENT_TYPES = List.of("主岗", "兼岗", "矩阵任职");
    public static final List<String> ASSIGNMENT_STATUS = List.of("有效", "结束");
    public static final String REPORTING_ADMIN = "行政";
    public static final List<String> REPORTING_TYPES = List.of(REPORTING_ADMIN, "职能");
    public static final List<String> DOCUMENT_STATUS = List.of("有效", "作废");
    public static final List<String> ADMISSION_STATUS = List.of("待提交", "审核中", "通过", "驳回");
    public static final List<String> COOPERATION_STATUS = List.of("未启用", "合作中", "暂停", "终止");
    public static final List<String> PAYMENT_DUE_BASIS = List.of("发票确认日", "验收日", "约定固定日", "合同另定");
    public static final List<String> CONTACT_ROLES = List.of("商务", "交付", "财务", "其他");
    public static final List<String> CONTACT_STATUS = List.of("有效", "停用");
    public static final List<String> SUPPLY_CATEGORY_STATUS = List.of("有效", "停用");
    public static final List<String> QUALIFICATION_REVIEW_STATUS = List.of("待审核", "通过", "驳回");
    public static final List<String> QUALIFICATION_STATUS = List.of("有效", "停用");
    public static final List<String> PROJECT_TYPES = List.of("客户交付", "内部建设", "其他");
    public static final List<String> PROJECT_STATUS = List.of("筹备", "进行中", "暂停", "结项", "取消");
    public static final List<String> PROJECT_ROLES = List.of("交付负责人", "设计", "实施", "其他");
    public static final List<String> BUDGET_COST_BASIS = List.of("标准人工", "工资成本");
    public static final List<String> BUDGET_APPROVAL_STATUS = List.of("草稿", "待审批", "已批准", "驳回");

    private Codes() {
    }

    public static boolean china(String jurisdiction) {
        if (jurisdiction == null) {
            return false;
        }
        return jurisdiction.equals("中国")
                || jurisdiction.equals("CN")
                || jurisdiction.equals("CHN")
                || jurisdiction.equals("中华人民共和国")
                || jurisdiction.contains("中国");
    }

    public static String companyTypeLabel(String code) {
        if (COMPANY.equals(code)) {
            return "公司";
        }
        if (BRANCH.equals(code)) {
            return "分公司";
        }
        return code == null ? "" : code;
    }
}
