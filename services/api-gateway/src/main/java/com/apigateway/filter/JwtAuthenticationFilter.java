package com.apigateway.filter;

import com.apigateway.util.ApplicationConstants;
import com.apigateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {
    @Value("${gateway.secret.key}")
    private String GATEWAY_SECRET;

    final List<String> apiEndpoints = List.of("/v1/api/user/register", "/v1/api/authenticate", "/v1/api/renewToken", "/actuator/health", "/eureka");
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("Request path: {}", request.getURI().getPath());
        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));
        String requestId = UUID.randomUUID().toString();

        if (isApiSecured.test(request)) {
            log.info("Secured API endpoint accessed: {}", request.getURI().getPath());

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization format");
            }
            String token = authHeader.substring(7);
            try {
                boolean isValid = jwtUtil.validateTokenReactive(token, requestId);
                if (!isValid) {
                    return onError(exchange, "Invalid token");
                }
                // return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                return onError(exchange);
            }
        } else {
            log.info("Non-secured API endpoint accessed: {}", request.getURI().getPath());
        }
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(ApplicationConstants.REQUEST_ID, requestId)
                .header(ApplicationConstants.GATEWAY_SIGNATURE_HEADER, generateGatewaySignature())
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.error("Unauthorized request");
        return response.setComplete();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.error("Unauthorized request: {}", message);
        return response.setComplete();
    }

    private String generateGatewaySignature() {
        // Create a signature using a secret key
        String timestamp = String.valueOf(System.currentTimeMillis());
        return Base64.getEncoder().encodeToString((timestamp + ":" + GATEWAY_SECRET).getBytes());
    }

}