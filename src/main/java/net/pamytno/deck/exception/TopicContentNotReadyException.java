package net.pamytno.deck.exception;

import net.pamytno.common.error.ConflictException;

import java.util.UUID;

/**
 * У темы ещё нет готового текста для генерации вопросов.
 */
public class TopicContentNotReadyException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "TOPIC_CONTENT_NOT_READY";

    /**
     * Создаёт исключение для темы.
     *
     * @param topicId идентификатор темы
     */
    public TopicContentNotReadyException(UUID topicId) {
        super(CODE, "У темы " + topicId + " ещё нет обработанного текста");
    }
}
