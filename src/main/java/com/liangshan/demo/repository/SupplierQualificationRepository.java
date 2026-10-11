package com.liangshan.demo.repository;

import com.liangshan.demo.domain.SupplierQualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierQualificationRepository extends JpaRepository<SupplierQualification, Long> {

    List<SupplierQualification> findBySupplierIdOrderByIdAsc(Long supplierId);

    boolean existsByQualificationDocumentId(Long documentId);

    boolean existsByReviewedByEmployeeId(Long employeeId);
}
