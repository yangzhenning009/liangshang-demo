package com.liangshan.demo.repository;

import com.liangshan.demo.domain.LegalPersonRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalPersonRegistrationRepository extends JpaRepository<LegalPersonRegistration, Long> {

    List<LegalPersonRegistration> findByLegalPersonIdOrderByValidFromAsc(Long legalPersonId);

    List<LegalPersonRegistration> findByUnifiedSocialCreditCode(String unifiedSocialCreditCode);

    List<LegalPersonRegistration> findByRegistrationCertificateNumber(String registrationCertificateNumber);

    List<LegalPersonRegistration> findByTaxpayerIdentificationNumber(String taxpayerIdentificationNumber);

    boolean existsBySourceDocumentId(Long sourceDocumentId);
}
