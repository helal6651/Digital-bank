package com.user_service.model.dto;

import com.user_service.constants.ErrorCode;
import com.user_service.validators.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = ErrorCode.USERNAME_LENGTH)
    private String username;
    @NotBlank(message = "Email is required")
    @Email(message = ErrorCode.INVALID_EMAIL)
    private String email;

    @ValidPassword(message = ErrorCode.INVALID_PASSWORD_PATTERN)
    private String password;
}
