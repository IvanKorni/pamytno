package net.pamytno.deck.domain;

/**
 * Что генерирует задача.
 */
public enum GenerationJobType {

    /** Вопросы по фрагментам единого текста. */
    QUESTIONS,
    /** Карточки по одобренным вопросам. */
    CARDS
}
