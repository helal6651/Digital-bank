package com.bankingsystem.account_service.service.impl;

import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.entity.Account;
import com.bankingsystem.account_service.entity.User;
import com.bankingsystem.account_service.exception.UserNotFoundException;
import com.bankingsystem.account_service.mapper.AccountMapper;
import com.bankingsystem.account_service.repository.AccountRepository;
import com.bankingsystem.account_service.repository.UserRepository;
import com.bankingsystem.account_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import com.bankingsystem.account_service.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bankingsystem.account_service.exceptions.BankingApplicationException;
import com.bankingsystem.account_service.enums.ResultCodeConstants;
import static com.bankingsystem.account_service.response.BankingResponseUtil.throwApplicationException;


@Service
@RequiredArgsConstructor

public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final KafkaProducer kafkaProducer;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO accountRequestDTO) {
        User user = userRepository.findById(accountRequestDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + accountRequestDTO.getUserId()));

        Account existingAccount = accountRepository.findByAccountNumber(accountRequestDTO.getAccountNumber());

        if (existingAccount != null) {
            System.out.println("Account already exists with account number: " + accountRequestDTO.getAccountNumber());
            throwApplicationException(ResultCodeConstants.ALREADY_EXIST);
        }


        Account account = accountMapper.toEntity(accountRequestDTO);
        account.setUser(user);
//        Account account = Account.builder()
//                .accountNumber(request.getAccountNumber())
//                .balance(request.getBalance())
//                .user(user)
//                .build();
        account = accountRepository.save(account);
        kafkaProducer.sendMessage("Account created with ID: " + account.getAccountId());

        // Map saved entity to response DTO
        return accountMapper.toResponseDto(account);
    }


}

