package com.bankingsystem.account_service.service;

import com.bankingsystem.account_service.dto.AccountResponseDTO;

public interface RedisServiceForAccount {
   void saveAccount(String accountId, AccountResponseDTO accountResponseDTO);

   Object getAccount(String accountId);

   void deleteAccount(String accountId);

   boolean existsAccount(String accountId);

   void updateAccount(String accountId, String accountData);
}
