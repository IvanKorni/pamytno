package net.pamytno.topic.domain;

/**
 * Состояние обработки источника.
 */
public enum SourceStatus {

    /** Источник принят, обработка ещё не начата. */
    UPLOADED,
    /** Идёт извлечение текста. */
    PROCESSING,
    /** Текст извлечён. */
    READY,
    /** Текст извлечь не удалось. */
    ERROR;

    /**
     * Проверяет, что обработка ещё не завершена.
     *
     * @return {@code true} для {@link #UPLOADED} и {@link #PROCESSING}
     */
    public boolean isInProgress() {
        return this == UPLOADED || this == PROCESSING;
    }
}
