package com.user_service.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserProfileRequest {
    private long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String birthDate;
    private String governmentId;

}
