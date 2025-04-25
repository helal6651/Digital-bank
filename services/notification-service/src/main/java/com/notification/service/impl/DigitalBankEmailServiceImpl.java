package com.notification.service.impl;

import com.notification.config.email.MailConfiguration;
import com.notification.config.email.MailDataProvider;
import com.notification.model.entity.VerificationToken;
import com.notification.service.DigitalBankEmailService;
import com.notification.service.EmailService;
import com.notification.utils.ApplicationConstants;
import com.notification.utils.MailTemplateName;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class DigitalBankEmailServiceImpl implements DigitalBankEmailService {
    private final MailDataProvider mailDataProvider;
    private final MailConfiguration mailConfig;
    private final EmailService emailService;
    public DigitalBankEmailServiceImpl( MailDataProvider mailDataProvider, MailConfiguration mailConfig, EmailService emailService) {
        this.mailDataProvider = mailDataProvider;
        this.mailConfig = mailConfig;
        this.emailService = emailService;
    }

    @Override
    public void sendAccountActivationMail(Long userId, String recipientEmail, VerificationToken verificationToken, String userName, String templateName, int i) {
        String subject = mailDataProvider.getMailSubjectCommon(ApplicationConstants.MailType.DIGITAL_BANKING_ACTIVATE_USER);
        log.info("Sending account activation mail to userId: {}, recipientEmail: {}, subject: {}", userId, recipientEmail, subject);
        String link = mailConfig.getDigitalBankRootUrl()
                + ApplicationConstants.EMAIL_URL_TOKEN_PARAMETER + verificationToken.getToken()
                + ApplicationConstants.EMAIL_URL_EMAIL_PARAMETER + recipientEmail;
        log.info("Generated link: {}", link);
        String templateNamePrefix = MailTemplateName.DIGITAL_BANK_ACTIVE_USER_PREFIX;
        log.info("Using template: {}", templateNamePrefix);
          sendMail(link, subject, templateNamePrefix, recipientEmail);
    }

    private void sendMail(String link, String subject, String templateNamePrefix, String to) {
        Map<String, String> model = new HashMap<>();
        model.put(ApplicationConstants.MailTemplateFields.LINK, link);
        try {
            String body = mailDataProvider.getMailBody(model, templateNamePrefix);
            log.info("Generated mail body: {}", body);
            log.info("Sending mail to: {}, from: {}, subject: {}", to, mailConfig.getMailFromAddress(), subject);
            emailService.sendMail(to, mailConfig.getMailFromAddress(), mailConfig.getMailFromName(),
                    subject, body);
        } catch (IOException | TemplateException | MessagingException e) {
            log.error("Can't send mail.{}", e.getLocalizedMessage(), e);
        }
    }
}
