package com.bankingsystem.account_service.model;


import com.common_service.model.entity.User;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDTO {
    private String id;
    private String username;

    public static UserDTO from(User user) {
        return UserDTO.builder()  // Corrected the reference to the builder
                .id(user.getUserId().toString())
                .username(user.getUsername())
                .build();
    }
}