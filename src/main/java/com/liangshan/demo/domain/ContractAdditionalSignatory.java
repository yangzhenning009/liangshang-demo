package com.liangshan.demo.domain;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * 买方和卖方以外的签署方。签署对象恰好一个，由应用层校验。
 */
@Entity
@Table(name = "contract_additional_signatory", indexes = {
        @Index(name = "idx_additional_signatory_contract", columnList = "contract_id"),
        @Index(name = "idx_additional_signatory_company", columnList = "signatory_company_id"),
        @Index(name = "idx_additional_signatory_document", columnList = "basis_document_id")
})
@Getter
public class ContractAdditionalSignatory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    public Long id;

    @Column(name = "contract_id", nullable = false)
    public Long contractId;

    @Column(name = "signatory_company_id")
    public Long signatoryCompanyId;

    @Column(name = "signatory_legal_person_id")
    public Long signatoryLegalPersonId;

    @Column(name = "signatory_person_id")
    public Long signatoryPersonId;

    @Column(name = "signatory_role", nullable = false, length = 32)
    public String signatoryRole;

    @Column(name = "responsibility_description", nullable = false, length = 500)
    public String responsibilityDescription;

    @Column(name = "valid_from", nullable = false)
    public LocalDate validFrom;

    @Column(name = "valid_to")
    public LocalDate validTo;

    @Column(name = "basis_document_id")
    public Long basisDocumentId;
}
