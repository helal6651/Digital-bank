package com.user_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.user_service.utils.ApplicationContextUtils;
import com.user_service.utils.Messages;

public enum AuthenticationType {
    @JsonProperty("1")
    BANKING(1),
    @JsonProperty("2")
    GOOGLE(2),
    @JsonProperty("3")
    FACEBOOK(3),
    ;

    @JsonCreator
    public static AuthenticationType forValue(Integer value) {
        for (AuthenticationType authenticationType : AuthenticationType.values()) {
            if (authenticationType.type == value) {
                return authenticationType;
            }
        }
        throw new IllegalArgumentException(ApplicationContextUtils.getContext().getBean(Messages.class).getInvalidAuthenticationType());
    }

    private final int type;

    AuthenticationType(int type) {
        this.type = type;
    }

    @JsonValue
    public int getValue() {
        return type;
    }
}
