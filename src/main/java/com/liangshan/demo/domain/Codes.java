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
    public static final List<String> CONTRACT_TYPES = List.of("销售", "采购", "其他");
    public static final List<String> PRICING_METHODS = List.of("固定总价", "按量", "按工时", "其他");
    public static final List<String> CONTRACT_APPROVAL_STATUS = List.of("草稿", "待审批", "已批准", "驳回");
    public static final List<String> CONTRACT_STATUS = List.of("未生效", "生效", "终止", "作废");
    public static final List<String> STAGE_STATUS = List.of("未开始", "进行中", "完成", "取消");
    public static final List<String> SIGNATORY_ROLES = List.of("担保", "见证", "其他");
    public static final String CONTRACT_PROCUREMENT = "采购";
    public static final String TRIGGER_DATE = "指定日期";
    public static final String TRIGGER_MANUAL = "人工确认";
    public static final List<String> BILLING_TRIGGERS = List.of("指定日期", "签约", "阶段验收", "人工确认");
    public static final List<String> PAYMENT_TRIGGERS = List.of("指定日期", "签约", "阶段验收", "开票", "人工确认");
    public static final List<String> PAYMENT_PURPOSES = List.of("预付款", "进度款", "尾款", "质保金", "其他");
    public static final String DELIVERY_SUBMITTED = "已提交";
    public static final String DELIVERY_WITHDRAWN = "已撤回";
    public static final List<String> DELIVERY_STATUS = List.of(DELIVERY_SUBMITTED, DELIVERY_WITHDRAWN);
    public static final String ACCEPTANCE_EFFECTIVE = "有效";
    public static final String ACCEPTANCE_REVOKED = "已撤销";
    public static final List<String> ACCEPTANCE_STATUS = List.of(ACCEPTANCE_EFFECTIVE, ACCEPTANCE_REVOKED);
    public static final List<String> ACCEPTANCE_RESULTS = List.of("通过", "不通过", "部分通过");
    public static final String LABOR_BASIS_STANDARD = "标准成本";
    public static final List<String> LABOR_COST_BASIS = List.of(LABOR_BASIS_STANDARD, "工资成本");
    public static final String WORK_DRAFT = "草稿";
    public static final String WORK_PENDING = "待审";
    public static final String WORK_APPROVED = "通过";
    public static final String WORK_REJECTED = "驳回";
    public static final List<String> WORK_APPROVAL_STATUS = List.of(WORK_DRAFT, WORK_PENDING, WORK_APPROVED, WORK_REJECTED);
    public static final String WORK_RECORD_EFFECTIVE = "有效";
    public static final String WORK_RECORD_SUPERSEDED = "已被替代";
    public static final String WORK_RECORD_VOID = "已作废";
    public static final List<String> WORK_RECORD_STATUS = List.of(WORK_RECORD_EFFECTIVE, WORK_RECORD_SUPERSEDED, WORK_RECORD_VOID);
    public static final String RATE_APPROVED = "已批准";
    public static final List<String> RATE_APPROVAL_STATUS = List.of("草稿", "待审批", RATE_APPROVED, "驳回");
    public static final String COST_BASIS_ACTUAL = "实际费用";
    public static final String COST_BASIS_STANDARD_LABOR = "标准人工成本";
    public static final String COST_BASIS_PAYROLL = "工资成本";
    public static final List<String> COST_BASIS = List.of(COST_BASIS_ACTUAL, COST_BASIS_STANDARD_LABOR, COST_BASIS_PAYROLL);
    public static final List<String> COST_TYPES = List.of("云服务", "人工成本", "体检", "差旅", "其他");
    public static final String COST_EFFECTIVE = "有效";
    public static final String COST_SUPERSEDED = "已被替代";
    public static final String COST_VOID = "已作废";
    public static final List<String> COST_RECORD_STATUS = List.of(COST_EFFECTIVE, COST_SUPERSEDED, COST_VOID);
    public static final List<String> COST_APPROVAL_STATUS = List.of("草稿", "待审批", RATE_APPROVED, "驳回");
    public static final List<String> ALLOCATION_METHODS = List.of("按工时", "比例", "直接指定");
    public static final String ALLOCATION_EFFECTIVE = "有效";
    public static final List<String> ALLOCATION_STATUS = List.of(ALLOCATION_EFFECTIVE, "已替代", "已作废");
    public static final String CLAIM_PROJECT = "项目费用";
    public static final String CLAIM_EXAM = "体检费用";
    public static final List<String> CLAIM_TYPES = List.of(CLAIM_PROJECT, CLAIM_EXAM);
    public static final String CLAIM_DRAFT = "草稿";
    public static final String CLAIM_PENDING = "待审";
    public static final String CLAIM_APPROVED = "通过";
    public static final String CLAIM_REJECTED = "驳回";
    public static final List<String> CLAIM_APPROVAL_STATUS = List.of(CLAIM_DRAFT, CLAIM_PENDING, CLAIM_APPROVED, CLAIM_REJECTED);
    public static final String PAYABLE_CONFIRMED = "已确认";
    public static final List<String> PAYABLE_STATUS = List.of("草稿", PAYABLE_CONFIRMED, "已作废");
    public static final String PAYABLE_REIMBURSEMENT = "员工报销";
    public static final List<String> INVOICE_TYPES = List.of("增值税专用发票", "增值税普通发票", "其他");
    public static final List<String> INVOICE_MEDIUMS = List.of("电子", "纸质");
    public static final List<String> INVOICE_COLORS = List.of("蓝字", "红字");
    public static final String INVOICE_ISSUED = "已开具";
    public static final List<String> INVOICE_STATUS = List.of("草稿", INVOICE_ISSUED, "已作废");
    public static final List<String> TAX_TREATMENTS = List.of("适用税率", "免税", "不征税");
    public static final String ALLOCATION_GROSS = "含税";
    public static final String ALLOCATION_NET = "不含税";
    public static final List<String> INVOICE_ALLOCATION_BASIS = List.of(ALLOCATION_GROSS, ALLOCATION_NET);
    public static final List<String> RECEIVABLE_PURPOSES = List.of("预收约定款", "阶段款", "尾款", "质保金", "其他");
    public static final List<String> PAYABLE_PURPOSES = List.of("采购款", "预付约定款", "员工报销", "尾款", "其他");
    public static final String ADJUSTMENT_APPROVED = "已批准";
    public static final List<String> ADJUSTMENT_STATUS = List.of("草稿", ADJUSTMENT_APPROVED, "已作废");
    public static final String PAYMENT_NORMAL = "普通收付";
    public static final String PAYMENT_REFUND = "退款";
    public static final List<String> PAYMENT_KINDS = List.of(PAYMENT_NORMAL, PAYMENT_REFUND);
    public static final List<String> PAYMENT_PURPOSE = List.of("合同款", "员工报销", "预付款", "其他");
    public static final List<String> PAYMENT_CHANNELS = List.of("银行转账", "现金", "支付平台");
    public static final String PAYMENT_CONFIRMED = "已确认";
    public static final List<String> PAYMENT_STATUS = List.of("草稿", PAYMENT_CONFIRMED, "已作废");
    public static final String SETTLEMENT_APPLY = "apply";
    public static final String SETTLEMENT_REVERSE = "reverse";
    public static final List<String> SETTLEMENT_ACTIONS = List.of(SETTLEMENT_APPLY, SETTLEMENT_REVERSE);
    public static final List<String> SETTLEMENT_STATUS = List.of("草稿", "已确认");
    public static final String LEDGER_ACTIVE = "启用";
    public static final List<String> LEDGER_STATUS = List.of(LEDGER_ACTIVE, "停用");
    public static final List<String> LEDGER_TYPES = List.of("公司账簿", "法人账簿");
    public static final String ACCOUNT_ASSET = "资产";
    public static final String ACCOUNT_LIABILITY = "负债";
    public static final String ACCOUNT_EQUITY = "权益";
    public static final String ACCOUNT_INCOME = "收入";
    public static final String ACCOUNT_EXPENSE = "费用";
    public static final List<String> ACCOUNT_CATEGORIES = List.of(
            ACCOUNT_ASSET, ACCOUNT_LIABILITY, ACCOUNT_EQUITY, ACCOUNT_INCOME, ACCOUNT_EXPENSE);
    public static final String BALANCE_DEBIT = "借";
    public static final String BALANCE_CREDIT = "贷";
    public static final List<String> BALANCE_DIRECTIONS = List.of(BALANCE_DEBIT, BALANCE_CREDIT);
    public static final List<String> ACCOUNT_STATUS = List.of(LEDGER_ACTIVE, "停用");
    public static final String PERIOD_OPEN = "开放";
    public static final String PERIOD_CLOSED = "关闭";
    public static final List<String> PERIOD_STATUS = List.of(PERIOD_OPEN, PERIOD_CLOSED);
    public static final String JOURNAL_DRAFT = "草稿";
    public static final String JOURNAL_POSTED = "已过账";
    public static final List<String> JOURNAL_STATUS = List.of(JOURNAL_DRAFT, JOURNAL_POSTED);
    public static final String REVENUE_DRAFT = "草稿";
    public static final String REVENUE_APPROVED = "已审批";
    public static final String REVENUE_VOID = "已作废";
    public static final List<String> REVENUE_STATUS = List.of(REVENUE_DRAFT, REVENUE_APPROVED, REVENUE_VOID);

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
