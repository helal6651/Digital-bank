package com.user_service.utils;


import com.common_service.model.entity.User;
import com.common_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Component
public class AuthenticationUtils {

    private final UserRepository userRepository;

    public AuthenticationUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the currently logged-in user from the security context
     *
     * @return Optional containing the User if authenticated, empty otherwise
     */
    public Optional<User> getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        log.info("Authentication user: {}", authentication.getName());
        Matcher matcher = ApplicationConstants.emailPattern.matcher(authentication.getName());
        Optional<User> user;
        if (matcher.matches()) {
            log.info("User is email");
            user = userRepository.findByEmail(authentication.getName());
        } else {
            user = userRepository.findByUsername(authentication.getName());
        }
        return user;
    }

    /**
     * Retrieves the ID of the currently logged-in user
     *
     * @return User ID if authenticated, null otherwise
     */
    public Long getLoggedInUserId() {
        return getLoggedInUser()
                .map(User::getUserId)
                .orElse(null);
    }
}