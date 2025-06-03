package com.bankingsystem.account_service.service.impl;

import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.entity.Account;
import com.bankingsystem.account_service.enums.ResultCodeConstants;
import com.bankingsystem.account_service.exception.UserNotFoundException;
import com.bankingsystem.account_service.mapper.AccountMapper;
import com.bankingsystem.account_service.repository.AccountRepository;
import com.bankingsystem.account_service.response.BankingResponseUtil;
import com.bankingsystem.account_service.service.AccountService;
import com.bankingsystem.account_service.service.kafka.KafkaProducer;
import com.common_service.repository.UserRepository;
import com.common_service.model.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {
   private final AccountRepository accountRepository;
   private final UserRepository userRepository;
   private final AccountMapper accountMapper;
   private final KafkaProducer kafkaProducer;
   private final KafkaTemplate<String, String> kafkaTemplate;

   @Transactional
   public AccountResponseDTO createAccount(AccountRequestDTO accountRequestDTO) {
      User user = (User)this.userRepository.findByUserId(accountRequestDTO.getUserId()).orElseThrow(() -> {
         return new UserNotFoundException("User not found with id: " + accountRequestDTO.getUserId());
      });
      Optional<Account> existingAccount = this.accountRepository.findByAccountNumber(accountRequestDTO.getAccountNumber());
      if (existingAccount.isPresent()) {
         System.out.println("Account already exists with account number: " + accountRequestDTO.getAccountNumber());
         BankingResponseUtil.throwApplicationException(ResultCodeConstants.ALREADY_EXIST);
      }

      Account account = this.accountMapper.toEntity(accountRequestDTO);
      account.setUser(user);
      account = (Account)this.accountRepository.save(account);
     // this.kafkaProducer.sendMessage("Account created with ID: " + account.getAccountId());
      return this.accountMapper.toResponseDto(account);
   }

   public AccountResponseDTO getAccountFromDb(String accountNumber) {
      Optional<Account> account = this.accountRepository.findByAccountNumber(accountNumber);
      AccountMapper mapper = this.accountMapper;
      return (AccountResponseDTO)account.map(mapper::toResponseDto).orElseThrow(() -> {
         return new RuntimeException("Account not found with number: " + accountNumber);
      });
   }

   /**
    * Fetch all accounts by user ID.
    *
    * @param userId the ID of the user
    * @return list of AccountResponseDTO
    */
   public List<AccountResponseDTO> getAccountsByUserId(Long userId) {
      // Ensure the user exists before fetching accounts
      User user = this.userRepository.findByUserId(userId).orElseThrow(() -> {
         return new UserNotFoundException("User not found with id: " + userId);
      });

      // Fetch accounts linked to the user
      List<Account> accounts = this.accountRepository.findAllByUser_UserId(userId);
      accounts = accounts != null ? accounts : Collections.emptyList();

      // Map entities to response DTOs
      return accounts.stream()
              .map(this.accountMapper::toResponseDto)
              .toList();
   }
   public AccountServiceImpl(final AccountRepository accountRepository, final UserRepository userRepository, final AccountMapper accountMapper, final KafkaProducer kafkaProducer, final KafkaTemplate<String, String> kafkaTemplate) {
      this.accountRepository = accountRepository;
      this.userRepository = userRepository;
      this.accountMapper = accountMapper;
      this.kafkaProducer = kafkaProducer;
      this.kafkaTemplate = kafkaTemplate;
   }
}
