package com.user_service.response.user;

import com.common_service.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String userName;
    private String email;
    UserStatus status;
    private Set<String> roles;
    private Boolean mfaEnabled;
    LocalDateTime createdAt;
    LocalDateTime lastLogin;

}
