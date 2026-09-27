package net.pamytno.common.error;

/**
 * Операция конфликтует с текущим состоянием ресурса (HTTP 409).
 */
public class ConflictException extends ApplicationException {

    /**
     * Создаёт исключение с кодом и сообщением.
     *
     * @param code    стабильный код ошибки
     * @param message сообщение на русском
     */
    public ConflictException(String code, String message) {
        super(code, message);
    }
}
