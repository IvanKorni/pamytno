package net.pamytno.common.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Последний рубеж: любое необработанное исключение логируется и превращается в 500 без деталей реализации.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class UnexpectedExceptionHandler {

    private final ErrorResponseFactory errors;

    /**
     * Обрабатывает непредвиденную ошибку.
     *
     * @param ex исключение
     * @return ответ 500 с кодом {@code INTERNAL_ERROR}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Непредвиденная ошибка обработки запроса", ex);
        return errors.response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                "Внутренняя ошибка сервера");
    }
}
