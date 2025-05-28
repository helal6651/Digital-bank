package com.user_service.service.impl;

import com.common_service.model.entity.UserProfile;
import com.common_service.repository.UserProfileRepository;
import com.user_service.enums.ResultCodeConstants;
import com.user_service.model.dto.UserProfileRequest;
import com.user_service.response.user.UserProfileResponse;
import com.user_service.service.UserProfileService;
import com.user_service.utils.AuthenticationUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static com.user_service.response.BankingResponseUtil.throwApplicationException;


@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final AuthenticationUtils authenticationUtils;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, AuthenticationUtils authenticationUtils) {
        this.userProfileRepository = userProfileRepository;
        this.authenticationUtils = authenticationUtils;
    }

    @Override
    public UserProfileResponse update(UserProfileRequest userProfileRequest) {
        Long userId = authenticationUtils.getLoggedInUserId();
        if (userId == null) {
            throwApplicationException(ResultCodeConstants.UNAUTHORIZED_OPERATION);
        }
        Optional<UserProfile> userProfile = userProfileRepository.findByUserId(userId);
        UserProfile profile = userProfile.orElseGet(UserProfile::new);
        profile.setFirstName(userProfileRequest.getFirstName());
        profile.setLastName(userProfileRequest.getLastName());
        profile.setPhoneNumber(userProfileRequest.getPhoneNumber());
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(userProfileRequest.getBirthDate());
            profile.setBirthDate(date);
        } catch (ParseException e) {
            // Handle the parse exception - either log it or throw a custom exception
            throwApplicationException(ResultCodeConstants.INVALID_DATA);
        }
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);
        return UserProfileResponse.builder()
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .birthDate(String.valueOf(profile.getBirthDate()))
                .governmentId(profile.getGovernmentId())
                .build();
    }
}
