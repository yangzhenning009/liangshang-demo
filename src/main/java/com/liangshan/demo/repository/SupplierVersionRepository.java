package com.liangshan.demo.repository;

import com.liangshan.demo.domain.SupplierVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierVersionRepository extends JpaRepository<SupplierVersion, Long> {

    List<SupplierVersion> findBySupplierIdOrderByVersionNumberAsc(Long supplierId);

    boolean existsByBasisDocumentId(Long documentId);

    boolean existsByChangedByEmployeeId(Long employeeId);
}
