package com.bankingsystem.account_service.mapper;

import com.bankingsystem.account_service.dto.AccountRequestDTO;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    // Map AccountRequestDTO to Account entity
    @Mapping(source = "userId", target = "user.userId") // Maps userId from DTO to the User object in Account
    Account toEntity(AccountRequestDTO accountRequestDTO);

    // Map Account entity to AccountResponseDTO
    @Mapping(source = "user.userId", target = "userId") // Extracts userId from the User object in Account
    AccountResponseDTO toResponseDto(Account account);
}