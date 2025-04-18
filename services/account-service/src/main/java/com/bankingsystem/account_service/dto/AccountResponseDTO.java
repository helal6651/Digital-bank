package com.bankingsystem.account_service.dto;

import com.bankingsystem.account_service.model.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDTO {

    private Long accountId; // Unique ID of the account

    private Long userId; // ID of the user associated with the account

    private String accountNumber; // Unique account number

    private String accountType; // Type of account (e.g., savings, checking)

    private BigDecimal balance; // Current balance of the account

    private Currency currency; // Currency type (e.g., USD, BDT)

    private String status; // Status of the account (e.g., active, inactive, closed)

    private LocalDateTime createdAt; // Timestamp when the account was created

    private LocalDateTime updatedAt; // Timestamp when the account was last updated

    private LocalDateTime deletedAt; // Timestamp when the account was deleted (if applicable)
}