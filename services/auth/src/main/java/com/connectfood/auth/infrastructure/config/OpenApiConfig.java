package com.connectfood.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI authOpenApi() {
    final String schemeName = "bearerAuth";

    return new OpenAPI()
        .info(new Info()
            .title("ConnectFood - Auth Service")
            .description("Auth microservice (users, roles, JWT access/refresh)")
            .version("v1"))
        .addSecurityItem(new SecurityRequirement().addList(schemeName))
        .components(new io.swagger.v3.oas.models.Components()
            .addSecuritySchemes(schemeName,
                new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ));
  }
}
