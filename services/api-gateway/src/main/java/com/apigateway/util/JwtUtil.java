package com.apigateway.util;


import com.apigateway.config.UserSecretsManager;
import com.apigateway.enums.TokenType;
import com.apigateway.exceptions.InvalidJwtToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class JwtUtil {
    @Getter
    private static Map<Integer, RSAPublicKey> mapRsaPublicKey = new ConcurrentHashMap<>();
    @Getter
    private static Map<String, Integer> mapVersion = new ConcurrentHashMap<>();
    private final UserSecretsManager userSecretsManager;
    private final Map<Integer, JwtDecoder> jwtDecoderCache = new ConcurrentHashMap<>();

    public JwtUtil(UserSecretsManager userSecretsManager) {
        this.userSecretsManager = userSecretsManager;
    }

    public boolean validateTokenReactive(String token, String requestId) throws Exception {
        log.info("Validating token reactively: {} requestId:{}", token, requestId);
        Integer intVersion = userSecretsManager.getVaultKeyVersionFromToken(token);
        log.info("RSA key version in JWT Util: {}", intVersion);

        if (intVersion == null) {
            throw new InvalidJwtToken("Invalid token: No key version found");
        }

        RSAPublicKey publicKey = userSecretsManager.fetchPublicKey(intVersion);
        if (publicKey != null) {
            mapRsaPublicKey.put(intVersion, publicKey);
            mapVersion.put(requestId, intVersion);
        } else {
            throw new InvalidJwtToken("Could not retrieve public key");
        }

        // Create decoder on demand using the public key
        JwtDecoder decoder = jwtDecoderCache.computeIfAbsent(intVersion, k ->
                NimbusJwtDecoder.withPublicKey(publicKey).build());

        Jwt decodedJwt = decoder.decode(token);
        log.info("Decoded JWT: {}", decodedJwt);

        if (!TokenType.ACCESS.name().equals(decodedJwt.getClaim(ApplicationConstants.TOKEN_TYPE))) {
            log.error("Invalid or missing token type.");
            throw new InvalidJwtToken("Invalid token type.");
        }

        String userName = decodedJwt.getSubject();
        log.info("User name from token in API Gateway: {}", userName);
        return true;
    }
}