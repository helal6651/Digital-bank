/*
package com.user_service.service;

import com.common_service.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;

        // Collect both roles and permissions as authorities
        Set<GrantedAuthority> userAuthorities = new HashSet<>();

        // Add roles
        user.getRoles().forEach(role -> {
            userAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Add permissions from each role
            role.getPermissions().forEach(permission -> {
                userAuthorities.add(new SimpleGrantedAuthority(permission.getName()));
            });
        });

        this.authorities = userAuthorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public User getUser() {
        return user;
    }
}*/
