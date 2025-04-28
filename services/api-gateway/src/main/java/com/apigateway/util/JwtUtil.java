package com.apigateway.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    public void validateToken(final String token) {
       // Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }


}