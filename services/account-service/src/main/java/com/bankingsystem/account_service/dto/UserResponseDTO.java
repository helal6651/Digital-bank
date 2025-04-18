package com.bankingsystem.account_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long userId; // ID of the user

    private String fullName; // Full name of the user

    private String email; // Email of the user

    private String phoneNumber; // Phone number of the user

    private LocalDate dateOfBirth; // Date of birth of the user

    private String address; // Address of the user

    private String role; // Role ('user' or 'admin')

    private List<AccountResponseDTO> accounts; // Associated accounts (if needed)
}