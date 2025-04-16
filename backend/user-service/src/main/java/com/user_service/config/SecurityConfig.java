package com.user_service.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration class for the Spring Boot application.
 * <p>
 * Configures security settings, including authentication mechanisms, password
 * encoding, JWT token management, and session management. It also sets up
 * filters for handling JWT authentication and access control rules.
 * </p>
 *
 * @author BJIT
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            "/v1/user/**"};





    /**
     * Configures the security filter chain.
     * <p>
     * Defines access control rules, session management, CSRF protection, and custom
     * filters.
     * </p>
     *
     * @param httpSecurity the {@link HttpSecurity} instance.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if configuration fails.
     */
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception {
        log.info ("Configuring security filter chain");
        return httpSecurity
                .authorizeHttpRequests (auth -> auth
                        .requestMatchers (AUTH_WHITELIST).permitAll ()
                        .requestMatchers ("api/**").authenticated ())
                .csrf (AbstractHttpConfigurer::disable)
                .sessionManagement (session -> session.sessionCreationPolicy (SessionCreationPolicy.STATELESS))

                .httpBasic (withDefaults ()).headers (header -> header.frameOptions (HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .build ();
    }


}