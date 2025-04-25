package com.notification.service;

import com.notification.model.entity.VerificationToken;
import org.springframework.scheduling.annotation.Async;

public interface DigitalBankEmailService {
    @Async("taskExecutor")
    void sendAccountActivationMail(Long userId, String recipientEmail, VerificationToken token, String userName, String templateName, int i);
}
