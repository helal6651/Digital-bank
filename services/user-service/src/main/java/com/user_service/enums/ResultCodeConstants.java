package com.user_service.enums;

/**
 * Enum representing various result codes used throughout the application.
 * <p>
 * This enum defines a set of constant values representing different types of
 * errors or result codes that may occur during the execution of the
 * application. These codes can be used in response objects, logging, and
 * exception handling to provide consistent and meaningful error reporting.
 * </p>
 *
 * @author BJIT
 * @version 1.0
 */
public enum ResultCodeConstants {
    INTERNAL_SERVER_ERROR,
    INVALID_DATA,
    UNAUTHORIZED_OPERATION,
    ROLE_NOT_FOUND,
    ALREADY_EXIST,
    TOKEN_EXPIRED,
    AUTH_FAILURE,
    INVALID_PASSWORD_PATTERN,
    USER_NOT_FOUND,
    WRONG_CREDENTIALS,
    ResultCodeConstants() {
    }
}
