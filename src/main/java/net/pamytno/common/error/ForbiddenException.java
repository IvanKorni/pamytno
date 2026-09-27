package net.pamytno.common.error;

/**
 * Операция запрещена настройками или правами (HTTP 403).
 */
public class ForbiddenException extends ApplicationException {

    /**
     * Создаёт исключение с кодом и сообщением.
     *
     * @param code    стабильный код ошибки
     * @param message сообщение на русском
     */
    public ForbiddenException(String code, String message) {
        super(code, message);
    }
}
