package com.bankingsystem.account_service.service;
import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.entity.Account;
import com.bankingsystem.account_service.entity.User;
import com.bankingsystem.account_service.exception.UserNotFoundException;
import com.bankingsystem.account_service.mapper.AccountMapper;
import com.bankingsystem.account_service.repository.AccountRepository;
import com.bankingsystem.account_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO accountRequestDTO) {
        User user = userRepository.findById(accountRequestDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + accountRequestDTO.getUserId()));
        Account account = accountMapper.toEntity(accountRequestDTO);
        account.setUser(user);
//        Account account = Account.builder()
//                .accountNumber(request.getAccountNumber())
//                .balance(request.getBalance())
//                .user(user)
//                .build();
        account = accountRepository.save(account);

        // Map saved entity to response DTO
        return accountMapper.toResponseDto(account);
    }



    // Method to send message to Kafka topic
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Produced message to topic: " + topic + " | Message: " + message);
    }

}
