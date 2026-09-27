package net.pamytno.learning.domain;

/**
 * Прогресс изучения набора карточек.
 *
 * @param totalCards    всего карточек
 * @param newCards      ни разу не повторялись
 * @param learningCards повторялись, но ещё не изучены
 * @param masteredCards изучены
 * @param dueCards      ожидают повторения сейчас
 * @param dueToday      к повторению до конца сегодняшнего дня
 * @param progress      процент изученных, 0–100 с одним знаком после запятой
 */
public record ProgressStats(int totalCards, int newCards, int learningCards, int masteredCards, int dueCards,
                            int dueToday, double progress) {

    private static final double PERCENT = 100.0;
    private static final double ONE_DECIMAL = 10.0;

    /** Прогресс без карточек. */
    public static final ProgressStats EMPTY = of(0, 0, 0, 0, 0);

    /**
     * Считает производные показатели по счётчикам.
     *
     * @param total    всего карточек
     * @param fresh    ни разу не повторялись
     * @param mastered изучены
     * @param due      к повторению сейчас
     * @param dueToday к повторению сегодня
     * @return прогресс
     */
    public static ProgressStats of(int total, int fresh, int mastered, int due, int dueToday) {
        var percent = total == 0 ? 0.0 : Math.round(mastered * PERCENT * ONE_DECIMAL / total) / ONE_DECIMAL;
        return new ProgressStats(total, fresh, total - fresh - mastered, mastered, due, dueToday, percent);
    }

    /**
     * Суммирует прогресс двух наборов карточек.
     *
     * @param other другой набор
     * @return общий прогресс
     */
    public ProgressStats plus(ProgressStats other) {
        return of(totalCards + other.totalCards, newCards + other.newCards, masteredCards + other.masteredCards,
                dueCards + other.dueCards, dueToday + other.dueToday);
    }
}
