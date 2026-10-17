package com.taskflow.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentation OpenAPI : Swagger UI sur /swagger-ui.html, avec un bouton "Authorize" (jeton Bearer). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI taskFlowOpenApi() {
        return new OpenAPI()
                .info(new Info().title("TaskFlow API").version("1.0.0")
                        .description("API de gestion de tâches sécurisée par Keycloak."))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}