package net.pamytno.deck.domain;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.exception.QuestionAlreadyHasCardException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты сущности {@link Question}.
 */
class QuestionTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Test
    @DisplayName("Вопрос создаётся GENERATED и наследует тему, владельца и фрагмент")
    void create_linksToChunk() {
        var chunk = DeckFixtures.chunk(DeckFixtures.topic(), "Текст.");

        var question = new Question(chunk, "Что?", "Текст.", NOW);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.GENERATED);
        assertThat(question.getTopicId()).isEqualTo(chunk.getTopicId());
        assertThat(question.getUserId()).isEqualTo(chunk.getUserId());
        assertThat(question.getChunkId()).isEqualTo(chunk.getId());
    }

    @Test
    @DisplayName("Решение можно менять: одобрить, затем отклонить")
    void decisions_canBeChanged() {
        var question = question();

        QuestionDecision.APPROVE.applyTo(question, NOW);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.APPROVED);

        QuestionDecision.REJECT.applyTo(question, NOW);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.REJECTED);
    }

    @Test
    @DisplayName("Формулировку можно поправить до создания карточки")
    void rephrase_changesText() {
        var question = question();

        question.rephrase("Что выполняет JVM?", NOW.plusSeconds(1));

        assertThat(question.getText()).isEqualTo("Что выполняет JVM?");
        assertThat(question.getUpdatedAt()).isEqualTo(NOW.plusSeconds(1));
    }

    @Test
    @DisplayName("После создания карточки решение и формулировка не меняются — 409")
    void cardCreated_locksQuestion() {
        var question = question();
        question.approve(NOW);
        question.markCardCreated(NOW);

        assertThatThrownBy(() -> question.reject(NOW)).isInstanceOf(QuestionAlreadyHasCardException.class);
        assertThatThrownBy(() -> question.rephrase("x", NOW)).isInstanceOf(QuestionAlreadyHasCardException.class);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.CARD_CREATED);
    }

    /**
     * Создаёт новый вопрос.
     *
     * @return вопрос в статусе GENERATED
     */
    private static Question question() {
        return new Question(DeckFixtures.chunk(DeckFixtures.topic(), "Текст."), "Что?", "Текст.", NOW);
    }
}
