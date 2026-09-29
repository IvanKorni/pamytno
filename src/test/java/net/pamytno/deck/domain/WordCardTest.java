package net.pamytno.deck.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link WordCard}.
 */
class WordCardTest {

    @Test
    @DisplayName("Лицевая сторона — объяснение и предложение с пропуском, оборотная — слово, перевод и перевод примера")
    void sides_followVocabularyLayout() {
        // given
        var card = WordCard.of("contract", "договор, контракт", "A formal written agreement.",
                "They signed a _____ to buy the house.", "Они подписали договор о покупке дома.").orElseThrow();

        // when
        var front = card.front();
        var back = card.back();

        // then
        assertThat(front).isEqualTo("A formal written agreement.\n\nThey signed a _____ to buy the house.");
        assertThat(back).isEqualTo("**contract**\nдоговор, контракт\n\nОни подписали договор о покупке дома.");
    }

    @Test
    @DisplayName("Пропуск любой длины приводится к одному виду, лишние пробелы убираются")
    void of_normalizesGap() {
        // when
        var card = WordCard.of(" make a decision ", "принять решение", "To choose what to do.",
                "She ___ quickly. ", "Она быстро приняла решение.");

        // then
        assertThat(card).get().satisfies(found -> {
            assertThat(found.word()).isEqualTo("make a decision");
            assertThat(found.example()).isEqualTo("She _____ quickly.");
        });
    }

    @Test
    @DisplayName("Выражение, вписанное вместо пропуска, заменяется пропуском без учёта регистра")
    void of_replacesWordWithGap() {
        // when
        var card = WordCard.of("reliable", "надёжный", "You can trust it.", "Reliable friends help.",
                "Надёжные друзья помогают.");

        // then
        assertThat(card).get().extracting(WordCard::example).isEqualTo("_____ friends help.");
    }

    @Test
    @DisplayName("Без пропуска и без выражения в предложении, как и с пустым полем, карточки нет")
    void of_rejectsIncompleteAnswer() {
        // when
        var noGap = WordCard.of("reliable", "надёжный", "You can trust it.", "Good friends help.", "Перевод.");
        var noTranslation = WordCard.of("reliable", " ", "You can trust it.", "A _____ car.", "Надёжная машина.");

        // then
        assertThat(noGap).isEmpty();
        assertThat(noTranslation).isEmpty();
    }
}
