package com.bankingsystem.account_service.service;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.dto.UserDTO;
import com.bankingsystem.account_service.dto.UserRequestDTO;
import com.bankingsystem.account_service.dto.UserResponseDTO;
import com.bankingsystem.account_service.entity.Account;
import com.bankingsystem.account_service.entity.User;
import com.bankingsystem.account_service.mapper.AccountMapper;
import com.bankingsystem.account_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bankingsystem.account_service.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {


        //Account account = accountMapper.toEntity(accountRequestDTO);
        User user = userMapper.toEntity(userRequestDTO);

//        User user = User.builder()
//                .fullName(userDTO.getName())
//                .email(userDTO.getEmail())
//                .build();
//        User user = UserMapper.toEntity(userRequestDTO);
          User savedUser = userRepository.save(user);
          return userMapper.toResponseDto(savedUser);
    }

    public List<UserResponseDTO> getAllUsersWithAccounts() {
        List<User> users = userRepository.findAll(); // Fetch all users

        return users.stream()
                .map(user -> UserResponseDTO.builder()
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .dateOfBirth(user.getDateOfBirth())
                        .address(user.getAddress())
                        .role(user.getRole())
                        .accounts(user.getAccounts().stream() // Map the accounts of each user
                                .map(account -> accountMapper.toResponseDto(account)
                                ).collect(Collectors.toList()))
                        .build()
                )
                .collect(Collectors.toList()); // Convert to a list of UserResponseDTO
    }

//    private UserDTO mapToDTO(User user) {
//        return UserDTO.builder()
//                .id(user.getUserId())
//                .name(user.getFullName())
//                .email(user.getEmail())
//                .build();
//    }
}