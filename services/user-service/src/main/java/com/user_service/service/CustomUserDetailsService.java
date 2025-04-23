package com.user_service.service;


import com.user_service.enums.UserStatus;
import com.user_service.model.entity.Permission;
import com.user_service.model.entity.Role;
import com.user_service.model.entity.User;
import com.user_service.repository.UserRepository; // Replace with your actual repository
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username: {}", username);
        // Fetch user from the database
      User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // Add roles with ROLE_ prefix
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Add permissions directly as authorities
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
        }

        log.info("Loaded authorities for user {}: {}", username, authorities);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.getStatus().equals(UserStatus.ACTIVE),
                true, true, true,
                authorities);

        // Map the user entity to Spring Security's UserDetails
  /*      return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities) // Replace with your logic to fetch roles/authorities
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
              //  .disabled(!user.isEnabled())
                .build();*/
    }
}