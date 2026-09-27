package net.pamytno.common.error;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

/**
 * Превращает ошибки разбора и валидации входных данных в ответ 400 с кодом {@code VALIDATION_FAILED}.
 */
@Order(1)
@RestControllerAdvice
@RequiredArgsConstructor
public class ValidationExceptionHandler {

    private final ErrorResponseFactory errors;

    /**
     * Тело запроса не прошло bean validation.
     *
     * @param ex исключение Spring MVC
     * @return ответ с перечнем некорректных полей
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(ValidationExceptionHandler::describe)
                .collect(Collectors.joining("; "));
        return validationFailed(message);
    }

    /**
     * Параметры метода контроллера не прошли валидацию.
     *
     * @param ex исключение Spring MVC
     * @return ответ с ошибкой валидации
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameters(HandlerMethodValidationException ex) {
        return validationFailed("Параметры запроса не прошли проверку");
    }

    /**
     * Не передан обязательный параметр, часть multipart-запроса или параметр неверного типа.
     *
     * @param ex исключение Spring MVC
     * @return ответ с ошибкой валидации
     */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleMissingOrWrongParameter(Exception ex) {
        return validationFailed("Неверные или отсутствующие параметры запроса");
    }

    /**
     * Тело запроса не удалось прочитать (битый JSON, неизвестное значение enum).
     *
     * @param ex исключение Spring MVC
     * @return ответ с кодом {@code MALFORMED_REQUEST}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return errors.response(HttpStatus.BAD_REQUEST, ErrorCodes.MALFORMED_REQUEST,
                "Не удалось прочитать тело запроса");
    }

    /**
     * Формирует описание ошибки одного поля.
     *
     * @param error ошибка поля
     * @return строка вида {@code "title: не должно быть пустым"}
     */
    private static String describe(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    /**
     * Собирает ответ 400 с кодом {@code VALIDATION_FAILED}.
     *
     * @param message описание ошибки
     * @return ответ с ошибкой
     */
    private ResponseEntity<ErrorResponse> validationFailed(String message) {
        return errors.response(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, message);
    }
}
