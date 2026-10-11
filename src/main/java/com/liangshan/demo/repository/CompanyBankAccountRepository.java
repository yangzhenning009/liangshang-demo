package com.liangshan.demo.repository;

import com.liangshan.demo.domain.CompanyBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Long> {

    List<CompanyBankAccount> findByCompanyIdOrderByIdAsc(Long companyId);
}
