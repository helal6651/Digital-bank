package com.notification.utils;

public class ApplicationConstants {
    public static final String EMAIL_URL_TOKEN_PARAMETER = "?token=";
    public static final String EMAIL_URL_EMAIL_PARAMETER = "&email=";
    public static final int MAX_ATTEMPT = 2;

    public static final class MailType {
        public static final int CMS_RESET_PASSWORD = 1;
        public static final int DIGITAL_BANKING_ACTIVATE_USER = 2;
        public static final int WMC_ACTIVE_USER = 3;


    }

    public static final class MailTemplateFields {
        public static final String LINK = "link";
        public static final String SUBJECT = "subject";
        public static final String CODE = "code";
        public static final String USER_NAME = "userName";
        public static final String EMAIL_DOMAIN = "emailDomain";
        public static final String SERVICE_NAME = "serviceName";
        public static final String ROOT_URL = "rootUrl";
        public static final String CONTACT_URL = "contactUrl";
    }
}
