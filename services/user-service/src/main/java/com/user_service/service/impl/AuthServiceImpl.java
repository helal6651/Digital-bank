package com.user_service.service.impl;

import com.common_service.enums.UserStatus;
import com.common_service.model.entity.User;
import com.common_service.repository.UserRepository;
import com.user_service.config.JwtSettings;
import com.user_service.config.UserSecretsManager;

import com.user_service.enums.TokenType;
import com.user_service.model.dto.LoginRequest;
import com.user_service.model.dto.SecretDto;
import com.user_service.response.AuthenticationResponseDTO;
import com.user_service.service.AuthService;
import com.user_service.service.CustomUserDetailsService;
import com.user_service.service.JwtTokenService;
import com.user_service.utils.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.user_service.enums.ResultCodeConstants.ResultCodeConstants;
import static com.user_service.response.BankingResponseUtil.throwApplicationException;


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
    private final UserRepository userRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

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
                           JwtSettings jwtSettings, UserRepository userRepository, Argon2PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userSecretsManager = userSecretsManager;
        this.jwtSettings = jwtSettings;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthenticationResponseDTO authenticate(LoginRequest request) throws Exception {
        log.info("User name: {}", request.getUsername());
        SecretDto secretDto = userSecretsManager.getSecretDto();
        Matcher matcher = ApplicationConstants.emailPattern.matcher(request.getUsername());
        Optional<User> userModel = null;
        if (matcher.matches()) {
            log.info("User is email");
            userModel = userRepository.findByEmail(request.getUsername());
        } else {
            userModel = userRepository.findByUsername(request.getUsername());
        }
        log.info("User name: {}", request.getUsername());

        User user = userModel
                .orElseThrow(() -> throwApplicationException(ResultCodeConstants.WRONG_CREDENTIALS));

        log.info("User status: {}", user.getStatus());

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throwApplicationException(ResultCodeConstants.AUTH_FAILURE);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throwApplicationException(ResultCodeConstants.WRONG_CREDENTIALS);
        }
        System.out.println("User password hash: " + user.getPasswordHash());
        // Create authentication token
        Integer intRSAKeyVersion = secretDto.getMetadata().getVersion();
        log.info("intRSAKeyVersion in AuthService impl: {}", intRSAKeyVersion);
        LocalDateTime now = LocalDateTime.now();
        user.setLastLogin(now);
        userRepository.save(user);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                       user.getUsername(),
                        request.getPassword()
                )
        );

        // Get authorities from the authenticated user
        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("User authorities:: {}", authorities);

        return AuthenticationResponseDTO.builder()
                .accessToken(tokenService.generateToken(authentication, jwtSettings.getTokenExpirationTime(), jwtSettings.getTokenIssuer(),
                        TokenType.ACCESS, intRSAKeyVersion, ApplicationConstants.USER_ADD, authorities))
                .refreshToken(tokenService.generateToken(authentication, jwtSettings.getRefreshTokenExpTime(), jwtSettings.getTokenIssuer(),
                        TokenType.REFRESH, intRSAKeyVersion, ApplicationConstants.TOKEN_RENEW, authorities))
                .build();
    }
}