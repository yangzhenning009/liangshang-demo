package com.liangshan.demo.service;

/**
 * 合同、费用、应付实现本接口并注册为 Spring Bean。
 * 任一实现返回 true 时，供应商的采购公司和供货对象不再允许更换，档案也不能删除。
 * 合同同时查看 contract.supplier_id 与历史条款版本上的 supplier_id。
 * 仅有供应商版本或资质不会锁定身份。
 */
public interface SupplierUsage {

    boolean referencesSupplier(long supplierId);
}
