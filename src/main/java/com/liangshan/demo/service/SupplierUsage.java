package com.liangshan.demo.service;

/**
 * 后续工作包（合同、费用、应付）实现本接口并注册为 Spring Bean。
 * 任一实现返回 true 时，供应商的采购公司和供货对象不再允许更换，档案也不能删除。
 * 当前没有业务引用实现，因此仅有版本或资质不会锁定身份。
 */
public interface SupplierUsage {

    boolean referencesSupplier(long supplierId);
}
