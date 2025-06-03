package com.bankingsystem.account_service.controller;
import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.enums.ResponseType;
import com.bankingsystem.account_service.response.BaseResponse;
import com.bankingsystem.account_service.service.AccountService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AccountQueryResolver implements GraphQLQueryResolver {

    @Autowired
    private AccountService accountService;

    /**
     * GraphQL Query to fetch accounts by user ID.
     */

    public BaseResponse getAccountsByUserId(String userId) {
        try {
            Long userIdLong = Long.valueOf(userId);
            // Fetch account DTOs from the service layer
            System.out.println("Fetching accounts for user IDDDDD: " + userId + " from AccountQueryResolver...");
            List<AccountResponseDTO> accounts = accountService.getAccountsByUserId(userIdLong);
            System.out.println("Accounts fetched successfullyyyy: " + accounts);

            // Build and return a successful response
            return BaseResponse.builder()
                    .responseType(ResponseType.RESULT)
                    .message(Collections.singletonList("Accounts fetched successfully"))
                    .result(accounts) // List of AccountResponseDTO
                    .error("No error") // No error
                    .code("200")
                    .build();
        } catch (Exception ex) {
            // Log and build an error response
            ex.printStackTrace();
            return BaseResponse.builder()
                    .responseType(ResponseType.ERROR)
                    .message(Collections.singletonList("Failed to fetch accounts"))
                    .result(Collections.emptyList())
                    .error(ex.getMessage() != null ? ex.getMessage() : "Hel36 Unknown error occurred")
                    .code("500")
                    .build();
        }
    }
}