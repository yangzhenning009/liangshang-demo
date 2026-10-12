package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CostOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostOccurrenceRepository extends JpaRepository<CostOccurrence, Long> {

    List<CostOccurrence> findAllByOrderByIncurredAtDescIdDesc();

    List<CostOccurrence> findByWorkLogIdOrderByIdAsc(Long workLogId);

    List<CostOccurrence> findByCostNumber(String costNumber);

    List<CostOccurrence> findBySupplierId(Long supplierId);

    boolean existsByLaborCostRateId(Long laborCostRateId);

    boolean existsBySupplierId(Long supplierId);

    boolean existsByIncurredByEmployeeId(Long employeeId);

    boolean existsByApprovedByEmployeeId(Long employeeId);

    boolean existsByPurchaseContractId(Long contractId);

    boolean existsBySourceDocumentId(Long documentId);

    List<CostOccurrence> findByPayrollEmployeeIdAndPayrollEmploymentIdAndPayrollPeriodAndPayrollBatchNumberAndPayrollLineNumber(
            Long payrollEmployeeId,
            Long payrollEmploymentId,
            String payrollPeriod,
            String payrollBatchNumber,
            Integer payrollLineNumber);
}
