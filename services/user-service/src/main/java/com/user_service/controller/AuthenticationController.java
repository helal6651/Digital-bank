package com.user_service.controller;


import com.user_service.enums.ResponseType;
import com.user_service.model.dto.LoginRequest;
import com.user_service.model.dto.RefreshTokenRequest;
import com.user_service.response.BaseResponse;
import com.user_service.service.AuthService;
import com.user_service.utils.ApplicationConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * AuthenticationController for handling authentication and token generation.
 * <p>
 * This controller provides endpoints for user authentication using OAuth2 and
 * JWT. After successful authentication, JWT access and refresh tokens are
 * returned to the client.
 * </p>
 *
 * @author BJIT
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/api")
@Validated
public class AuthenticationController {
    private final AuthService authService;

    /**
     * Constructor to inject AuthService dependency.
     *
     * @param authService the service responsible for token generation and
     *                    validation.
     */
    public AuthenticationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/authenticate")
    public BaseResponse authenticate(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        return BaseResponse.builder()
                .responseType(ResponseType.RESULT)
                .message(Collections.singleton(HttpStatus.OK.getReasonPhrase()))
                .result(authService.authenticate(loginRequest))
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }

    @PostMapping ("/renewToken")
    public BaseResponse renewToken (@RequestBody RefreshTokenRequest refreshTokenRequest) throws Exception {
        return BaseResponse.builder ()
                .responseType (ResponseType.RESULT)
                .message (Collections.singleton (HttpStatus.OK.getReasonPhrase ()))
                .result (authService.renewToken (refreshTokenRequest))
                .code (ApplicationConstants.SUCCESS_CODE)
                .build ();
    }
}