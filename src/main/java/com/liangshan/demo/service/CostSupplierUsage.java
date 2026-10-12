package com.liangshan.demo.service;

import com.liangshan.demo.repository.CostOccurrenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 费用引用供应商档案后，采购公司和供货对象锁定，档案不能删除。
 */
@Component
@RequiredArgsConstructor
public class CostSupplierUsage implements SupplierUsage {

    private final CostOccurrenceRepository costs;

    @Override
    public boolean referencesSupplier(long supplierId) {
        return costs.existsBySupplierId(supplierId);
    }
}
