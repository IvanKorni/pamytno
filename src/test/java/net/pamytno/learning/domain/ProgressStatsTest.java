package net.pamytno.learning.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link ProgressStats}.
 */
class ProgressStatsTest {

    @Test
    @DisplayName("Карточки на изучении — всё, что не новое и не изученное; процент с одним знаком")
    void of_derivesLearningAndPercent() {
        var stats = ProgressStats.of(3, 1, 1, 2, 2);

        assertThat(stats.learningCards()).isEqualTo(1);
        assertThat(stats.progress()).isEqualTo(33.3);
        assertThat(ProgressStats.of(3, 0, 2, 0, 0).progress()).isEqualTo(66.7);
    }

    @Test
    @DisplayName("Без карточек прогресс 0, а не деление на ноль")
    void of_returnsZero_whenNoCards() {
        assertThat(ProgressStats.EMPTY.progress()).isZero();
        assertThat(ProgressStats.EMPTY.totalCards()).isZero();
    }

    @Test
    @DisplayName("Сумма пересчитывает процент по общим числам, а не складывает проценты")
    void plus_recomputesPercent() {
        var sum = ProgressStats.of(3, 1, 1, 1, 1).plus(ProgressStats.of(1, 1, 0, 1, 1));

        assertThat(sum).isEqualTo(new ProgressStats(4, 2, 1, 1, 2, 2, 25.0));
    }
}
