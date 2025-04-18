package com.bankingsystem.account_service.controller;
import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/createaccount")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDTO createAccount(@Valid @RequestBody AccountRequestDTO request) {
        AccountResponseDTO accountService2 = accountService.createAccount(request);
        accountService.sendMessage("account_created_messages", "Account created with ID: " + accountService2.getAccountId());
        return accountService2;
    }
}
