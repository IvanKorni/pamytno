package net.pamytno.deck.exception;

import net.pamytno.common.error.ConflictException;

/**
 * Генерация того же вида для темы уже идёт.
 */
public class GenerationInProgressException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "GENERATION_IN_PROGRESS";

    /**
     * Создаёт исключение со стандартным сообщением.
     */
    public GenerationInProgressException() {
        super(CODE, "Генерация для этой темы уже выполняется");
    }
}
