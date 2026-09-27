package net.pamytno.topic.service.extraction;

import lombok.Getter;
import net.pamytno.topic.domain.SourceErrorCode;

/**
 * Текст источника получить не удалось. Не HTTP-ошибка: причина записывается в сам источник.
 */
@Getter
public class TextExtractionException extends RuntimeException {

    private final SourceErrorCode errorCode;

    /**
     * Создаёт исключение с причиной.
     *
     * @param errorCode код причины
     * @param message   описание для пользователя
     */
    public TextExtractionException(SourceErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Создаёт исключение с причиной и исходной ошибкой.
     *
     * @param errorCode код причины
     * @param message   описание для пользователя
     * @param cause     исходная ошибка
     */
    public TextExtractionException(SourceErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
