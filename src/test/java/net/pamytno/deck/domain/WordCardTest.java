package net.pamytno.deck.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link WordCard}.
 */
class WordCardTest {

    @Test
    @DisplayName("Спереди — объяснение и предложение с подсказкой, сзади — слово, перевод и перевод примера")
    void sides_followVocabularyLayout() {
        // given
        var card = WordCard.from(new WordEntry("contract", "договор, контракт", "A formal written agreement.",
                "They signed a contract to buy the house.", "contract", "Они подписали договор о покупке дома."))
                .orElseThrow();

        // when
        var front = card.front();
        var back = card.back();

        // then
        assertThat(front).isEqualTo("A formal written agreement.\n\nThey signed a co_____ to buy the house.");
        assertThat(back).isEqualTo("**contract**\nдоговор, контракт\n\nОни подписали договор о покупке дома.");
    }

    @Test
    @DisplayName("Пустое поле или выражение, которого нет в предложении, не дают карточки; форма необязательна")
    void from_rejectsIncompleteAnswer() {
        // when
        var noTranslation = WordCard.from(new WordEntry("reliable", " ", "You can trust it.", "A reliable car.",
                "reliable", "Надёжная машина."));
        var notInSentence = WordCard.from(new WordEntry("reliable", "надёжный", "You can trust it.",
                "Good friends help.", "reliable", "Перевод."));
        var withoutAnswer = WordCard.from(new WordEntry(" reliable ", "надёжный", "You can trust it.",
                "Reliable friends help. ", null, "Надёжные друзья помогают."));

        // then
        assertThat(noTranslation).isEmpty();
        assertThat(notInSentence).isEmpty();
        assertThat(withoutAnswer).get().satisfies(card -> {
            assertThat(card.word()).isEqualTo("reliable");
            assertThat(card.example()).isEqualTo("Re_____ friends help.");
        });
    }
}
