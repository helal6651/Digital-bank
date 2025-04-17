package com.user_service.constants;

/**
 * A utility class that defines error codes used throughout the application.
 * <p>
 * This class provides a set of static constants representing specific error
 * codes for various error scenarios. These error codes are used to standardize
 * error reporting and facilitate easier debugging and client-side error
 * handling.
 * </p>
 *
 * @author BJIT
 * @version 1.0
 */
public final class ErrorCode {
    public static final String ALREADY_EXIST = "40101";
    public static final String JWT_TOKEN_EXPIRED = "40103";

    public static final String INTERNAL_SERVER_ERROR = "5000";
    public static final int MAINTENANCE_MODE = 9999;

    private ErrorCode () {
    }

}
