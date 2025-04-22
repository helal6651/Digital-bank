package com.bankingsystem.account_service.exception;
import com.bankingsystem.account_service.exceptions.BankingApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(BankingApplicationException.class)
    public ResponseEntity<Object> handleAccountExistException(BankingApplicationException ex, WebRequest request) {
        // Build the custom error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", ex.getStatus().value()); // HTTP status code
        errorResponse.put("error", ex.getStatus().getReasonPhrase()); // HTTP reason phrase
        errorResponse.put("resultCode", ex.getResultCode()); // Custom result code
        errorResponse.put("errorCode", ex.getErrorCode()); // Custom error code
        errorResponse.put("message", ex.getMessage()); // Exception message
        errorResponse.put("path", request.getDescription(false).replace("uri=", "")); // API endpoint path

        // Return the response entity with proper HTTP status
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

}