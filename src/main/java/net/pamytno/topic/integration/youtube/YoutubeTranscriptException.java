package net.pamytno.topic.integration.youtube;

/**
 * Субтитры видео получить не удалось: их нет, видео недоступно или YouTube ответил ошибкой.
 */
public class YoutubeTranscriptException extends RuntimeException {

    /**
     * Создаёт исключение с описанием.
     *
     * @param message описание причины
     */
    public YoutubeTranscriptException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с описанием и исходной ошибкой.
     *
     * @param message описание причины
     * @param cause   исходная ошибка
     */
    public YoutubeTranscriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
