package net.pamytno.learning.domain;

import net.pamytno.learning.exception.LearningSessionCompletedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты {@link LearningSession}.
 */
class LearningSessionTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Test
    @DisplayName("Ответы считаются в статистике сессии, CONTINUE не считается")
    void register_countsAnswers() {
        var session = new LearningSession(UUID.randomUUID(), UUID.randomUUID(), 3, NOW);

        session.register(ReviewResult.REMEMBER);
        session.register(ReviewResult.FORGOT);
        session.register(ReviewResult.REMEMBER);
        session.register(ReviewResult.CONTINUE);

        assertThat(session.getCardsTotal()).isEqualTo(3);
        assertThat(session.getCardsRemembered()).isEqualTo(2);
        assertThat(session.getCardsForgotten()).isEqualTo(1);
    }

    @Test
    @DisplayName("Завершённая сессия не принимает ответы, повторное завершение не меняет время")
    void completedSession_rejectsAnswers() {
        var session = new LearningSession(UUID.randomUUID(), UUID.randomUUID(), 1, NOW);

        session.complete(NOW.plusSeconds(60));
        session.complete(NOW.plusSeconds(120));

        assertThat(session.getCompletedAt()).isEqualTo(NOW.plusSeconds(60));
        assertThatThrownBy(() -> session.register(ReviewResult.REMEMBER))
                .isInstanceOf(LearningSessionCompletedException.class);
    }
}
