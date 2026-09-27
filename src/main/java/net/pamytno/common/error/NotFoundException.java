package net.pamytno.common.error;

/**
 * Запрошенный ресурс не найден или принадлежит другому пользователю (HTTP 404).
 */
public class NotFoundException extends ApplicationException {

    /**
     * Создаёт исключение с кодом и сообщением.
     *
     * @param code    стабильный код ошибки
     * @param message сообщение на русском
     */
    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
