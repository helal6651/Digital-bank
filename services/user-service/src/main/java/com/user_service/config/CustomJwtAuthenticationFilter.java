package com.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.user_service.enums.TokenType;
import com.user_service.exceptions.InvalidJwtToken;
import com.user_service.service.CustomUserDetailsService;
import com.user_service.utils.ApplicationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger("REQUEST_RESPONSE_LOGGER");
    @Getter
    private static Map<Integer, RSAPublicKey> mapRsaPublicKey = new ConcurrentHashMap<>();
    @Getter
    private static Map<String, Integer> mapVersion = new ConcurrentHashMap<>();

    /**
     * The {@link JwtDecoder} used for decoding and validating JWT tokens.
     */

    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserSecretsManager userSecretsManager;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Processes each incoming request, validating the JWT token if present.
     * <p>
     * This method extracts the token from the `Authorization` header, validates it
     * using the {@link JwtDecoder}, and checks the token type to ensure it is an
     * ACCESS token. If validation fails, an appropriate error response is returned.
     * If the token is valid, the request is passed down the filter chain.
     * </p>
     *
     * @param request     the {@link HttpServletRequest} object
     * @param response    the {@link HttpServletResponse} object
     * @param filterChain the {@link FilterChain} to pass the request and response
     * @throws ServletException if an error occurs during request processing
     * @throws IOException      if an I/O error occurs during response writing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Log before the request is processed
        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        logger.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        long startTime = System.currentTimeMillis();
        // Extract and validate the JWT token
        String token = userSecretsManager.extractTokenFromRequest(request);
        if (token != null && !token.isEmpty()) {
            try {
                String requestId = UUID.randomUUID().toString();
                request.setAttribute(ApplicationConstants.REQUEST_ID, requestId);
                log.info("Request ID: {} -", requestId);
                validateToken(token, requestId, request);
            } catch (Exception e) {
                handleException(response, e);
                return;
            }
        }
        // Pass the request down the filter chain
        filterChain.doFilter(request, response);

        // Log outgoing response
        log.info("Outgoing response: {} {} - {} (took {} ms)", response.getStatus(), request.getMethod(), request.getRequestURI(), System.currentTimeMillis() - startTime);
        logger.info("Outgoing response: {} {} - {} (took {} ms)", response.getStatus(), request.getMethod(), request.getRequestURI(), System.currentTimeMillis() - startTime);
    }

    /**
     * Validates the JWT token.
     * <p>
     * This method fetches the RSA public key version from the token, retrieves the
     * corresponding public key, and decodes the token using the {@link JwtDecoder}.
     * It also checks if the token type is ACCESS.
     * </p>
     *
     * @param token     the JWT token to validate
     * @param requestId the unique request ID for logging purposes
     * @throws Exception if the token is invalid or missing
     */
    private void validateToken(String token, String requestId, HttpServletRequest request) throws Exception {
        Integer intVersion = userSecretsManager.getVaultKeyVersionFromToken(token);
        log.info("RSA key version in Custom JWT Authentication Filter: {}", intVersion);
        if (intVersion != null) {
            RSAPublicKey publicKey = userSecretsManager.fetchPublicKey(intVersion);
            if (publicKey != null) {
                mapRsaPublicKey.put(intVersion, publicKey);
                mapVersion.put(requestId, intVersion);
            }
        }

        Jwt decodedJwt = jwtDecoder.decode(token);
        log.info("Decoded JWT: {}", decodedJwt);

        if (!TokenType.ACCESS.name().equals(decodedJwt.getClaim(ApplicationConstants.TOKEN_TYPE))) {
            log.error("Invalid or missing token type.");
            throw new InvalidJwtToken("Invalid or missing token.");
        }
       String userName =  decodedJwt.getSubject();
        log.info("User name from token: {}", userName);
        /*if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("Setting authentication for user: {}", userName);
            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(userName);
            System.out.println("Authentication details: " + userDetails.getAuthorities());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }*/
    }

    /**
     * Handles exceptions during token validation.
     * <p>
     * This method logs the error, clears the security context, sets the response
     * status to 401 (Unauthorized), and writes an error message to the response.
     * </p>
     *
     * @param response the {@link HttpServletResponse} object
     * @param e        the exception that occurred
     * @throws IOException if an I/O error occurs during response writing
     */
    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        log.error("Error in Custom JWT Authentication filter: {}", e.getMessage());
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String errorMessage = e.getMessage().contains("Jwt expired") ? "Token has expired" : "Invalid or missing token.";
        response.getWriter().write("{\"error\": \"" + errorMessage + "\", \"code\":\"500\"}");
    }
}