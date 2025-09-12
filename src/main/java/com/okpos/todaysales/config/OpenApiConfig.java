package com.okpos.todaysales.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI miniTodaySalesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini Today Sales API")
                        .description("소상공인을 위한 간단한 매출 관리 시스템 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mini Today Sales Team")
                                .email("support@okpos.com")
                                .url("https://github.com/planetdoy/mini-today-sales"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.okpos.com")
                                .description("프로덕션 서버")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 이용한 인증")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}