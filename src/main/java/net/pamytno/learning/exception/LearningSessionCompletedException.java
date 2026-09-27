package net.pamytno.learning.exception;

import net.pamytno.common.error.ConflictException;

import java.util.UUID;

/**
 * Учебная сессия уже завершена — ответы в ней больше не учитываются.
 */
public class LearningSessionCompletedException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "LEARNING_SESSION_COMPLETED";

    /**
     * Создаёт исключение.
     *
     * @param id идентификатор сессии
     */
    public LearningSessionCompletedException(UUID id) {
        super(CODE, "Учебная сессия " + id + " уже завершена");
    }
}
