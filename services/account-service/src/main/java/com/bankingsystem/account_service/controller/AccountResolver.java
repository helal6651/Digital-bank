package com.bankingsystem.account_service.controller;

import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.entity.Book;
import com.bankingsystem.account_service.enums.ResponseType;
import com.bankingsystem.account_service.response.BaseResponse;
import com.bankingsystem.account_service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Controller
public class AccountResolver {

    @Autowired
    private AccountService accountService;

    @QueryMapping
    public BaseResponse getAccountsByUserId(@Argument Long id) {
        try {
            Book book1 = new Book(1L, "To Kill a Mockingbird", "Harper Lee");
            System.out.println("Fetching book with IDDDD: " + id + " from AccountResolver...");
            List<AccountResponseDTO> accounts = accountService.getAccountsByUserId(id);
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
