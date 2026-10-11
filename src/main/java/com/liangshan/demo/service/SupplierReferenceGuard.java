package com.liangshan.demo.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupplierReferenceGuard {

    private final List<SupplierUsage> usages;

    public SupplierReferenceGuard(ObjectProvider<SupplierUsage> usages) {
        this.usages = usages.orderedStream().toList();
    }

    public boolean referenced(long supplierId) {
        for (SupplierUsage usage : usages) {
            if (usage.referencesSupplier(supplierId)) {
                return true;
            }
        }
        return false;
    }
}
