package net.pamytno.deck.integration.ai.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.AiFormatRules;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.integration.ai.GeneratedWord;
import net.pamytno.deck.integration.ai.VocabularyRules;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Провайдер AI через установленный CLI Claude Code или Codex.
 * Вызов идёт без shell, поэтому текст материала не интерпретируется оболочкой.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "pamytno.deck.ai", name = "provider", havingValue = "cli")
public class CliAiProvider implements AiProvider {

    private static final int MAX_ERROR_OUTPUT_LENGTH = 500;
    private static final String QUESTIONS_PROMPT = """
            Сгенерируй вопросы для интервального обучения по материалу ниже.
            Выбирай только содержательные факты, определения, причинно-следственные связи и различия.
            Не задавай вопросы о том, что буквально сказано в тексте. Формулируй самостоятельные вопросы,
            на которые можно ответить по материалу. Не дублируй вопросы.
            %s
            Верни ТОЛЬКО JSON без обёртки ```json и пояснений; переносы строк внутри значений — \\n:
            {"questions":[{"text":"вопрос","sourceFragment":"короткий фрагмент материала"}]}
            Максимум вопросов: %d.

            Материал:
            <material>
            %s
            </material>
            """;
    private static final String CARD_PROMPT = """
            Составь учебную карточку по вопросу и материалу.
            Лицевая сторона должна содержать точный вопрос, оборотная — короткий, но достаточный ответ.
            Не добавляй сведения, которых нет в материале.
            %s
            Верни ТОЛЬКО JSON без обёртки ```json и пояснений; переносы строк внутри значений — \\n:
            {"front":"вопрос","back":"ответ"}

            Вопрос:
            <question>
            %s
            </question>

            Материал:
            <material>
            %s
            </material>
            """;
    private static final String VOCABULARY_PROMPT = """
            %s
            Верни ТОЛЬКО JSON без обёртки ```json и пояснений:
            {"cards":[{"word":"…","translation":"…","definition":"…","example":"…","answer":"…",
            "exampleTranslation":"…"}]}

            %s
            """;

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final CliProcessRunner processRunner;

    /**
     * Генерирует содержательные вопросы через установленный CLI.
     *
     * @param text фрагмент материала
     * @return вопросы с фрагментами-источниками
     */
    @Override
    public List<GeneratedQuestion> generateQuestions(String text) {
        var prompt = QUESTIONS_PROMPT.formatted(AiFormatRules.QUESTIONS, properties.questionsPerChunk(), text);
        var root = parse(execute(prompt));
        var items = root.isArray() ? root : root.path("questions");
        if (!items.isArray()) {
            throw new AiGenerationException("CLI вернул JSON без массива questions");
        }
        var questions = new ArrayList<GeneratedQuestion>();
        for (var item : items) {
            var question = item.path("text").asText("").strip();
            if (!question.isBlank()) {
                questions.add(new GeneratedQuestion(question, item.path("sourceFragment").asText("")));
            }
        }
        return questions;
    }

    /**
     * Генерирует карточку через установленный CLI.
     *
     * @param question вопрос
     * @param context  контекст материала
     * @return карточка
     */
    @Override
    public GeneratedCard generateCard(String question, String context) {
        var root = parse(execute(CARD_PROMPT.formatted(AiFormatRules.CARD, question, context)));
        var front = root.path("front").asText("").strip();
        var back = root.path("back").asText("").strip();
        if (front.isBlank() || back.isBlank()) {
            throw new AiGenerationException("CLI вернул неполную карточку");
        }
        return new GeneratedCard(front, back);
    }

    /**
     * Карточки английских слов через установленный CLI.
     *
     * @param instruction что взять из текста
     * @param text        слова, список или текст
     * @return выражения для карточек
     */
    @Override
    public List<GeneratedWord> generateVocabulary(String instruction, String text) {
        var prompt = VOCABULARY_PROMPT.formatted(VocabularyRules.RULES, VocabularyRules.message(instruction, text));
        var root = parse(execute(prompt));
        var items = root.isArray() ? root : root.path("cards");
        if (!items.isArray()) {
            throw new AiGenerationException("CLI вернул JSON без массива cards");
        }
        var words = new ArrayList<GeneratedWord>();
        items.forEach(item -> words.add(new GeneratedWord(item.path("word").asText(""),
                item.path("translation").asText(""), item.path("definition").asText(""),
                item.path("example").asText(""), item.path("answer").asText(""),
                item.path("exampleTranslation").asText(""))));
        return words;
    }

    /** Выполняет CLI с prompt через stdin. */
    private String execute(String prompt) {
        var cli = properties.cli();
        try {
            return processRunner.run(cli, prompt);
        } catch (IOException e) {
            throw new AiGenerationException("Не удалось запустить AI CLI [" + cli.command() + "]: "
                    + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiGenerationException("Ожидание AI CLI прервано", e);
        }
    }

    /** Извлекает JSON, даже если CLI добавил служебный текст вокруг ответа. */
    private JsonNode parse(String output) {
        var json = output.strip();
        var firstObject = json.indexOf('{');
        var lastObject = json.lastIndexOf('}');
        if (firstObject >= 0 && lastObject > firstObject) {
            json = json.substring(firstObject, lastObject + 1);
        }
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new AiGenerationException("AI CLI вернул невалидный JSON: " + limit(output), e);
        }
    }

    /** Ограничивает вывод CLI, попадающий в сообщение об ошибке. */
    private static String limit(String value) {
        var normalized = value.strip();
        return normalized.length() <= MAX_ERROR_OUTPUT_LENGTH ? normalized
                : normalized.substring(0, MAX_ERROR_OUTPUT_LENGTH) + "…";
    }
}
