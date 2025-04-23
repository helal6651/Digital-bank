package com.bankingsystem.account_service.controller;
import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.bankingsystem.account_service.response.BaseResponse;
import com.bankingsystem.account_service.enums.ResponseType;
import com.bankingsystem.account_service.utils.ApplicationConstants;

import java.util.Collections;

@RestController
@RequestMapping("/api/accounts")

public class AccountController {

    private final AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse createAccount(@Valid @RequestBody AccountRequestDTO request) {
        System.out.println("UserController::" + request);
        return BaseResponse.builder()
                .responseType(ResponseType.RESULT)
                .message(Collections.singleton(HttpStatus.OK.getReasonPhrase()))
                .result(accountService.createAccount(request))
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }



}
