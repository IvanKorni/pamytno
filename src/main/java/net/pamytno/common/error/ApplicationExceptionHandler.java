package net.pamytno.common.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Превращает исключения приложения ({@link ApplicationException}) в HTTP-ответы.
 */
@Slf4j
@Order(1)
@RestControllerAdvice
@RequiredArgsConstructor
public class ApplicationExceptionHandler {

    private final ErrorResponseFactory errors;

    /**
     * Ресурс не найден — 404.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return toResponse(HttpStatus.NOT_FOUND, ex);
    }

    /**
     * Конфликт состояния — 409.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return toResponse(HttpStatus.CONFLICT, ex);
    }

    /**
     * Нарушение бизнес-правила во входных данных — 400.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return toResponse(HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Нет аутентификации или неверные учётные данные — 401.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return toResponse(HttpStatus.UNAUTHORIZED, ex);
    }

    /**
     * Операция запрещена — 403.
     *
     * @param ex исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return toResponse(HttpStatus.FORBIDDEN, ex);
    }

    /**
     * Логирует исключение и собирает ответ.
     *
     * @param status HTTP-статус
     * @param ex     исключение приложения
     * @return ответ с ошибкой
     */
    private ResponseEntity<ErrorResponse> toResponse(HttpStatus status, ApplicationException ex) {
        log.warn("Ошибка запроса [{}]: {}", ex.getCode(), ex.getMessage());
        return errors.response(status, ex.getCode(), ex.getMessage());
    }
}
