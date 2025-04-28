package com.user_service.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private static final String NOTIFICATIONS_TOPIC = "user-created";

    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, NotificationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

  /*  public void sendMessage(String message) {
        try {
            kafkaTemplate.send("user-created", message);
            System.out.println("Message sent: " + message);
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }*/

    public void sendNotification(NotificationMessage notification) {
        kafkaTemplate.send(NOTIFICATIONS_TOPIC, notification);
    }

    public void sendAccountActivationNotification(Long userId, String email, String userName, String templateName) {
        NotificationMessage notification = new NotificationMessage(
                userId,
                email,
                userName,
                templateName
        );
        sendNotification(notification);
    }
}