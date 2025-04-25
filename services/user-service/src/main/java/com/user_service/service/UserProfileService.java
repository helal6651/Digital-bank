package com.user_service.service;

import com.user_service.model.dto.UserProfileRequest;
import com.user_service.model.entity.UserProfile;
import com.user_service.response.user.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse update(UserProfileRequest userProfileRequest);
}
