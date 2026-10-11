package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findAllByOrderBySupplierCodeAsc();

    List<Supplier> findByPurchasingCompanyId(Long purchasingCompanyId);

    boolean existsByPurchasingCompanyId(Long purchasingCompanyId);

    boolean existsByCompanyId(Long companyId);

    boolean existsByLegalPersonId(Long legalPersonId);

    boolean existsByPersonId(Long personId);

    boolean existsByProcurementOwnerEmployeeId(Long employeeId);

    boolean existsByAdmissionReviewedByEmployeeId(Long employeeId);

    boolean existsByAdmissionDocumentId(Long documentId);
}
