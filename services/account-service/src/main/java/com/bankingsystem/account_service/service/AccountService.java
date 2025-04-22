package com.bankingsystem.account_service.service;
import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import org.springframework.transaction.annotation.Transactional;


public interface AccountService {

    public AccountResponseDTO createAccount(AccountRequestDTO accountRequestDTO);

}
