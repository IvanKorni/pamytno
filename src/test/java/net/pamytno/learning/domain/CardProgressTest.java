package net.pamytno.learning.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link CardProgress}: путь карточки по расписанию.
 */
class CardProgressTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Test
    @DisplayName("Новая карточка на этапе 0 и к повторению сразу")
    void newCard_isDueImmediately() {
        var progress = progress();

        assertThat(progress.getStage()).isZero();
        assertThat(progress.getNextReviewAt()).isEqualTo(NOW);
        assertThat(progress.getTotalReviews()).isZero();
    }

    @Test
    @DisplayName("Шесть «вспомнил» подряд проводят карточку через все интервалы до изученной")
    void remember_walksThroughScheduleToMastered() {
        var progress = progress();
        var expectedDays = new long[] {1, 3, 7, 14, 30};
        var moment = NOW;
        for (var days : expectedDays) {
            progress.remember(moment);
            assertThat(progress.getNextReviewAt()).isEqualTo(moment.plus(Duration.ofDays(days)));
            moment = progress.getNextReviewAt();
        }

        progress.remember(moment);

        assertThat(progress.isMastered()).isTrue();
        assertThat(progress.getNextReviewAt()).isNull();
        assertThat(progress.getConsecutiveSuccess()).isEqualTo(6);
        assertThat(progress.getTotalRemembered()).isEqualTo(6);
        assertThat(progress.getLastReviewAt()).isEqualTo(moment);
    }

    @Test
    @DisplayName("«Не вспомнил» уменьшает этап, сбрасывает серию и возвращает карточку к повторению сейчас")
    void forget_lowersStageAndMakesCardDueNow() {
        var progress = progress();
        progress.remember(NOW);
        progress.remember(NOW);
        progress.remember(NOW);

        var later = NOW.plus(Duration.ofDays(8));
        progress.forget(later);

        assertThat(progress.getStage()).isEqualTo(2);
        assertThat(progress.getNextReviewAt()).isEqualTo(later);
        assertThat(progress.getConsecutiveSuccess()).isZero();
        assertThat(progress.getTotalForgotten()).isEqualTo(1);
        assertThat(progress.getTotalReviews()).isEqualTo(4);
    }

    @Test
    @DisplayName("CONTINUE не меняет прогресс")
    void continue_doesNotChangeProgress() {
        var progress = progress();

        ReviewResult.CONTINUE.applyTo(progress, NOW.plusSeconds(5));

        assertThat(progress.getTotalReviews()).isZero();
        assertThat(progress.getStage()).isZero();
        assertThat(progress.getNextReviewAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("REMEMBER и FORGOT применяются через ReviewResult")
    void reviewResult_delegatesToProgress() {
        var progress = progress();

        ReviewResult.REMEMBER.applyTo(progress, NOW);
        assertThat(progress.getStage()).isEqualTo(1);

        ReviewResult.FORGOT.applyTo(progress, NOW);
        assertThat(progress.getStage()).isZero();
    }

    /**
     * Прогресс новой карточки.
     *
     * @return прогресс на этапе 0
     */
    private static CardProgress progress() {
        var ref = new CardRef(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        return new CardProgress(ref, new CardSnapshot("Что такое JVM?", "Виртуальная машина."), NOW);
    }
}
