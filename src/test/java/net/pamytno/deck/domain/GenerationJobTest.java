package net.pamytno.deck.domain;

import net.pamytno.deck.DeckFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link GenerationJob}.
 */
class GenerationJobTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Test
    @DisplayName("Новая задача выполняется, успешное завершение фиксирует количество и время")
    void complete_marksReady() {
        var job = new GenerationJob(DeckFixtures.topic(), GenerationJobType.QUESTIONS, NOW);
        assertThat(job.getStatus()).isEqualTo(GenerationJobStatus.PROCESSING);

        job.complete(7, NOW.plusSeconds(5));

        assertThat(job.getStatus()).isEqualTo(GenerationJobStatus.READY);
        assertThat(job.getItemsCreated()).isEqualTo(7);
        assertThat(job.getCompletedAt()).isEqualTo(NOW.plusSeconds(5));
    }

    @Test
    @DisplayName("Ошибка сохраняет созданное количество и обрезает длинное сообщение")
    void fail_marksErrorAndTruncatesMessage() {
        var job = new GenerationJob(DeckFixtures.topic(), GenerationJobType.CARDS, NOW);

        job.fail(2, "x".repeat(1200), NOW);

        assertThat(job.getStatus()).isEqualTo(GenerationJobStatus.ERROR);
        assertThat(job.getItemsCreated()).isEqualTo(2);
        assertThat(job.getErrorMessage()).hasSize(1000);
    }
}
