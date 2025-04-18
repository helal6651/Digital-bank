package com.bankingsystem.account_service.mapper;

import com.bankingsystem.account_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.bankingsystem.account_service.dto.UserRequestDTO;
import com.bankingsystem.account_service.dto.UserResponseDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Map UserRequestDTO to User entity
    User toEntity(UserRequestDTO userRequestDTO);

    // Map User entity to UserResponseDTO
    @Mapping(source = "accounts", target = "accounts")
    UserResponseDTO toResponseDto(User user);
}