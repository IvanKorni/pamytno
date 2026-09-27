package net.pamytno.topic.exception;

import net.pamytno.common.error.BadRequestException;

/**
 * Ссылка не ведёт на видео YouTube.
 */
public class InvalidYoutubeUrlException extends BadRequestException {

    /** Код ошибки. */
    public static final String CODE = "INVALID_YOUTUBE_URL";

    /**
     * Создаёт исключение со стандартным сообщением.
     */
    public InvalidYoutubeUrlException() {
        super(CODE, "Ссылка не ведёт на видео YouTube");
    }
}
