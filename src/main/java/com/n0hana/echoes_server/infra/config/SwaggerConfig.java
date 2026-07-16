package com.n0hana.echoes_server.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openApi() {
        OpenAPI openAPI = new OpenAPI();
        
        openAPI.info(
            new Info()
                .title("Echoes-API")
                .description("API REST para controle e autenticação de usuários no sistema Echoes")
                .version("1.0.0")
        );

        return openAPI;
    }
}