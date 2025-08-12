package com.apigateway.config;

import com.apigateway.filter.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GatewayConfig {
    private final JwtAuthenticationFilter filter;

    public GatewayConfig(JwtAuthenticationFilter filter) {
        this.filter = filter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        log.info("Configuring routes for the API Gateway");
        return builder.routes()
                // User Service routes
                .route("user-service", r -> r.path("/v1/api/user/**")
                        .filters(f -> f.filter(filter))
                        .uri("http://user-service:9491"))
                .build();
    }
}