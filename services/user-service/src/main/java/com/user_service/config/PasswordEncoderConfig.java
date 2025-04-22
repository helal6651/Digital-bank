package com.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public Argon2PasswordEncoder passwordEncoder() {
        int saltLength = 16;       // bytes
        int hashLength = 32;       // bytes
        int parallelism = 1;       // currently only 1 supported by Java
        int memoryInKb = 65536;    // 64MB
        int iterations = 4;        // higher = slower = safer

        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memoryInKb, iterations);
    }
}
