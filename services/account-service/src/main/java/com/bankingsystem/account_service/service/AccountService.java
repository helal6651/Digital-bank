package com.bankingsystem.account_service.service;

import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;

public interface AccountService {
   AccountResponseDTO createAccount(AccountRequestDTO accountRequestDTO);

   AccountResponseDTO getAccountFromDb(String accountNumber) throws Exception;
}
