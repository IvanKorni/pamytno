package net.pamytno.common.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Собирает {@link ErrorResponse} с текущим временем и оборачивает его в HTTP-ответ.
 */
@Component
@RequiredArgsConstructor
public class ErrorResponseFactory {

    private final Clock clock;

    /**
     * Создаёт тело ошибки.
     *
     * @param code    код ошибки
     * @param message сообщение на русском
     * @return тело ошибки с текущим временем
     */
    public ErrorResponse create(String code, String message) {
        return new ErrorResponse(code, message, clock.instant());
    }

    /**
     * Создаёт HTTP-ответ с телом ошибки.
     *
     * @param status  HTTP-статус
     * @param code    код ошибки
     * @param message сообщение на русском
     * @return ответ с телом {@link ErrorResponse}
     */
    public ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(create(code, message));
    }
}
