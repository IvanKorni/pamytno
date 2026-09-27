package net.pamytno.learning.domain;

import java.time.Instant;

/**
 * Ответ пользователя по карточке в режиме обучения.
 */
public enum ReviewResult {

    /** Вспомнил: карточка переходит на следующий этап. */
    REMEMBER,
    /** Не вспомнил: этап уменьшается, карточка сразу снова к повторению. */
    FORGOT,
    /** Не оценка памяти — переход к следующей части карточки; прогресс не меняется. */
    CONTINUE;

    /**
     * Применяет ответ к прогрессу карточки.
     *
     * @param progress прогресс карточки
     * @param now      момент ответа
     */
    public void applyTo(CardProgress progress, Instant now) {
        switch (this) {
            case REMEMBER -> progress.remember(now);
            case FORGOT -> progress.forget(now);
            case CONTINUE -> {
                // Прогресс намеренно не меняется: это навигация, а не оценка памяти.
            }
            default -> throw new IllegalStateException("Неизвестный ответ: " + this);
        }
    }
}
