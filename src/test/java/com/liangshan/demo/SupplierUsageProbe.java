package com.liangshan.demo;

import com.liangshan.demo.service.SupplierUsage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
public class SupplierUsageProbe {

    public static final Set<Long> REFERENCED = ConcurrentHashMap.newKeySet();

    @Bean
    SupplierUsage supplierUsageProbe() {
        return REFERENCED::contains;
    }
}
