package net.pamytno.common.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link ApplicationExceptionHandler}: каждая категория исключения даёт свой HTTP-статус.
 */
class ApplicationExceptionHandlerTest {

    private final ApplicationExceptionHandler handler = new ApplicationExceptionHandler(
            new ErrorResponseFactory(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)));

    @Test
    @DisplayName("NotFoundException превращается в 404 с кодом исключения")
    void handleNotFound_returns404() {
        var response = handler.handleNotFound(new NotFoundException("TOPIC_NOT_FOUND", "Тема не найдена"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("TOPIC_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Тема не найдена");
    }

    @Test
    @DisplayName("ConflictException превращается в 409")
    void handleConflict_returns409() {
        var response = handler.handleConflict(new ConflictException("EMAIL_TAKEN", "Занято"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_TAKEN");
    }

    @Test
    @DisplayName("BadRequestException превращается в 400")
    void handleBadRequest_returns400() {
        var response = handler.handleBadRequest(new BadRequestException("INVALID_URL", "Неверная ссылка"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_URL");
    }

    @Test
    @DisplayName("UnauthorizedException превращается в 401")
    void handleUnauthorized_returns401() {
        var response = handler.handleUnauthorized(new UnauthorizedException("INVALID_CREDENTIALS", "Неверно"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("ForbiddenException превращается в 403")
    void handleForbidden_returns403() {
        var response = handler.handleForbidden(new ForbiddenException("REGISTRATION_DISABLED", "Запрещено"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("REGISTRATION_DISABLED");
    }
}
