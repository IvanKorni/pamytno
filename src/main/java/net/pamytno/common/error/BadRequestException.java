package net.pamytno.common.error;

/**
 * Запрос корректен синтаксически, но нарушает бизнес-правила (HTTP 400).
 */
public class BadRequestException extends ApplicationException {

    /**
     * Создаёт исключение с кодом и сообщением.
     *
     * @param code    стабильный код ошибки
     * @param message сообщение на русском
     */
    public BadRequestException(String code, String message) {
        super(code, message);
    }
}
