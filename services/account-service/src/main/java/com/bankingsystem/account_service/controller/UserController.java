package com.bankingsystem.account_service.controller;
import com.bankingsystem.account_service.dto.*;
import com.bankingsystem.account_service.service.AccountService;
import com.bankingsystem.account_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/createuser")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO userRequestDTODTO) {
        return userService.createUser(userRequestDTODTO);
    }
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDTO> getAllUsersWithAccounts() {
        return userService.getAllUsersWithAccounts();
    }

}