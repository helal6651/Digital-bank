package com.notification.service;


import com.notification.model.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenService {
    Optional<Integer> verifyToken(String token);

    void deleteToken(String token);

    VerificationToken generateInvitationToken(Long userId, String email, String serviceName, String templateName, Integer tokenType);


}
