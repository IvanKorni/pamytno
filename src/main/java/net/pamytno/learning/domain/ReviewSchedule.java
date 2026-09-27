package net.pamytno.learning.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Расписание интервального повторения: этап 0 — сейчас, 1 — через день, 2 — через 3 дня,
 * 3 — через неделю, 4 — через 2 недели, 5 — через 30 дней. После успешного этапа 5 карточка изучена.
 * Позже расписание можно заменить на FSRS, не трогая остальной код.
 */
public final class ReviewSchedule {

    /** Этап изученной карточки: в очередь повторения она больше не попадает. */
    public static final int MASTERED_STAGE = 6;

    private static final List<Duration> INTERVALS = List.of(
            Duration.ZERO, Duration.ofDays(1), Duration.ofDays(3), Duration.ofDays(7), Duration.ofDays(14),
            Duration.ofDays(30));

    /**
     * Запрещает создание экземпляров.
     */
    private ReviewSchedule() {
    }

    /**
     * Этап после ответа «вспомнил».
     *
     * @param stage текущий этап
     * @return следующий этап, не выше {@link #MASTERED_STAGE}
     */
    public static int afterRemember(int stage) {
        return Math.min(stage + 1, MASTERED_STAGE);
    }

    /**
     * Этап после ответа «не вспомнил»: на один назад, но с этапа 1 и ниже — сразу 0.
     *
     * @param stage текущий этап
     * @return уменьшенный этап
     */
    public static int afterForget(int stage) {
        return stage > 1 ? stage - 1 : 0;
    }

    /**
     * Когда повторить карточку на этапе.
     *
     * @param stage этап
     * @param from  момент, от которого отсчитывается интервал
     * @return момент повторения или {@code null} для изученной карточки
     */
    public static Instant nextReviewAt(int stage, Instant from) {
        return stage >= MASTERED_STAGE ? null : from.plus(INTERVALS.get(stage));
    }
}
