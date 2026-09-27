package net.pamytno.common.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link ErrorResponseFactory}.
 */
class ErrorResponseFactoryTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    private final ErrorResponseFactory factory = new ErrorResponseFactory(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("Тело ошибки содержит код, сообщение и текущее время")
    void create_fillsCodeMessageAndTimestamp() {
        // when
        var error = factory.create("TOPIC_NOT_FOUND", "Тема не найдена");

        // then
        assertThat(error).isEqualTo(new ErrorResponse("TOPIC_NOT_FOUND", "Тема не найдена", NOW));
    }

    @Test
    @DisplayName("HTTP-ответ несёт переданный статус и тело ошибки")
    void response_wrapsErrorWithStatus() {
        // when
        var response = factory.response(HttpStatus.CONFLICT, "CODE", "Сообщение");

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("CODE", "Сообщение", NOW));
    }
}
