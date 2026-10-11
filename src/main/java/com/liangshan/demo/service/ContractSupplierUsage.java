package com.liangshan.demo.service;

import com.liangshan.demo.repository.ContractRepository;
import com.liangshan.demo.repository.ContractTermVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 合同引用供应商档案。当前条款和已关闭的条款版本都算引用，避免换版后放行旧档案。
 */
@Component
@RequiredArgsConstructor
public class ContractSupplierUsage implements SupplierUsage {

    private final ContractRepository contracts;
    private final ContractTermVersionRepository terms;

    @Override
    public boolean referencesSupplier(long supplierId) {
        return contracts.existsBySupplierId(supplierId) || terms.existsBySupplierId(supplierId);
    }
}
