package net.pamytno.learning.exception;

import net.pamytno.common.error.ConflictException;

import java.util.UUID;

/**
 * Карточка относится к другой теме, чем учебная сессия.
 */
public class LearningSessionTopicMismatchException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "LEARNING_SESSION_TOPIC_MISMATCH";

    /**
     * Создаёт исключение.
     *
     * @param id идентификатор сессии
     */
    public LearningSessionTopicMismatchException(UUID id) {
        super(CODE, "Карточка не относится к теме сессии " + id);
    }
}
