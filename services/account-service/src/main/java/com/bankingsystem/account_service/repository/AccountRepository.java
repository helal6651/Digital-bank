package com.bankingsystem.account_service.repository;
import com.bankingsystem.account_service.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}