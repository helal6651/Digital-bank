package com.user_service.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String birthDate;
    private String governmentId;
}
