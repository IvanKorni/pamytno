package net.pamytno.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Копия текста карточки из модуля {@code deck}, чтобы показывать карточки к повторению
 * без обращения к другому модулю.
 *
 * @param front вопрос
 * @param back  ответ
 */
@Embeddable
public record CardSnapshot(
        @Column(name = "card_front", nullable = false, columnDefinition = "text") String front,
        @Column(name = "card_back", nullable = false, columnDefinition = "text") String back
) {
}
