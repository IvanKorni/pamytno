package net.pamytno.learning.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link ReviewSchedule}.
 */
class ReviewScheduleTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Test
    @DisplayName("Интервалы этапов: сейчас, 1, 3, 7, 14, 30 дней; изученная карточка не планируется")
    void nextReviewAt_followsSchedule() {
        assertThat(ReviewSchedule.nextReviewAt(0, NOW)).isEqualTo(NOW);
        assertThat(ReviewSchedule.nextReviewAt(1, NOW)).isEqualTo(NOW.plus(Duration.ofDays(1)));
        assertThat(ReviewSchedule.nextReviewAt(2, NOW)).isEqualTo(NOW.plus(Duration.ofDays(3)));
        assertThat(ReviewSchedule.nextReviewAt(3, NOW)).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(ReviewSchedule.nextReviewAt(4, NOW)).isEqualTo(NOW.plus(Duration.ofDays(14)));
        assertThat(ReviewSchedule.nextReviewAt(5, NOW)).isEqualTo(NOW.plus(Duration.ofDays(30)));
        assertThat(ReviewSchedule.nextReviewAt(ReviewSchedule.MASTERED_STAGE, NOW)).isNull();
    }

    @Test
    @DisplayName("«Вспомнил» повышает этап, но не выше изученного")
    void afterRemember_incrementsUpToMastered() {
        assertThat(ReviewSchedule.afterRemember(0)).isEqualTo(1);
        assertThat(ReviewSchedule.afterRemember(5)).isEqualTo(ReviewSchedule.MASTERED_STAGE);
        assertThat(ReviewSchedule.afterRemember(ReviewSchedule.MASTERED_STAGE))
                .isEqualTo(ReviewSchedule.MASTERED_STAGE);
    }

    @Test
    @DisplayName("«Не вспомнил»: при этапе > 1 — на один назад, иначе 0")
    void afterForget_followsSpecification() {
        assertThat(ReviewSchedule.afterForget(4)).isEqualTo(3);
        assertThat(ReviewSchedule.afterForget(2)).isEqualTo(1);
        assertThat(ReviewSchedule.afterForget(1)).isZero();
        assertThat(ReviewSchedule.afterForget(0)).isZero();
    }
}
