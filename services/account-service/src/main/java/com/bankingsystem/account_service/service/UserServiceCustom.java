package com.bankingsystem.account_service.service;


import com.bankingsystem.account_service.model.SignUp;
import com.common_service.enums.AuthProvider;
import com.common_service.enums.UserRole;
import com.common_service.enums.UserStatus;
import com.common_service.model.entity.Role;
import com.common_service.model.entity.User;
import com.common_service.repository.RoleRepository;
import com.common_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class UserServiceCustom {
    private final UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    public UserServiceCustom(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User createUser(SignUp signupDTO) {
        User user = new User();

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
         user.setUsername(signupDTO.getUserName());
        // Save the user in the repository
        user = userRepository.save((User) user);
        return user;
    }


}
