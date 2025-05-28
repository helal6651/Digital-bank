package com.user_service.model.dto;

import com.user_service.constants.ErrorCode;
import com.user_service.enums.AuthenticationType;
import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * DTO for user authentication requests.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    /**
     * The username of the user attempting to authenticate.
     */
    private String username;
    /**
     * The password of the user attempting to authenticate.
     */
    private String password;

    @NotNull(message = ErrorCode.INVALID_ARGUMENT)
    private AuthenticationType type;

    private String snsAccessToken;

}
