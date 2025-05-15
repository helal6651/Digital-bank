package com.apigateway.config;


import com.apigateway.util.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final UserSecretsManager userSecretsManager;
    private final Map<String, JwtDecoder> jwtDecoderCache = new ConcurrentHashMap<>();

    public SecurityConfig(UserSecretsManager userSecretsManager) {
        this.userSecretsManager = userSecretsManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable) // Disable default CORS - we'll use our custom filter
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        log.info("Creating default JwtDecoder bean");
        // Create a default decoder for cases when we don't have a specific key version
        return NimbusJwtDecoder.withPublicKey(
                userSecretsManager.readPublicKey(
                        userSecretsManager.getSecretDto().getData().get(ApplicationConstants.PUBLIC_KEY)
                )
        ).build();
    }
}