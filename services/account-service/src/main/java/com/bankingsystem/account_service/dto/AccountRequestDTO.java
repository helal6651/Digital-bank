package com.bankingsystem.account_service.dto;

import com.bankingsystem.account_service.model.Currency;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId; // The user to whom the account will be associated

    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number must not exceed 20 characters")
    private String accountNumber; // Unique account number

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(savings|checking)$", message = "Account type must be either 'savings' or 'checking'")
    private String accountType; // Type of account (e.g., savings, checking)

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be greater than or equal to 0.0")
    @Digits(integer = 15, fraction = 2, message = "Balance must have up to 15 digits and 2 decimal places")
    private BigDecimal balance; // Initial account balance

    @NotNull(message = "Currency is required")
    private Currency currency; // Currency type (e.g., USD, BDT)

    @NotBlank(message = "Account status is required")
    @Pattern(regexp = "^(active|inactive|closed)$", message = "Status must be 'active', 'inactive', or 'closed'")
    private String status; // Status of the account
}