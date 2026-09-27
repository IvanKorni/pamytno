package net.pamytno.deck.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Вопрос не найден или принадлежит другому пользователю.
 */
public class QuestionNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "QUESTION_NOT_FOUND";

    /**
     * Создаёт исключение для вопроса.
     *
     * @param questionId идентификатор вопроса
     */
    public QuestionNotFoundException(UUID questionId) {
        super(CODE, "Вопрос " + questionId + " не найден");
    }
}
