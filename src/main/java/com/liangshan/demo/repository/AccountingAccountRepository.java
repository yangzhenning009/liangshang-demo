package com.liangshan.demo.repository;

import com.liangshan.demo.domain.AccountingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountingAccountRepository extends JpaRepository<AccountingAccount, Long> {

    List<AccountingAccount> findByLedgerIdOrderByAccountCodeAsc(Long ledgerId);

    List<AccountingAccount> findAllByOrderByAccountCodeAsc();

    List<AccountingAccount> findByParentAccountId(Long parentAccountId);
}
