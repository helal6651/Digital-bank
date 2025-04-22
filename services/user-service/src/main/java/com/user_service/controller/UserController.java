package com.user_service.controller;

import com.user_service.enums.ResponseType;
import com.user_service.model.dto.RegisterRequest;
import com.user_service.response.BaseResponse;
import com.user_service.response.user.UserResponse;
import com.user_service.service.UserService;
import com.user_service.utils.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public BaseResponse register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("UserController::" + request);
        return BaseResponse.builder()
                .responseType(ResponseType.RESULT)
                .message(Collections.singleton(HttpStatus.OK.getReasonPhrase()))
                .result(userService.register(request))
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }
    @GetMapping("/show")
    public BaseResponse getUserInfo() {
        return BaseResponse.builder()
                .message(Collections.singletonList(ApplicationConstants.OK_MSG))
                .responseType(ResponseType.RESULT)
                .result(userService.showUser())
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }
    @GetMapping("/list")
    public BaseResponse getAllUser(@PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return BaseResponse.builder()
                .message(Collections.singletonList(ApplicationConstants.OK_MSG))
                .responseType(ResponseType.RESULT)
                .result(userService.getAllUser(pageable))
                .code(ApplicationConstants.SUCCESS_CODE)
                .build();
    }
}
