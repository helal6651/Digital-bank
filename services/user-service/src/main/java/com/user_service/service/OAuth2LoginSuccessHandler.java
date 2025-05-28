/*
package com.user_service.service;

import com.common_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.config.JwtSettings;
import com.user_service.config.UserSecretsManager;
import com.user_service.enums.TokenType;
import com.user_service.model.dto.SecretDto;
import com.user_service.response.AuthenticationResponseDTO;
import com.user_service.utils.ApplicationConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserSecretsManager userSecretsManager;
    private final JwtTokenService tokenService;
    private final JwtSettings jwtSettings;
    private final ObjectMapper objectMapper; // For JSON serialization
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserSecretsManager userSecretsManager, JwtTokenService tokenService, JwtSettings jwtSettings,
                                     ObjectMapper objectMapper, @Lazy AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.userSecretsManager = userSecretsManager;
        this.tokenService = tokenService;
        this.jwtSettings = jwtSettings;
        this.objectMapper = objectMapper;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        //CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
       // User user = oauth2User.getUser();

        // Get authorities from the authentication object
        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Generate tokens with correct authorities
        AuthenticationResponseDTO authResponse = buildAuthenticationResponse(
                authentication,
                authorities
        );

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), authResponse);
    }
    private AuthenticationResponseDTO buildAuthenticationResponse(
            Authentication authentication,
            Collection<String> authorities
    ) {
        SecretDto secretDto = userSecretsManager.getSecretDto();

        String accessToken = tokenService.generateToken(
                authentication,
                jwtSettings.getTokenExpirationTime(),
                jwtSettings.getTokenIssuer(),
                TokenType.ACCESS,
                secretDto.getMetadata().getVersion(),
                ApplicationConstants.USER_ADD,
                authorities
        );

        String refreshToken = tokenService.generateToken(
                authentication,
                jwtSettings.getRefreshTokenExpTime(),
                jwtSettings.getTokenIssuer(),
                TokenType.REFRESH,
                secretDto.getMetadata().getVersion(),
                ApplicationConstants.TOKEN_RENEW,
                authorities
        );

        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}*/
