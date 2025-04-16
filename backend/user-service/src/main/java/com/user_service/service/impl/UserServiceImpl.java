package com.user_service.service.impl;

import com.user_service.enums.ResultCodeConstants;
import com.user_service.enums.UserStatus;
import com.user_service.model.converter.UserConverter;
import com.user_service.model.converter.response.PageableResponseConverter;
import com.user_service.model.dto.RegisterRequest;
import com.user_service.model.entity.User;
import com.user_service.repository.UserRepository;
import com.user_service.response.PageableResponseDTO;
import com.user_service.response.user.UserResponse;
import com.user_service.service.UserService;
import com.user_service.service.kafka.KafkaProducer;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.user_service.response.BankingResponseUtil.throwApplicationException;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducerService;
    private final UserConverter userConverter;

    public UserServiceImpl(UserRepository userRepository, KafkaProducer kafkaProducerService, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.userConverter = userConverter;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        User exisUser = userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail());
        if (exisUser != null) {
            throwApplicationException(ResultCodeConstants.ALREADY_EXIST);
        }
        // Generate salt and hash password
        String salt = generateSalt();
        String hashedPassword = hashPassword(request.getPassword(), salt);
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .passwordSalt(salt)
                .status(UserStatus.ACTIVE)
                .mfaEnabled(false)
                .build();
        userRepository.save(user);
        // Send notification email
        LocalDateTime now = LocalDateTime.now();  // Get current date and time

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDateTime = now.format(formatter);
        kafkaProducerService.sendMessage("user-registration-topic "+ request.getEmail()+", "+ formattedDateTime);

        return UserResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }


    public List<UserResponse> readAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .mfaEnabled(user.getMfaEnabled())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build()
        ).collect(Collectors.toList());
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public PageableResponseDTO<UserResponse> getAllUser(Pageable pageable) {
        PageableResponseConverter<User, UserResponse> converter = new PageableResponseConverter<>();
        return converter.convert(userRepository.findAll(pageable), userConverter);
    }
}
