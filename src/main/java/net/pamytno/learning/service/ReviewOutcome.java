package net.pamytno.learning.service;

import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.domain.ReviewResult;

/**
 * Итог ответа по карточке.
 *
 * @param progress новое состояние карточки
 * @param result   ответ пользователя
 */
public record ReviewOutcome(CardProgress progress, ReviewResult result) {

    /**
     * Нужно ли показать карточку ещё раз в текущей сессии.
     *
     * @return {@code true} после ответа «не вспомнил»
     */
    public boolean returnToSession() {
        return result == ReviewResult.FORGOT;
    }
}
