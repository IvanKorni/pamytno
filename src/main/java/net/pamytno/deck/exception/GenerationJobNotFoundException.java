package net.pamytno.deck.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Задача генерации не найдена или принадлежит другому пользователю.
 */
public class GenerationJobNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "GENERATION_JOB_NOT_FOUND";

    /**
     * Создаёт исключение для задачи.
     *
     * @param jobId идентификатор задачи
     */
    public GenerationJobNotFoundException(UUID jobId) {
        super(CODE, "Задача генерации " + jobId + " не найдена");
    }
}
