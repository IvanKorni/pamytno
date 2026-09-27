package net.pamytno.topic.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Единый текст темы ещё не собран: нет ни одного обработанного источника.
 */
public class TopicContentNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "TOPIC_CONTENT_NOT_FOUND";

    /**
     * Создаёт исключение для темы.
     *
     * @param topicId идентификатор темы
     */
    public TopicContentNotFoundException(UUID topicId) {
        super(CODE, "Единый текст темы " + topicId + " ещё не собран");
    }
}
