package com.user_service.model.converter;

import com.user_service.enums.UserStatus;
import com.user_service.model.entity.User;
import com.user_service.response.user.UserResponse;
import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class UserConverter implements Converter<User, UserResponse> {
    @Override
    public UserResponse convert(@Nonnull User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .mfaEnabled(user.getMfaEnabled())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
