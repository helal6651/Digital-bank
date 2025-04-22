package com.user_service.service.impl;

import com.user_service.config.JwtSettings;
import com.user_service.config.UserSecretsManager;

import com.user_service.enums.TokenType;
import com.user_service.enums.UserStatus;
import com.user_service.model.dto.LoginRequest;
import com.user_service.model.dto.SecretDto;
import com.user_service.model.entity.User;
import com.user_service.repository.UserRepository;
import com.user_service.response.AuthenticationResponseDTO;
import com.user_service.service.AuthService;
import com.user_service.service.JwtTokenService;
import com.user_service.utils.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.user_service.enums.ResultCodeConstants.ResultCodeConstants;
import static com.user_service.response.BankingResponseUtil.throwApplicationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Implementation of the {@link AuthService} interface responsible for handling
 * authentication and token management.
 * <p>
 * This class provides functionality for:
 * <ul>
 * <li>Authenticating users and generating JWT access and refresh tokens.</li>
 * <li>Validating and renewing refresh tokens.</li>
 * </ul>
 *
 * @author BJIT
 * @version 1.0
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final JwtTokenService tokenService;
    private final UserSecretsManager userSecretsManager;
    private final JwtSettings jwtSettings;
    private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final UserRepository userRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    /**
     * Constructor to initialize dependencies.
     *
     * @param tokenService       the service used for JWT token generation and
     *                           validation
     * @param userSecretsManager the secrets manager for retrieving valid
     *                           credentials
     * @param jwtSettings        the JWT settings, including expiration times
     *                           for tokens
     */
    public AuthServiceImpl(JwtTokenService tokenService, UserSecretsManager userSecretsManager,
                           JwtSettings jwtSettings, UserRepository userRepository, Argon2PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userSecretsManager = userSecretsManager;
        this.jwtSettings = jwtSettings;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthenticationResponseDTO authenticate(LoginRequest request) throws IOException {
        log.info("User name: {}", request.getUsername());
        SecretDto secretDto = userSecretsManager.getSecretDto();
        Matcher matcher = emailPattern.matcher(request.getUsername());
        Optional<User> userModel = null;
        if (matcher.matches()) {
            userModel = userRepository.findByEmail(request.getUsername());
        } else {
            userModel = userRepository.findByUsername(request.getUsername());
        }

        User user = userModel
                .orElseThrow(() -> throwApplicationException(ResultCodeConstants.AUTH_FAILURE));

        log.info("User status: {}", user.getStatus());

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throwApplicationException(ResultCodeConstants.AUTH_FAILURE);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throwApplicationException(ResultCodeConstants.AUTH_FAILURE);
        }

        // Create authentication token
        var authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Integer intRSAKeyVersion = secretDto.getMetadata().getVersion();
        log.info("intRSAKeyVersion in AuthService impl: {}", intRSAKeyVersion);
        LocalDateTime now = LocalDateTime.now();
        user.setLastLogin(now);
        userRepository.save(user);
        return AuthenticationResponseDTO.builder()
                .accessToken(tokenService.generateToken(authenticationToken, jwtSettings.getTokenExpirationTime(), jwtSettings.getTokenIssuer(),
                        TokenType.ACCESS, intRSAKeyVersion, ApplicationConstants.USER_ADD))
                .refreshToken(tokenService.generateToken(authenticationToken, jwtSettings.getRefreshTokenExpTime(), jwtSettings.getTokenIssuer(),
                        TokenType.REFRESH, intRSAKeyVersion, ApplicationConstants.TOKEN_RENEW))
                .build();
    }


}