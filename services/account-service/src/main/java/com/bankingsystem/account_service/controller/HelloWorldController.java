package com.bankingsystem.account_service.controller;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HelloWorldController {

    @QueryMapping
    public String hello() {
        return "Hello, World!";
    }
}