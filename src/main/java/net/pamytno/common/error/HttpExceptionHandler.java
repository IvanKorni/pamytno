package net.pamytno.common.error;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Превращает инфраструктурные ошибки HTTP-слоя (нет пути, не тот метод, большой файл) в единый формат.
 */
@Order(1)
@RestControllerAdvice
@RequiredArgsConstructor
public class HttpExceptionHandler {

    private final ErrorResponseFactory errors;

    /**
     * Запрошен несуществующий путь.
     *
     * @param ex исключение Spring MVC
     * @return ответ 404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return errors.response(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "Ресурс не найден");
    }

    /**
     * HTTP-метод не поддерживается.
     *
     * @param ex исключение Spring MVC
     * @return ответ 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return errors.response(HttpStatus.METHOD_NOT_ALLOWED, ErrorCodes.METHOD_NOT_ALLOWED,
                "Метод " + ex.getMethod() + " не поддерживается");
    }

    /**
     * Загружаемый файл больше лимита {@code spring.servlet.multipart.max-file-size}.
     *
     * @param ex исключение Spring MVC
     * @return ответ 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException ex) {
        return errors.response(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.FILE_TOO_LARGE,
                "Файл превышает допустимый размер");
    }
}
