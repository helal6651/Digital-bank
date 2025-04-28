package com.notification.service.impl;

import com.notification.service.EmailService;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void sendMail(String to, String from, String fromName, String subject, String body,
                         String attachmentFilename, File file) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        mimeMessage.setFrom(new InternetAddress(from, fromName));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        helper.setSubject(subject);
        helper.setText(body, true);

        if (StringUtils.isNotBlank(attachmentFilename) && file != null) {
            helper.addAttachment(attachmentFilename, file);
        }

        emailSender.send(mimeMessage);
    }

    @Override
    public void sendMail(String to, String from, String fromName, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        sendMail(to, from, fromName, subject, body, null, null);

    }
}
