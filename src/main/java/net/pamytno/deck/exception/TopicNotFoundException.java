package net.pamytno.deck.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Тема неизвестна модулю карточек или принадлежит другому пользователю.
 */
public class TopicNotFoundException extends NotFoundException {

    /** Код ошибки — тот же, что у модуля тем. */
    public static final String CODE = "TOPIC_NOT_FOUND";

    /**
     * Создаёт исключение для темы.
     *
     * @param topicId идентификатор темы
     */
    public TopicNotFoundException(UUID topicId) {
        super(CODE, "Тема " + topicId + " не найдена");
    }
}
