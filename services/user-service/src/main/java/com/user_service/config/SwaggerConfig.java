package com.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger configuration class.
 * <p>
 * Configures OpenAPI settings, including application title, version, and
 * security schemes.
 * </p>
 *
 * @author BJIT
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI () {
        // Define the security scheme
        SecurityScheme securityScheme = new SecurityScheme ().type (SecurityScheme.Type.HTTP).scheme ("bearer")
                .bearerFormat ("JWT").description ("JWT Bearer token authentication");

        // Add the security scheme to components
        Components components = new Components ().addSecuritySchemes ("Bearer Authentication", securityScheme);

        // Add global security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement ().addList ("Bearer Authentication");

        // Return the OpenAPI definition
        return new OpenAPI ().components (components)
                // .addSecurityItem(securityRequirement) // Apply globally
                .info (new Info ().title ("User service API Documentation in " + "dev").version ("1.0")
                        .description ("API documentation with custom security requirements"));
    }
}
