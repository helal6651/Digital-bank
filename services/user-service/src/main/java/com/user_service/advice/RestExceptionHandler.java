package com.user_service.advice;


import com.user_service.constants.ErrorCode;
import com.user_service.constants.Messages;
import com.user_service.exceptions.BankingApplicationException;
import com.user_service.exceptions.InvalidJwtToken;
import com.user_service.logging.ErrorMessageInfo;
import com.user_service.logging.LogMessageConfig;
import com.user_service.response.BaseResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.naming.AuthenticationException;
import java.util.*;
import java.util.stream.Collectors;

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


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        String errorCode = getErrorCode(fieldErrors);
        String message = getMessage(errorCode);

        return buildResponseEntity(Collections.singleton(message), errorCode, HttpStatus.BAD_REQUEST, ex, fieldErrors);

    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestPartException ex) {
        String name = ex.getRequestPartName();
        log.error("{} part is missing", name);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request params '" + name + "' is missing");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        String message = "Access denied: You don't have permission to perform this operation";
        log.error("Access denied exception: {}", ex.getMessage());
        return buildResponseEntity(message, ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handler(IllegalArgumentException ex) {
        return buildResponseEntity(ex.getMessage(), ErrorCode.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST, ex);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        // Extract the root cause which contains the actual error message
        Throwable rootCause = ex.getMostSpecificCause();
        String errorMessage = rootCause.getMessage();

        log.error("JSON parse error: {}", errorMessage);
        return buildResponseEntity(errorMessage, ErrorCode.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Handles custom {@link InvalidJwtToken} exceptions.
     *
     * @param e the exception to handle.
     * @return a {@link BaseResponse} with the error details.
     */
    @ExceptionHandler ({InvalidJwtToken.class, JwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse handle (InvalidJwtToken e) {
        logger (e.getLocalizedMessage (), e);
        return BaseResponse.builder ().responseType (ERROR).message (Collections.singleton (e.getMessage ()))
                .code (ErrorCode.INVALID_JWT_TOKEN).build ();
    }
    private ResponseEntity<Object> buildResponseEntity(Collection<String> message, String errorCode,
                                                       HttpStatus status, Exception ex, Object error) {
        logger(message + " " + ex.getLocalizedMessage(), ex);
        return new ResponseEntity<>(BaseResponse.builder()
                .responseType(ERROR).message(message).code(errorCode).error(error).build(), status);
    }

    private String getErrorCode(List<FieldError> fieldErrors) {

        String code = ErrorCode.INVALID_ARGUMENT;
        return fieldErrors.stream().findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage).orElse(code);
    }

    private String getMessage(String errorCode) {
        try {
            ErrorMessageInfo errorMessageInfo = logMessageConfig.getErrorMessageInfo(errorCode);
            return errorMessageInfo.getMessageTemplate();
        } catch (NoSuchElementException e) {
            return errorCode;
        }
    }
}
