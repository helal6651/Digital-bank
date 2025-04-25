package com.user_service.service.kafka;

import lombok.*;


@Getter
@Setter
@Data
public class NotificationMessage {
    private Long userId;
    private String recipientEmail;
    private String userName;
    private String templateName;

    public NotificationMessage() {
    }

    public NotificationMessage(Long userId, String recipientEmail, String userName, String templateName) {
        this.userId = userId;
        this.recipientEmail = recipientEmail;
        this.userName = userName;
        this.templateName = templateName;
    }
}
