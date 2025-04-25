package com.notification.service;

import com.notification.model.NotificationMessage;
import com.notification.model.entity.VerificationToken;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerService {
    private static final String NOTIFICATIONS_TOPIC = "user-created";
    private static final String NOTIFICATION_GROUP = "notification-group";
    private final DigitalBankEmailService digitalBankEmailService;
    private final VerificationTokenService verificationTokenService;

    public KafkaConsumerService(DigitalBankEmailService digitalBankEmailService, VerificationTokenService verificationTokenService) {
        this.digitalBankEmailService = digitalBankEmailService;
        this.verificationTokenService = verificationTokenService;
    }

    @KafkaListener(topics = NOTIFICATIONS_TOPIC, groupId = NOTIFICATION_GROUP)
    public void userService(NotificationMessage notificationMessage) {
        System.out.println("Received Message in group 'notification-group': " + notificationMessage.getRecipientEmail());
        VerificationToken token = verificationTokenService.generateInvitationToken(
                notificationMessage.getUserId(), notificationMessage.getRecipientEmail(),
                notificationMessage.getUserName(), notificationMessage.getTemplateName(), 1);
        digitalBankEmailService.sendAccountActivationMail(notificationMessage.getUserId(), notificationMessage.getRecipientEmail(), token, notificationMessage.getUserName(), notificationMessage.getTemplateName(), 1);
    }

    @KafkaListener(topics = "account-created", groupId = "notification-group")
    public void accountNotification(String message) {
        System.out.println("Received Message for the topic 'account-created': " + message);
    }
}