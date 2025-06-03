package com.bankingsystem.account_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import com.common_service.model.entity.User;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Arrays;
@SpringBootApplication(scanBasePackages = {
		"com.bankingsystem.account_service", // Scan account service packages
		"com.common_service"                 // Scan common service packages
})
@EnableJpaRepositories(basePackages = {
		"com.bankingsystem.account_service.repository", // Account repository
		"com.common_service.repository"                // User repository
})
@ComponentScan(basePackages = {
		"com.bankingsystem.account_service",
		"com.common_service"
})
public class AccountServiceApplication {

	public static void main(String[] args) {
		User user = new User();
		user.setUsername("John Doe");
		System.out.println("User NameeeeeeeeeeeeTT: " + user.getUsername());
		ConfigurableApplicationContext context = SpringApplication.run(AccountServiceApplication.class, args);
		String[] beanNames = context.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println("HELS36 bean name==>"+beanName);
		}
	}

}
