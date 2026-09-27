package net.pamytno.deck.domain;

/**
 * Состояние задачи генерации.
 */
public enum GenerationJobStatus {

    /** Задача выполняется. */
    PROCESSING,
    /** Задача завершена успешно. */
    READY,
    /** Задача завершилась ошибкой; созданное до ошибки сохраняется. */
    ERROR
}
