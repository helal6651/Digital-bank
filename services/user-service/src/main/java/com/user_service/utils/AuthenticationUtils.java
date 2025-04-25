package com.user_service.utils;


import com.user_service.model.entity.User;
import com.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

        return userRepository.findByUsername(authentication.getName());
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