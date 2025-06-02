package com.user_service.service.impl;

import com.common_service.enums.AuthProvider;
import com.common_service.enums.UserRole;
import com.common_service.enums.UserStatus;
import com.common_service.model.entity.Role;
import com.common_service.model.entity.User;
import com.common_service.repository.RoleRepository;
import com.common_service.repository.UserRepository;
import com.user_service.enums.ResultCodeConstants;
import com.user_service.model.converter.UserConverter;
import com.user_service.model.converter.response.PageableResponseConverter;
import com.user_service.model.dto.RegisterRequest;
import com.user_service.response.PageableResponseDTO;
import com.user_service.response.user.UserResponse;
import com.user_service.service.UserService;
import com.user_service.service.kafka.KafkaProducer;
import com.user_service.utils.AuthenticationUtils;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.user_service.response.BankingResponseUtil.throwApplicationException;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducerService;
    private final UserConverter userConverter;
    private final Argon2PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationUtils authenticationUtils;

    public UserServiceImpl(UserRepository userRepository, KafkaProducer kafkaProducerService, UserConverter userConverter,
                           Argon2PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository, AuthenticationUtils authenticationUtils) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.userConverter = userConverter;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationUtils = authenticationUtils;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        User exisUser = userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail());
        if (exisUser != null) {
            throwApplicationException(ResultCodeConstants.ALREADY_EXIST);
        }


        Role userRole = roleRepository.findByName(UserRole.USER.name())
                .orElseThrow(() -> throwApplicationException(ResultCodeConstants.ROLE_NOT_FOUND));

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .mfaEnabled(false)
                .roles(new HashSet<>()) // Initialize the roles set
                .build();

        user.getRoles().add(userRole);
        User savedUser = userRepository.save(user);

        // Send notification email
//        kafkaProducerService.sendAccountActivationNotification(
//                savedUser.getUserId(),
//                savedUser.getEmail(),
//                savedUser.getUsername(),
//                "digital-bank-active-user-en"
//        );
        return userConverter.convert(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponseDTO<UserResponse> getAllUser(Pageable pageable) {
        PageableResponseConverter<User, UserResponse> converter = new PageableResponseConverter<>();
        return converter.convert(userRepository.findAll(pageable), userConverter);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse showUser() {
        return authenticationUtils.getLoggedInUser()
                .map(userConverter::convert)
                .orElseThrow(() -> throwApplicationException(ResultCodeConstants.USER_NOT_FOUND));
    }
}
