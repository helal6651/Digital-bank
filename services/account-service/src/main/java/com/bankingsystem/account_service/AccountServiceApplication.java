package com.bankingsystem.account_service;

import com.bankingsystem.account_service.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bankingsystem.account_service")
public class AccountServiceApplication {

	public static void main(String[] args) {
		User user = new User();
		user.setUsername("John Doe");
		System.out.println("User NameeeeeeeeeeeeTT: " + user.getUsername());
		SpringApplication.run(AccountServiceApplication.class, args);
	}

}
