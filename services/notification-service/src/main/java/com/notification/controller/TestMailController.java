package com.notification.controller;

import com.notification.model.NotificationMessage;
import com.notification.service.KafkaConsumerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/mail")
public class TestMailController {
    private final KafkaConsumerService kafkaConsumerService;

    public TestMailController(KafkaConsumerService kafkaConsumerService) {
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @PostMapping
    public void sendMail() {
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setUserId(1L);
        notificationMessage.setRecipientEmail("mdrafiq10015@gmail.com");
        notificationMessage.setUserName("Rafiq");
        notificationMessage.setTemplateName("digital-bank-active-user");
        kafkaConsumerService.userService(notificationMessage);
    }
}
