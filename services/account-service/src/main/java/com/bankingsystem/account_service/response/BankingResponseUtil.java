package com.bankingsystem.account_service.response;

import com.bankingsystem.account_service.constants.ErrorCode;
import com.bankingsystem.account_service.enums.ResultCodeConstants;
import com.bankingsystem.account_service.exceptions.BankingApplicationException;
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
        System.out.println("Result Code: " + authResultCode.name ()); // Debugging line to check the result code
        switch (authResultCode) {
            case ALREADY_EXIST:
                System.out.println("result one");
                throw new BankingApplicationException (
                        authResultCode,
                        ErrorCode.ALREADY_EXIST,
                        HttpStatus.BAD_REQUEST
                );
            case TOKEN_EXPIRED:
                System.out.println("result 2");
                throw new BankingApplicationException (
                        authResultCode,
                        ErrorCode.JWT_TOKEN_EXPIRED,
                        HttpStatus.BAD_REQUEST
                );


            default:
                System.out.println("result 3");
                // Default case for unhandled result codes
                throw BankingApplicationException.builder ().resultCode (ResultCodeConstants.INTERNAL_SERVER_ERROR)
                        .errorCode (ErrorCode.INTERNAL_SERVER_ERROR).build ();
        }
    }

}
