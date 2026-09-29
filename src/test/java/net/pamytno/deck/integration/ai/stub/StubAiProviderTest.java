package net.pamytno.deck.integration.ai.stub;

import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.integration.ai.GeneratedWord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link StubAiProvider}.
 */
class StubAiProviderTest {

    private final StubAiProvider provider = new StubAiProvider(
            new AiProperties("stub", 2, new AiProperties.Anthropic("model", 100, null, false),
                    new AiProperties.Cli("codex", "gpt-5.6-luna", 180)));

    @Test
    @DisplayName("Вопрос строится на каждое предложение, количество ограничено настройкой")
    void generateQuestions_buildsQuestionPerSentence() {
        var questions = provider.generateQuestions("JVM выполняет байткод. GC чистит память!\nТретье.");

        assertThat(questions).containsExactly(
                new GeneratedQuestion("JVM выполняет байткод.", "JVM выполняет байткод."),
                new GeneratedQuestion("GC чистит память!", "GC чистит память!"));
    }

    @Test
    @DisplayName("Длинное предложение сокращается в тексте вопроса, но целиком остаётся источником")
    void generateQuestions_shortensLongQuote() {
        var sentence = "а".repeat(100) + ".";

        var question = provider.generateQuestions(sentence).getFirst();

        assertThat(question.text()).isEqualTo("а".repeat(80) + "…");
        assertThat(question.sourceFragment()).isEqualTo(sentence);
    }

    @Test
    @DisplayName("Карточка: лицевая сторона — вопрос, оборотная — предложения контекста")
    void generateCard_usesQuestionAndContext() {
        var card = provider.generateCard("Что такое JVM?", "JVM — виртуальная машина.\nОна выполняет байткод.");

        assertThat(card.front()).isEqualTo("Что такое JVM?");
        assertThat(card.back()).isEqualTo("JVM — виртуальная машина. Она выполняет байткод.");
    }

    @Test
    @DisplayName("Слова: выделенные жирным, а без выделений — строки списка без перевода")
    void generateVocabulary_takesBoldWordsOrLines() {
        // when
        var bold = provider.generateVocabulary(null, "We signed a **contract**. It was **reliable**.");
        var list = provider.generateVocabulary("", "contract — договор\n\nreliable\tнадёжный\ncontract");

        // then
        assertThat(bold).extracting(GeneratedWord::word).containsExactly("contract", "reliable");
        assertThat(list).extracting(GeneratedWord::word).containsExactly("contract", "reliable");
        assertThat(list.getFirst().example()).contains("_____");
    }
}
