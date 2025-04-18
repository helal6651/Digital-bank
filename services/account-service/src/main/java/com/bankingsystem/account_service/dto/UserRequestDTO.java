package com.bankingsystem.account_service.dto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName; // Full name of the user

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email; // Email address of the user

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,20}$", message = "Invalid phone number format") // Supports international numbers
    private String phoneNumber; // Phone number of the user

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth; // Date of birth of the user

    private String address; // Address is optional

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(user|admin)$", message = "Role must be either 'user' or 'admin'")
    private String role; // Role with constraints: only 'user' or 'admin'

}