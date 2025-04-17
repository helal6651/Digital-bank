package com.user_service.advice;


import com.user_service.constants.ErrorCode;
import com.user_service.constants.Messages;
import com.user_service.exceptions.BankingApplicationException;
import com.user_service.logging.ErrorMessageInfo;
import com.user_service.logging.LogMessageConfig;
import com.user_service.response.BaseResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.naming.AuthenticationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.user_service.enums.ResponseType.ERROR;


@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestExceptionHandler {
    private final LogMessageConfig logMessageConfig;

    /**
     * Constructor to inject dependencies.
     *
     * @param logMessageConfig configuration for logging error messages.
     */
    @Autowired
    public RestExceptionHandler(LogMessageConfig logMessageConfig) {
        this.logMessageConfig = logMessageConfig;
    }

    @ExceptionHandler({ValidationException.class, RuntimeException.class, Exception.class})
    public ResponseEntity<?> handleCommonExceptions(Exception ex) {
        HttpStatus status = ex instanceof ValidationException ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex instanceof ValidationException ? ex.getMessage() : Messages.SERVER_ERROR;
        return buildResponseEntity(message, null, status, ex);
    }

    /**
     * Builds a structured error response with the given details.
     *
     * @param message   the error message.
     * @param errorCode the error code.
     * @param status    the HTTP status.
     * @param ex        the exception.
     * @return a {@link ResponseEntity} with the error details.
     */
    private ResponseEntity<Object> buildResponseEntity(String message, String errorCode, HttpStatus status,
                                                       Exception ex) {
        logger(message + " " + ex.getLocalizedMessage(), ex);
        return new ResponseEntity<>(BaseResponse.builder().responseType(ERROR).message(Collections.singleton(message))
                .code(errorCode).build(), status);

    }

    /**
     * Logs detailed information about the exception, including stack trace.
     *
     * @param message the error message.
     * @param ex      the exception.
     */
    private void logger(String message, Exception ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder str = new StringBuilder();
        int i = 0;
        for (StackTraceElement stackTraceElement : stackTrace) {
            str.append(stackTraceElement.getFileName()).append(", ").append(stackTraceElement.getLineNumber())
                    .append(", ").append(stackTraceElement.getMethodName()).append("\n");
            if (++i == 5)
                break;
        }
        log.error("{} {}", message, "This exception may occurred on \n" + str);
    }

    /**
     * Handles authentication-related exceptions.
     *
     * @param e the exception to handle.
     * @return a {@link BaseResponse} with the error details.
     */
    @ExceptionHandler({AuthenticationException.class})
    public BaseResponse handle(Exception e) {
        logger(e.getLocalizedMessage(), e);
        return BaseResponse.builder().responseType(ERROR).message(Collections.singleton(e.getMessage()))
                .code(ErrorCode.ALREADY_EXIST).build();
    }


    /**
     * Handles {@link BankingApplicationException} and builds a detailed error
     * response.
     *
     * @param ex the exception to handle.
     * @return a {@link ResponseEntity} with the error details.
     */
    @ExceptionHandler({BankingApplicationException.class})
    public ResponseEntity<Object> handleAppError(BankingApplicationException ex) {
        ErrorMessageInfo errorMessageInfo = logMessageConfig.getErrorMessageInfo(ex.getErrorCode());
        String message = errorMessageInfo.getMessageTemplate();
        return buildResponseEntity(message, ex.getErrorCode(), ex.getStatus(), ex);
    }

    /**
     * Handles validation exceptions for method arguments.
     * <p>
     * This method captures {@link MethodArgumentNotValidException} thrown when a method argument
     * annotated with {@code @Valid} fails validation. It extracts the validation errors and returns
     * them in a structured format.
     * </p>
     *
     * @param ex the {@link MethodArgumentNotValidException} containing validation errors
     * @return a {@link ResponseEntity} containing a map of field names and their corresponding error messages,
     * with a {@link HttpStatus#BAD_REQUEST} status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions when a required request part is missing.
     * <p>
     * This method captures {@link MissingServletRequestPartException} thrown when a required part of a multipart request is missing.
     * It logs the missing part name and returns a {@link ResponseEntity} with a {@link HttpStatus#BAD_REQUEST} status and an error message.
     * </p>
     *
     * @param ex the {@link MissingServletRequestPartException} containing details about the missing request part
     * @return a {@link ResponseEntity} with a {@link HttpStatus#BAD_REQUEST} status and an error message indicating the missing part
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestPartException ex) {
        String name = ex.getRequestPartName();
        log.error("{} part is missing", name);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request params '" + name + "' is missing");
    }
}
