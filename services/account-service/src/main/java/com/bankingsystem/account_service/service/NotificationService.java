package com.bankingsystem.account_service.service;

import com.bankingsystem.account_service.entity.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @KafkaListener(topics = "account_created_messages", groupId = "notification-group")
    public void consumeMessage(String message) {
        System.out.println("Consumed messageeee: " + message);

        // Save consumed message to database
//        Event event = new Event();
//        event.setName("Kafka Consumer");
//        event.setMessage(message);
//        eventRepository.save(event); // Persist message in MySQL
    }
}
