package br.com.fiap.sout.vendas.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Sales Service API")
                        .version("0.0.1-SNAPSHOT")
                        .description("API para gerenciamento de réplicas de itens do catálogo, reservas de veículos, " +
                                "e webhook para processamento de pagamentos das vendas."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT M2M fornecido para comunicação interna entre o Catálogo e Vendas."))
                        .addSecuritySchemes("hmacAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-Signature")
                                        .description("Assinatura HMAC-SHA256 gerada a partir do corpo da requisição bruta.")));
    }
}
