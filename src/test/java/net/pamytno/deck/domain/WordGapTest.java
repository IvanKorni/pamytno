package net.pamytno.deck.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link WordGap}.
 */
class WordGapTest {

    @Test
    @DisplayName("Пропуск закрывает форму из предложения, подсказка — её первые две буквы")
    void apply_hidesSentenceForm() {
        // when
        var gapped = WordGap.apply("After a month, I finally got used to the routine.", "got used to", "get used to");

        // then
        assertThat(gapped).contains("After a month, I finally go_____ the routine.");
    }

    @Test
    @DisplayName("Форма ищется целым словом: «use» внутри «because» не закрывается")
    void apply_matchesWholeWords() {
        // when
        var gapped = WordGap.apply("I stay because I use it daily.", "use", "use");

        // then
        assertThat(gapped).contains("I stay because I us_____ it daily.");
    }

    @Test
    @DisplayName("Без формы в предложении ищется словарная форма, затем пропуск модели дополняется подсказкой")
    void apply_fallsBackToWordAndModelGap() {
        // when
        var byWord = WordGap.apply("Make a decision today.", "", "make a decision");
        var modelGap = WordGap.apply("She ___ quickly.", "made a decision", "make a decision");
        var nothing = WordGap.apply("She left quickly.", "", "make a decision");

        // then
        assertThat(byWord).contains("Ma_____ today.");
        assertThat(modelGap).contains("She ma_____ quickly.");
        assertThat(nothing).isEmpty();
    }

    @Test
    @DisplayName("У короткого слова подсказка оставляет хотя бы одну скрытую букву")
    void hinted_keepsLetterHidden() {
        // when
        var twoLetters = WordGap.hinted("go");
        var oneLetter = WordGap.hinted("a");

        // then
        assertThat(twoLetters).isEqualTo("g_____");
        assertThat(oneLetter).isEqualTo("_____");
    }
}
