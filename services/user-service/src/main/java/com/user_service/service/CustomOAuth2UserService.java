/*
package com.user_service.service;


import com.common_service.enums.AuthProvider;
import com.common_service.enums.UserRole;
import com.common_service.enums.UserStatus;
import com.common_service.model.entity.Permission;
import com.common_service.model.entity.Role;
import com.common_service.model.entity.User;
import com.common_service.repository.RoleRepository;
import com.common_service.repository.UserRepository;
import com.user_service.enums.ResultCodeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.user_service.response.BankingResponseUtil.throwApplicationException;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository, Argon2PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // Ensure database operations are atomic
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Load the user info from Google using the default service
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 2. Extract required attributes (adapt based on provider if needed)
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        // Google's unique ID is typically in the 'sub' (subject) claim
        String providerId = oauth2User.getName(); // .getName() usually maps to 'sub'

        // Log extracted details for debugging
        log.info("OAuth2 User Attributes: {}", attributes);
        log.info("Extracted Email: {}, Name: {}, ProviderId (sub): {}", email, name, providerId);

        if (email == null || providerId == null) {
            log.error("Could not extract required attributes (email/sub) from OAuth2 provider. Attributes: {}", attributes);
            // You might want to throw a more specific exception or handle this case gracefully
            throw new OAuth2AuthenticationException("Missing required user attributes (email/sub) from OAuth2 provider.");
        }

        // 3. Process the user (register or update)
        User user = processOAuth2User(email, name, providerId);
        log.info("Processed user: {}", user); // Log the user details after processing

        // 4. Return the original OAuth2User (Spring Security uses it)
        // Spring Security will use the attributes and authorities from this object.
        // Our main goal here was to persist/update the user in our DB.

        return new CustomOAuth2User(user, oauth2User.getAttributes());
    }

    private User processOAuth2User(String email, String name, String providerId) {
        // Find user by the unique provider ID
        Optional<User> userOptional = userRepository.findByProviderId(providerId);
        User user;
        if (userOptional.isPresent()) {
            // User already exists, potentially update name if changed
            user = userOptional.get();
            if (!user.getUsername().equals(name)) {
                user.setUsername(name);
                log.info("Updating existing user's name. ProviderId: {}, New Name: {}", providerId, name);
                user = userRepository.save(user); // Save the updated user
            } else {
                log.info("User already exists and is up-to-date. ProviderId: {}", providerId);
            }
        } else {
            // Register new user
            log.info("Registering new user. ProviderId: {}, Email: {}", providerId, email);

            Role userRole = roleRepository.findByName(UserRole.USER.name())
                    .orElseThrow(() -> throwApplicationException(ResultCodeConstants.ROLE_NOT_FOUND));
            String hashedPassword = passwordEncoder.encode(name);
            log.info("USER role cached during service initialization");

            // Log role details to verify permissions
            log.info("Role found: {}", userRole.getName());
            log.info("Role permissions: {}", userRole.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toList()));
            user = User.builder()
                    .username(name)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .status(UserStatus.ACTIVE)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .mfaEnabled(false)
                    .roles(new HashSet<>())
                    .build();
            user.getRoles().add(userRole);
            user = userRepository.save(user); // Save the new user
        }
        return user;
    }
}*/
