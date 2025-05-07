package com.bankingsystem.account_service.repository;

import com.bankingsystem.account_service.entity.Account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
   Optional<Account> findByAccountNumber(String accountNumber);

   Account findByAccountId(Long accountId);

   boolean existsByAccountNumber(String accountNumber);

   boolean existsByAccountId(Long accountId);
   List<Account> findAllByUser_Id(Long userId);

}
