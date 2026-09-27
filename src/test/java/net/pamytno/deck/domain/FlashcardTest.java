package net.pamytno.deck.domain;

import net.pamytno.deck.DeckFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты сущности {@link Flashcard}.
 */
class FlashcardTest {

    @Test
    @DisplayName("Карточка наследует от вопроса тему, владельца и цитату-источник")
    void create_inheritsFromQuestion() {
        var question = new Question(DeckFixtures.chunk(DeckFixtures.topic(), "JVM."), "Что такое JVM?", "JVM.",
                Instant.EPOCH);

        var card = new Flashcard(question, "Что такое JVM?", "Виртуальная машина Java.", Instant.EPOCH);

        assertThat(card.getTopicId()).isEqualTo(question.getTopicId());
        assertThat(card.getUserId()).isEqualTo(question.getUserId());
        assertThat(card.getQuestionId()).isEqualTo(question.getId());
        assertThat(card.getSourceFragment()).isEqualTo("JVM.");
    }
}
