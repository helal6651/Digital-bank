package com.bankingsystem.account_service.controller;

import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.enums.ResponseType;
import com.bankingsystem.account_service.mapper.AccountMapper;
import com.bankingsystem.account_service.response.BaseResponse;
import com.bankingsystem.account_service.service.AccountService;
import com.bankingsystem.account_service.service.RedisServiceForAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/accounts"})
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final RedisServiceForAccount redisService;
    private final AccountMapper accountMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public AccountController(AccountService accountService, RedisServiceForAccount redisService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.redisService = redisService;
        this.accountMapper = accountMapper;
    }

    @PostMapping({"/create"})
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse createAccount(@Valid @RequestBody AccountRequestDTO request) {
        logger.info("Received request to create account: {}", request);

        try {
            AccountResponseDTO accountResponseDTO = this.accountService.createAccount(request);
            this.saveAccountInRedis(accountResponseDTO);
            return BaseResponse.builder().responseType(ResponseType.RESULT).message(Collections.singleton(HttpStatus.CREATED.getReasonPhrase())).result(accountResponseDTO).code("200").build();
        } catch (Exception var3) {
            logger.error("Error while creating account: {}", var3.getMessage(), var3);
            return BaseResponse.builder().responseType(ResponseType.ERROR).message(Collections.singleton("Failed to create account")).result((Object)null).code("404").build();
        }
    }

    @GetMapping({"/{accountNumber}"})
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse getAccount(@PathVariable String accountNumber) {
        logger.info("Fetching account with accountNumber: {}", accountNumber);

        try {
            AccountResponseDTO accountResponseDTO = this.getAccountFromCacheOrDb(accountNumber);
            return BaseResponse.builder().responseType(ResponseType.RESULT).message(Collections.singleton(HttpStatus.OK.getReasonPhrase())).result(accountResponseDTO).code("200").build();
        } catch (Exception var3) {
            logger.error("Error while fetching account: {}", var3.getMessage(), var3);
            return BaseResponse.builder().responseType(ResponseType.ERROR).message(Collections.singleton("Failed to fetch account")).result((Object)null).code("404").build();
        }
    }

    private void saveAccountInRedis(AccountResponseDTO accountResponseDTO) {
        try {
            String accountNumber = accountResponseDTO.getAccountNumber();
            this.redisService.saveAccount(accountNumber, accountResponseDTO);
            logger.info("Account saved in Redis : {}", accountNumber);
        } catch (Exception var3) {
            logger.error("Failed to save account in Redis: {}", var3.getMessage(), var3);
        }

    }

    private AccountResponseDTO getAccountFromCacheOrDb(String accountNumber) throws Exception {
        AccountResponseDTO accountResponseDTO = null;

        try {
            Object rawData = this.redisTemplate.opsForValue().get(accountNumber);
            logger.info("Raw data from Redis: {}", rawData);
            if (rawData instanceof LinkedHashMap) {
                logger.info("Raw data is a type of LinkedHashMap");
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                accountResponseDTO = (AccountResponseDTO)mapper.convertValue(rawData, AccountResponseDTO.class);
            } else if (rawData instanceof AccountResponseDTO) {
                logger.info("Raw data is a type of AccountResponseDTO");
                accountResponseDTO = (AccountResponseDTO)rawData;
            }
        } catch (Exception var6) {
            logger.error("Error connecting to Redis: {}", var6.getMessage(), var6);
        }

        if (accountResponseDTO == null) {
            logger.info("Account not found in Redis or Redis unavailable, fetching from DB");
            accountResponseDTO = this.accountService.getAccountFromDb(accountNumber);

            try {
                this.redisService.saveAccount(accountNumber, accountResponseDTO);
                logger.info("Account saved to Redis after fetching from DB: {}", accountNumber);
            } catch (Exception var5) {
                logger.error("Failed to save account in Redis: {}", var5.getMessage(), var5);
            }
        }

        return accountResponseDTO;
    }
}