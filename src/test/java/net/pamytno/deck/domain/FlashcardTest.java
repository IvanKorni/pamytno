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

    @Test
    @DisplayName("Редактирование меняет только переданные стороны")
    void edit_changesOnlyGivenSides() {
        var question = new Question(DeckFixtures.chunk(DeckFixtures.topic(), "JVM."), "Что?", "JVM.", Instant.EPOCH);
        var card = new Flashcard(question, "Что?", "Ответ.", Instant.EPOCH);

        card.edit(null, "Новый ответ.", Instant.EPOCH.plusSeconds(1));

        assertThat(card.getFront()).isEqualTo("Что?");
        assertThat(card.getBack()).isEqualTo("Новый ответ.");
        assertThat(card.getUpdatedAt()).isEqualTo(Instant.EPOCH.plusSeconds(1));
    }
}
