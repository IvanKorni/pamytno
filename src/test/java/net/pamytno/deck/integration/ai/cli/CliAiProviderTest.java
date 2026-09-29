package net.pamytno.deck.integration.ai.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.AiFormatRules;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.integration.ai.GeneratedWord;
import net.pamytno.deck.integration.ai.VocabularyRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link CliAiProvider}: промпты с правилами оформления и разбор ответа CLI без запуска процесса.
 */
@ExtendWith(MockitoExtension.class)
class CliAiProviderTest {

    private static final AiProperties PROPERTIES = new AiProperties("cli", 3,
            new AiProperties.Anthropic("model", 100, null, false),
            new AiProperties.Cli("codex", "gpt-5.6-luna", 180));

    @Mock
    private CliProcessRunner processRunner;

    private CliAiProvider provider;

    @BeforeEach
    void setUp() {
        provider = new CliAiProvider(PROPERTIES, new ObjectMapper(), processRunner);
    }

    @Test
    @DisplayName("Промпт вопросов несёт правило задач с вариантами, лимит и материал; пустые вопросы отбрасываются")
    void generateQuestions_sendsFormatRulesAndParsesAnswer() throws Exception {
        // given
        when(processRunner.run(eq(PROPERTIES.cli()), anyString())).thenReturn("""
                Ответ: {"questions":[
                  {"text":"Условие.\\n**Какой подход?**\\nA. CQRS\\nB. Шардирование","sourceFragment":"A. CQRS"},
                  {"text":"  ","sourceFragment":""}]}""");

        // when
        var questions = provider.generateQuestions("Материал задачи");

        // then
        assertThat(questions).containsExactly(
                new GeneratedQuestion("Условие.\n**Какой подход?**\nA. CQRS\nB. Шардирование", "A. CQRS"));
        assertThat(sentPrompt()).contains(AiFormatRules.QUESTIONS, "Максимум вопросов: 3",
                "<material>\nМатериал задачи\n</material>");
    }

    @Test
    @DisplayName("Карточка сохраняет переносы строк и разметку ответа, промпт несёт правила оформления")
    void generateCard_keepsMarkupAndSendsFormatRules() throws Exception {
        // given
        when(processRunner.run(eq(PROPERTIES.cli()), anyString())).thenReturn("""
                {"front":"Какой подход?\\nA. CQRS\\nB. Шардирование",
                 "back":"**A — CQRS.**\\n\\n- запись — в Kafka\\n- чтение — из Redis"}""");

        // when
        var card = provider.generateCard("Какой подход?", "Материал задачи");

        // then
        assertThat(card).isEqualTo(new GeneratedCard("Какой подход?\nA. CQRS\nB. Шардирование",
                "**A — CQRS.**\n\n- запись — в Kafka\n- чтение — из Redis"));
        assertThat(sentPrompt()).contains(AiFormatRules.CARD, "<question>\nКакой подход?\n</question>");
    }

    @Test
    @DisplayName("Невалидный JSON и карточка без ответа превращаются в AiGenerationException")
    void invalidAnswer_isAiGenerationException() throws Exception {
        // given
        when(processRunner.run(eq(PROPERTIES.cli()), anyString()))
                .thenReturn("не JSON")
                .thenReturn("{\"front\":\"Вопрос?\",\"back\":\" \"}");

        // when / then
        assertThatThrownBy(() -> provider.generateQuestions("Материал"))
                .isInstanceOf(AiGenerationException.class).hasMessageContaining("невалидный JSON");
        assertThatThrownBy(() -> provider.generateCard("Вопрос?", "Материал"))
                .isInstanceOf(AiGenerationException.class).hasMessageContaining("неполную карточку");
    }

    @Test
    @DisplayName("Промпт слов несёт правила, инструкцию и текст; ответ разбирается в GeneratedWord")
    void generateVocabulary_sendsRulesAndParsesAnswer() throws Exception {
        // given
        when(processRunner.run(eq(PROPERTIES.cli()), anyString())).thenReturn("""
                {"cards":[{"word":"contract","translation":"договор","definition":"A formal agreement.",
                  "example":"We signed a contract.","answer":"contract",
                  "exampleTranslation":"Мы подписали договор."}]}""");

        // when
        var words = provider.generateVocabulary("слова, выделенные жирным", "We signed a **contract**.");

        // then
        assertThat(words).containsExactly(new GeneratedWord("contract", "договор", "A formal agreement.",
                "We signed a contract.", "contract", "Мы подписали договор."));
        assertThat(sentPrompt()).contains(VocabularyRules.RULES,
                "<instruction>\nслова, выделенные жирным\n</instruction>",
                "<material>\nWe signed a **contract**.\n</material>");
    }

    @Test
    @DisplayName("Ответ без массива cards превращается в AiGenerationException")
    void generateVocabulary_rejectsAnswerWithoutCards() throws Exception {
        // given
        when(processRunner.run(eq(PROPERTIES.cli()), anyString())).thenReturn("{\"words\":1}");

        // when / then
        assertThatThrownBy(() -> provider.generateVocabulary(null, "contract"))
                .isInstanceOf(AiGenerationException.class).hasMessageContaining("cards");
    }

    /**
     * Промпт, переданный в CLI.
     *
     * @return текст промпта
     */
    private String sentPrompt() throws Exception {
        var prompt = ArgumentCaptor.forClass(String.class);
        verify(processRunner).run(eq(PROPERTIES.cli()), prompt.capture());
        return prompt.getValue();
    }
}
