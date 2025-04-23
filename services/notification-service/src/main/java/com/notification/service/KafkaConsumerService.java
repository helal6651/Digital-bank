package com.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerService {

    @KafkaListener(topics = "user-created", groupId = "notification-group")
    public void listen(String message) {
        System.out.println("Received Message in group 'notification-group': " + message);
    }
    @KafkaListener(topics = "account-created", groupId = "notification-group")
    public void accountNotification(String message) {
        System.out.println("Received Message for the topic 'account-created': " + message);
    }
}