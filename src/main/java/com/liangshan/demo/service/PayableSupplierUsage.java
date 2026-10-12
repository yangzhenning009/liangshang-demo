package com.liangshan.demo.service;

import com.liangshan.demo.repository.PayableItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 应付引用了供应商档案后，采购公司和供货对象不能更换，档案也不能删除。
 */
@Component
@RequiredArgsConstructor
public class PayableSupplierUsage implements SupplierUsage {

    private final PayableItemRepository payables;

    @Override
    public boolean referencesSupplier(long supplierId) {
        return payables.existsBySupplierId(supplierId);
    }
}
