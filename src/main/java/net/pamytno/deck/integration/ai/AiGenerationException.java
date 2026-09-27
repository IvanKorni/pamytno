package net.pamytno.deck.integration.ai;

/**
 * Модель не смогла выполнить генерацию: сеть, лимиты, отказ или непригодный ответ.
 */
public class AiGenerationException extends RuntimeException {

    /**
     * Создаёт исключение с описанием.
     *
     * @param message описание причины
     */
    public AiGenerationException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с описанием и исходной ошибкой.
     *
     * @param message описание причины
     * @param cause   исходная ошибка
     */
    public AiGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
