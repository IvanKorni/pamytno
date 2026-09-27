package net.pamytno.deck.exception;

import net.pamytno.common.error.ConflictException;

import java.util.UUID;

/**
 * По вопросу уже создана карточка: решение и формулировка вопроса больше не меняются,
 * редактируется карточка.
 */
public class QuestionAlreadyHasCardException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "QUESTION_ALREADY_HAS_CARD";

    /**
     * Создаёт исключение для вопроса.
     *
     * @param questionId идентификатор вопроса
     */
    public QuestionAlreadyHasCardException(UUID questionId) {
        super(CODE, "По вопросу " + questionId + " уже создана карточка");
    }
}
