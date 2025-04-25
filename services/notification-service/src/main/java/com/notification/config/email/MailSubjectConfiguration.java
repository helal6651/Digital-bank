package com.notification.config.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(value = "classpath:mail-subject.properties", encoding = "UTF-8")

public class MailSubjectConfiguration {
    private final Environment environment;

    @Autowired
    public MailSubjectConfiguration(Environment environment) {
        this.environment = environment;
    }

    public String getRegistrationEmailEnglishSubject() {
        return environment.getProperty("registration.email.subject.en");
    }

    public String getFinishRegistrationEmailEnglishSubject() {
        return environment.getProperty("finish.registration.email.subject.en");
    }

    public String getForgotPasswordEmailEnglishSubject() {
        return environment.getProperty("forgot.password.email.subject.en");
    }


}
