package net.pamytno.deck.exception;

import net.pamytno.common.error.ConflictException;

import java.util.UUID;

/**
 * В теме нет одобренных вопросов без карточек — создавать карточки не из чего.
 */
public class NoApprovedQuestionsException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "NO_APPROVED_QUESTIONS";

    /**
     * Создаёт исключение для темы.
     *
     * @param topicId идентификатор темы
     */
    public NoApprovedQuestionsException(UUID topicId) {
        super(CODE, "В теме " + topicId + " нет одобренных вопросов для карточек");
    }
}
