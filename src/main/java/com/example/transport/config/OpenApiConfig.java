package com.example.transport.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transportOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Transport Manage API")
                .description("Truck GPS tracking, transport orders, and trip dispatch/arrival notifications.")
                .version("v1"));
    }
}