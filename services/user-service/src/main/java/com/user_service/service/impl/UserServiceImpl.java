package com.user_service.service.impl;

import com.user_service.enums.ResultCodeConstants;
import com.user_service.enums.UserRole;
import com.user_service.enums.UserStatus;
import com.user_service.model.converter.UserConverter;
import com.user_service.model.converter.response.PageableResponseConverter;
import com.user_service.model.dto.RegisterRequest;
import com.user_service.model.entity.Role;
import com.user_service.model.entity.User;
import com.user_service.repository.RoleRepository;
import com.user_service.repository.UserRepository;
import com.user_service.response.PageableResponseDTO;
import com.user_service.response.user.UserResponse;
import com.user_service.service.UserService;
import com.user_service.service.kafka.KafkaProducer;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.user_service.response.BankingResponseUtil.throwApplicationException;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducerService;
    private final UserConverter userConverter;
    private final Argon2PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, KafkaProducer kafkaProducerService, UserConverter userConverter,
                           Argon2PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.userConverter = userConverter;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
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
                .mfaEnabled(false)
                .roles(new HashSet<>()) // Initialize the roles set
                .build();

        user.getRoles().add(userRole);

        userRepository.save(user);
        // Send notification email
        LocalDateTime now = LocalDateTime.now(); // Get current date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
         kafkaProducerService.sendMessage("user-registration-topic " + request.getEmail() + ", " + formattedDateTime);

        return UserResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public PageableResponseDTO<UserResponse> getAllUser(Pageable pageable) {
        PageableResponseConverter<User, UserResponse> converter = new PageableResponseConverter<>();
        return converter.convert(userRepository.findAll(pageable), userConverter);
    }

    @Override
    public UserResponse showUser() {
        Optional<User> user_ = getLoggedInUser();
        if (user_.isPresent()) {

            User user = user_.get();
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return UserResponse.builder()
                    .userId(user.getUserId())
                    .userName(user.getUsername())
                    .email(user.getEmail())
                    .roles(roleNames)
                    .status(user.getStatus())
                    .mfaEnabled(user.getMfaEnabled())
                    .createdAt(user.getCreatedAt())
                    .lastLogin(user.getLastLogin())
                    .build();
        }
        return null;
    }

    public Optional<User> getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }


        // Get the username from the authentication
        String username = authentication.getName();

        // Fetch the user from the repository using the username
        return userRepository.findByUsername(username);
    }

}
