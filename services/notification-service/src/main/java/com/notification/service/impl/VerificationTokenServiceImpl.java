package com.notification.service.impl;

import com.notification.config.email.MailConfiguration;
import com.notification.model.entity.VerificationToken;
import com.notification.repository.VerificationTokenRepository;
import com.notification.service.VerificationTokenService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final MailConfiguration mailConfiguration;
    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenServiceImpl(MailConfiguration mailConfiguration, VerificationTokenRepository verificationTokenRepository) {
        this.mailConfiguration = mailConfiguration;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public Optional<Integer> verifyToken(String token) {
        return Optional.empty();
    }

    @Override
    public void deleteToken(String token) {

    }

    @Override
    public VerificationToken generateInvitationToken(Long userId, String email, String serviceName, String templateName, Integer tokenType) {
        LocalDateTime expirationTime = LocalDateTime.now(ZoneOffset.UTC)
                .plusHours(mailConfiguration.getDefaultMailExpiration());
        return getVerificationToken(userId, email, serviceName, tokenType, expirationTime);
    }

    private VerificationToken getVerificationToken(Long userId, String email, String serviceName, Integer tokenType, LocalDateTime expirationTime) {
        long expirationTimeInMillis = expirationTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        byte[] randomBytes = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        System.out.println("Generated token: " + token);
        VerificationToken verificationToken = buildToken(userId, serviceName, tokenType, expirationTimeInMillis, token);
        verificationTokenRepository.save(verificationToken);
        System.out.println("Saved token: " + verificationToken.getServiceName());
        return verificationToken;
    }

    private VerificationToken buildToken(Long userId, String serviceName, Integer tokenType,
                                         Long expirationTime, String token) {
        return VerificationToken.builder()
                .userId(userId)
                .token(token)
                .serviceName(serviceName)
                .createdOn(ZonedDateTime.now(ZoneOffset.UTC))
                .expirationTime(expirationTime)
                .tokenType(tokenType)
                .build();
    }
}
