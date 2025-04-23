package com.user_service.controller;

import com.user_service.enums.ResponseType;
import com.user_service.model.dto.RegisterRequest;
import com.user_service.model.dto.UserProfileRequest;
import com.user_service.response.BaseResponse;
import com.user_service.service.UserProfileService;
import com.user_service.utils.ApplicationConstants;
import com.user_service.utils.Permissions;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/v1/api/user/profile")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.USER_PROFILE_UPDATE + "')")
    public BaseResponse update(@Valid @RequestBody UserProfileRequest request) {
        return BaseResponse.builder()
                .responseType(ResponseType.RESULT)
                .message(Collections.singleton(HttpStatus.OK.getReasonPhrase()))
                .result(userProfileService.update(request))
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }
}
