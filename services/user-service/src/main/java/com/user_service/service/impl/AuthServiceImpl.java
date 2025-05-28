package com.user_service.service.impl;

import com.common_service.enums.AuthProvider;
import com.common_service.enums.UserRole;
import com.common_service.enums.UserStatus;
import com.common_service.model.entity.Permission;
import com.common_service.model.entity.Role;
import com.common_service.model.entity.User;
import com.common_service.repository.RoleRepository;
import com.common_service.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.user_service.config.JwtSettings;
import com.user_service.config.UserSecretsManager;

import com.user_service.enums.ResultCodeConstants;
import com.user_service.enums.TokenType;
import com.user_service.model.dto.LoginRequest;
import com.user_service.model.dto.RefreshTokenRequest;
import com.user_service.model.dto.SecretDto;
import com.user_service.response.AuthenticationResponseDTO;
import com.user_service.service.AuthService;
import com.user_service.service.JwtTokenService;
import com.user_service.utils.ApplicationConstants;
import com.user_service.utils.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
    private final Messages messages;
    private final GoogleIdTokenVerifier verifier;
    private final RoleRepository roleRepository;

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
                           JwtSettings jwtSettings, UserRepository userRepository, Argon2PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, Messages messages,
                           GoogleIdTokenVerifier verifier, RoleRepository roleRepository) {
        this.tokenService = tokenService;
        this.userSecretsManager = userSecretsManager;
        this.jwtSettings = jwtSettings;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.messages = messages;
        this.verifier = verifier;
        this.roleRepository = roleRepository;
    }

    @Override
    public AuthenticationResponseDTO authenticate(LoginRequest request) throws Exception {
        AuthenticationResponseDTO responseDTO = null;
        responseDTO = switch (request.getType()) {
            case BANKING -> basicAuthentication(request);
            case GOOGLE -> googleAuthentication(request);
            default -> throw new IllegalArgumentException(messages.getInvalidAuthenticationType());
        };
        return responseDTO;

    }

    private AuthenticationResponseDTO basicAuthentication(LoginRequest request) {
        log.info("User name: {}", request.getUsername());
        if (StringUtils.isEmpty(request.getUsername())) {
            throwApplicationException(ResultCodeConstants.WRONG_CREDENTIALS);
        } else if (StringUtils.isEmpty(request.getPassword())
                || !request.getPassword().matches(ApplicationConstants.VALID_PASSWORD_REGEX)) {
            throwApplicationException(ResultCodeConstants.WRONG_CREDENTIALS);
        }
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
                .accessToken(tokenService.generateToken(authentication.getName(), jwtSettings.getTokenExpirationTime(), jwtSettings.getTokenIssuer(),
                        TokenType.ACCESS, intRSAKeyVersion, ApplicationConstants.USER_ADD, authorities))
                .refreshToken(tokenService.generateToken(authentication.getName(), jwtSettings.getRefreshTokenExpTime(), jwtSettings.getTokenIssuer(),
                        TokenType.REFRESH, intRSAKeyVersion, ApplicationConstants.TOKEN_RENEW, authorities))
                .build();
    }

    private AuthenticationResponseDTO googleAuthentication(LoginRequest request) throws GeneralSecurityException, IOException {


        GoogleIdToken googleIdToken = verifier.verify(request.getSnsAccessToken());
        if (googleIdToken == null) {
            return null;
        }
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        String providerId = payload.getSubject();
        String name = (String) payload.get("name");
        String email = payload.getEmail();
        Role userRole = roleRepository.findByName(UserRole.USER.name())
                .orElseThrow(() -> throwApplicationException(com.user_service.enums.ResultCodeConstants.ROLE_NOT_FOUND));
        Collection<String> authorities = userRole.getPermissions().stream()
                .map(Permission::getName).toList();
        Optional<User> userOptional = userRepository.findByProviderId(providerId);
        User user;
        if (userOptional.isPresent()) {
            // User already exists, potentially update name if changed
            user = userOptional.get();
            if (!user.getUsername().equals(name)) {
                user.setUsername(name);
                log.info("Updating existing user's name. ProviderId: {}, New Name: {}", providerId, name);
                user = userRepository.save(user); // Save the updated user
            }
        } else {
            // Register new user
            log.info("Registering new user. ProviderId: {}, Email: {}", providerId, email);
            // Log role details to verify permissions
            log.info("Role found: {}", userRole.getName());
            log.info("Role permissions: {}", authorities);
            user = User.builder()
                    .username(name)
                    .email(email)
                    .passwordHash(UUID.randomUUID().toString())
                    .status(UserStatus.ACTIVE)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .mfaEnabled(false)
                    .roles(new HashSet<>())
                    .build();
            user.getRoles().add(userRole);
            user = userRepository.save(user); // Save the new user
        }
        SecretDto secretDto = userSecretsManager.getSecretDto();
        Integer intRSAKeyVersion = secretDto.getMetadata().getVersion();
        System.out.println("intRSAKeyVersion in AuthService impl: " + intRSAKeyVersion);
        return AuthenticationResponseDTO.builder()
                .accessToken(tokenService.generateToken(user.getUsername(), jwtSettings.getTokenExpirationTime(), jwtSettings.getTokenIssuer(),
                        TokenType.ACCESS, intRSAKeyVersion, ApplicationConstants.USER_ADD, authorities))
                .refreshToken(tokenService.generateToken(user.getUsername(), jwtSettings.getRefreshTokenExpTime(), jwtSettings.getTokenIssuer(),
                        TokenType.REFRESH, intRSAKeyVersion, ApplicationConstants.TOKEN_RENEW, authorities))
                .build();

    }

    /**
     * Renews the access token using a valid refresh token.
     *
     * @param refreshTokenRequest the request containing the refresh token
     * @return an {@link AuthenticationResponseDTO} containing the new access token
     * and the existing refresh token
     */
    @Override
    public AuthenticationResponseDTO renewToken(RefreshTokenRequest refreshTokenRequest) throws Exception {
        // Validate the refresh token and retrieve authentication
        var authentication = tokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Refresh token User authorities:: {}", authorities + ", " + authentication.getName());
        // Generate new access token
        return AuthenticationResponseDTO.builder().accessToken(
                        tokenService.generateToken(authentication.getName(), jwtSettings.getTokenExpirationTime(), jwtSettings.getTokenIssuer(),
                                TokenType.ACCESS, userSecretsManager.getSecretDto().getMetadata().getVersion(), ApplicationConstants.USER_ADD, authorities))
                .refreshToken(refreshTokenRequest.getRefreshToken()).build();
    }

}