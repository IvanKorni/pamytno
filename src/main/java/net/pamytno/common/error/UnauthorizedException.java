package net.pamytno.common.error;

/**
 * Пользователь не аутентифицирован или передал неверные учётные данные (HTTP 401).
 */
public class UnauthorizedException extends ApplicationException {

    /**
     * Создаёт исключение с кодом и сообщением.
     *
     * @param code    стабильный код ошибки
     * @param message сообщение на русском
     */
    public UnauthorizedException(String code, String message) {
        super(code, message);
    }
}
