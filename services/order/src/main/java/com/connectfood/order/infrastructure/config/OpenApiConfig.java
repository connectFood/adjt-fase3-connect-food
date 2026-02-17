package com.connectfood.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI orderOpenApi() {
    final String schemeName = "bearerAuth";

    return new OpenAPI()
        .info(new Info()
            .title("ConnectFood - Order Service")
            .description("Order microservice (create orders, publish order.created)")
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
