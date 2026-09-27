package net.pamytno.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Общее описание REST API для Swagger UI ({@code /swagger-ui.html}) с авторизацией по Bearer JWT.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    /**
     * Метаданные OpenAPI-документа.
     *
     * @return описание API
     */
    @Bean
    public OpenAPI pamytnoOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Памятно API").version("v1")
                        .description("Темы, материалы, вопросы, карточки и интервальное повторение"))
                .components(new Components().addSecuritySchemes(BEARER, bearerScheme()))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    /**
     * Схема авторизации Bearer JWT.
     *
     * @return схема безопасности
     */
    private static SecurityScheme bearerScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
    }
}
