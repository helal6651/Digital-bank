package com.user_service.response;

import com.user_service.constants.ErrorCode;
import com.user_service.enums.ResultCodeConstants;
import com.user_service.exceptions.BankingApplicationException;
import org.springframework.http.HttpStatus;

/**
 * WatermarkResponseUtil class for handling application-specific exceptions and
 * responses.
 * <p>
 * This class provides methods to throw exceptions based on specific result
 * codes and error scenarios encountered in the application. It is designed to
 * standardize error handling and response generation.
 *
 * @author BJIT
 * @version 1.0
 */
public class BankingResponseUtil {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private BankingResponseUtil() {
    }


    public static BankingApplicationException throwApplicationException (ResultCodeConstants authResultCode) throws BankingApplicationException {
        switch (authResultCode) {
            case ALREADY_EXIST:
                throw new BankingApplicationException (
                        authResultCode,
                        ErrorCode.ALREADY_EXIST,
                        HttpStatus.BAD_REQUEST
                );
            case TOKEN_EXPIRED:
                throw new BankingApplicationException (
                        authResultCode,
                        ErrorCode.JWT_TOKEN_EXPIRED,
                        HttpStatus.BAD_REQUEST
                );


            default:
                // Default case for unhandled result codes
                throw BankingApplicationException.builder ().resultCode (ResultCodeConstants.INTERNAL_SERVER_ERROR)
                        .errorCode (ErrorCode.INTERNAL_SERVER_ERROR).build ();
        }
    }

}
