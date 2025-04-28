package com.user_service.config;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.user_service.model.dto.SecretDto;
import com.user_service.utils.ApplicationConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final String[] AUTH_WHITELIST = {"/v1/api/user/register", "/v1/api/authenticate", "/actuator/health"};
    private final Map<String, JwtEncoder> jwtEncoderCache = new ConcurrentHashMap<>();
    private final Map<String, JwtDecoder> jwtDecoderCache = new ConcurrentHashMap<>();
    private final UserSecretsManager userSecretsManager;

    public SecurityConfig(UserSecretsManager userSecretsManager) {
        this.userSecretsManager = userSecretsManager;
    }


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
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Configuring security filter chain");
        return httpSecurity
                .cors(withDefaults()) // Enable CORS with default configuration (uses CorsConfigurationSource bean)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers("v1/api/**").authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(entryPoint()))
                .httpBasic(withDefaults()).headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(customJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Add this converter to properly map authorities
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new CustomJwtGrantedAuthoritiesConverter());
        return jwtConverter;
    }

    @Bean
    public CustomJwtAuthenticationFilter customJwtAuthenticationFilter() {
        log.info("Creating CustomJwtAuthenticationFilter bean");
        return new CustomJwtAuthenticationFilter();
    }

    @Bean
    public CustomAuthenticationEntryPoint entryPoint() {
        log.info("Creating CustomAuthenticationEntryPoint bean");
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JwtEncoder jwtEncoder() throws Exception {
        log.info("Inside jwtEncoder in Security Config");
        SecretDto secretDto = userSecretsManager.getSecretDto();
        String privateKeyEncoded = Base64.getEncoder().encodeToString(readPrivateKey(secretDto).getEncoded());

        return jwtEncoderCache.computeIfAbsent(privateKeyEncoded, key -> {
            try {
                log.info("Inside jwtEncoder value put to map in Security Config");
                RSAKey rsaKey = new RSAKey.Builder(readPublicKey(secretDto))
                        .privateKey(readPrivateKey(secretDto))
                        .keyID(UUID.randomUUID().toString())
                        .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                JWKSource<SecurityContext> jwkSource = (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
                return new NimbusJwtEncoder(jwkSource);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Reads the RSA private key.
     *
     * @param secretDto the {@link SecretDto} containing the private key.
     * @return the {@link PrivateKey}.
     * @throws Exception if the private key cannot be read.
     */
    private PrivateKey readPrivateKey(SecretDto secretDto) throws Exception {
        log.info("Reading private key from SecretDto");
        String key = secretDto.getData().get(ApplicationConstants.PRIVATE_KEY);
        if (key == null) {
            log.error("Private key not found in vault");
            throw new Exception("Private key not found in vault");
        }
        byte[] keyBytes = java.util.Base64.getDecoder().decode(key.replaceAll("\\s", ""));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Reads the RSA public key.
     *
     * @param secretDto the {@link SecretDto} containing the public key.
     * @return the {@link RSAPublicKey}.
     * @throws Exception if the public key cannot be read.
     */
    private RSAPublicKey readPublicKey(SecretDto secretDto) throws Exception {
        log.info("Reading public key from SecretDto");
        String key = secretDto.getData().get(ApplicationConstants.PUBLIC_KEY);
        if (key == null) {
            log.error("Public key not found in vault");
            throw new Exception("Public key not found in vault");
        }
        return userSecretsManager.readPublicKey(key);
    }


    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JwtDecoder jwtDecoder() throws Exception {
        log.info("Creating JwtDecoder bean");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.info("No token found in request");
            return NimbusJwtDecoder.withPublicKey(readPublicKey(userSecretsManager.getSecretDto())).build();
        }

        HttpServletRequest request = attributes.getRequest();
        String requestId = (String) request.getAttribute(ApplicationConstants.REQUEST_ID);
        log.info("Request ID in Security Config: {}", requestId);
        Integer intRsaKeyVersion = CustomJwtAuthenticationFilter.getMapVersion().get(requestId);
        log.info("RSA key version in Security Config: {}", intRsaKeyVersion);
        if (intRsaKeyVersion != null) {
            RSAPublicKey rsaPublicKey = CustomJwtAuthenticationFilter.getMapRsaPublicKey().get(intRsaKeyVersion);
            if (rsaPublicKey != null) {
                String publicKeyEncoded = Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());
                return jwtDecoderCache.computeIfAbsent(publicKeyEncoded, key -> {
                    try {
                        log.info("Creating new JwtDecoder for public key");
                        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
                    } catch (Exception e) {
                        log.error("Error creating JwtDecoder", e);
                        throw new IllegalStateException(e);
                    }
                });
            }
        }
        return NimbusJwtDecoder.withPublicKey(readPublicKey(userSecretsManager.getSecretDto())).build();
    }
}