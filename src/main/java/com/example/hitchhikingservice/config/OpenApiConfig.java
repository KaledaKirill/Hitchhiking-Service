package com.example.hitchhikingservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hitchhikingServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hitchhiking Service API")
                        .description("API for searching rides and passengers")
                        .version("1.0")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub repository")
                        .url("https://github.com/KaledaKirill/Hitchhiking-Service"));
    }
}