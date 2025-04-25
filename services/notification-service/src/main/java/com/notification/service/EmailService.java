package com.notification.service;

import jakarta.mail.MessagingException;

import java.io.File;
import java.io.UnsupportedEncodingException;

public interface EmailService {
    void sendMail(String to, String from, String fromName, String subject, String body, String attachmentFilename,
                  File file) throws MessagingException, UnsupportedEncodingException;
    void sendMail(String to, String from, String fromName, String subject, String body) throws MessagingException, UnsupportedEncodingException;
}
