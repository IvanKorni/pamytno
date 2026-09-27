package net.pamytno.deck.domain;

import java.time.Instant;

/**
 * Решение пользователя по вопросу: изучать или нет.
 */
public enum QuestionDecision {

    /** Изучать вопрос. */
    APPROVE,
    /** Не изучать вопрос. */
    REJECT;

    /**
     * Применяет решение к вопросу.
     *
     * @param question вопрос
     * @param now      момент решения
     */
    public void applyTo(Question question, Instant now) {
        if (this == APPROVE) {
            question.approve(now);
        } else {
            question.reject(now);
        }
    }
}
