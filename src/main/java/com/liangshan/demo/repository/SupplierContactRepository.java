package com.liangshan.demo.repository;

import com.liangshan.demo.domain.SupplierContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierContactRepository extends JpaRepository<SupplierContact, Long> {

    List<SupplierContact> findBySupplierIdOrderByIdAsc(Long supplierId);

    boolean existsByPersonId(Long personId);
}
