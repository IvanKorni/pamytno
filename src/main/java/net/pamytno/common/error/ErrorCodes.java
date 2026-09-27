package net.pamytno.common.error;

/**
 * Коды ошибок, общие для всех модулей. Коды конкретных модулей объявляются в их исключениях.
 */
public final class ErrorCodes {

    /** Тело или параметры запроса не прошли валидацию. */
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    /** Тело запроса не удалось прочитать. */
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    /** Запрос без валидного токена. */
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    /** Недостаточно прав. */
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    /** Ресурс или путь не найден. */
    public static final String NOT_FOUND = "NOT_FOUND";
    /** HTTP-метод не поддерживается для пути. */
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    /** Загружаемый файл превышает допустимый размер. */
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
    /** Непредвиденная ошибка сервера. */
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    /**
     * Запрещает создание экземпляров класса-справочника.
     */
    private ErrorCodes() {
    }
}
