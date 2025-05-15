package com.user_service.model.converter;

import com.common_service.model.entity.Role;
import com.common_service.model.entity.User;
import com.user_service.response.user.UserResponse;
import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserConverter implements Converter<User, UserResponse> {
    @Override
    public UserResponse convert(@Nonnull User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .status(user.getStatus())
                .provider(user.getProvider())
                .mfaEnabled(user.getMfaEnabled())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
