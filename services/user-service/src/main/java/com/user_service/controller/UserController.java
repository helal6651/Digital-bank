package com.user_service.controller;

import com.user_service.enums.ResponseType;
import com.user_service.model.dto.RegisterRequest;
import com.user_service.response.BaseResponse;
import com.user_service.service.UserService;
import com.user_service.utils.ApplicationConstants;
import com.user_service.utils.Permissions;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/v1/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public BaseResponse register(@Valid @RequestBody RegisterRequest request) {
        return BaseResponse.builder()
                .responseType(ResponseType.RESULT)
                .message(Collections.singleton(HttpStatus.OK.getReasonPhrase()))
                .result(userService.register(request))
                .code(ApplicationConstants.CREATED_SUCCESS_CODE)
                .build();
    }

    @GetMapping("/show")
    @PreAuthorize("hasAuthority('" + Permissions.USER_PROFILE_SHOW + "')")
    public BaseResponse getUserInfo() {
        return BaseResponse.builder()
                .message(Collections.singletonList(ApplicationConstants.OK_MSG))
                .responseType(ResponseType.RESULT)
                .result(userService.showUser())
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('" + Permissions.USER_LIST + "')")
    public BaseResponse getAllUser(@PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return BaseResponse.builder()
                .message(Collections.singletonList(ApplicationConstants.OK_MSG))
                .responseType(ResponseType.RESULT)
                .result(userService.getAllUser(pageable))
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }
}
