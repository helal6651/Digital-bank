package com.notification.config.email;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:mail-config-local.properties", encoding = "UTF-8")
@Data
public class MailConfiguration {
    @Value("${digital.bank.root.url}")
    private String digitalBankRootUrl;
    @Value("${mail.from.address}")
    private String mailFromAddress;
    @Value("${mail.from.name}")
    private String mailFromName;
    @Value("${mail.digital.bank.url.user.activation}")
    private String userActivation;
    @Value("${otp.expired.in.hour}")
    private Integer otpExpiration;
    @Value("${forgot.password.mail.expired.in.hour}")
    private Integer resetPasswordMailExpiration;
    @Value("${email.expired.in.hour}")
    private Integer defaultMailExpiration;
    @Value("${email.max.number.attempts}")
    private Integer emailRetryAttemp;
    @Value("${mail.from.service.name}")
    private String mailServiceName;
    @Value("${mail.to.support}")
    private String supportMail;
}
