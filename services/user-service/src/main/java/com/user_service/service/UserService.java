package com.user_service.service;

import com.user_service.model.dto.RegisterRequest;
import com.user_service.response.PageableResponseDTO;
import com.user_service.response.user.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface  UserService {
    UserResponse register(RegisterRequest userDto);
    PageableResponseDTO<UserResponse> getAllUser(Pageable pageable);
    UserResponse showUser();
}
