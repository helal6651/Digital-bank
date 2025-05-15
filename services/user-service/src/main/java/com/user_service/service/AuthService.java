package com.user_service.service;



import com.user_service.model.dto.LoginRequest;
import com.user_service.model.dto.RefreshTokenRequest;
import com.user_service.response.AuthenticationResponseDTO;

import java.io.IOException;

/**
 * AuthService interface for handling authentication and token management operations.
 * <p>
 * This interface defines methods for:
 * <ul>
 *     <li>Authenticating users and generating JWT access and refresh tokens.</li>
 *     <li>Renewing access tokens using a valid refresh token.</li>
 * </ul>
 */
public interface AuthService {
    AuthenticationResponseDTO authenticate (LoginRequest request) throws Exception;

    AuthenticationResponseDTO renewToken (RefreshTokenRequest refreshTokenRequest) throws Exception;

}
