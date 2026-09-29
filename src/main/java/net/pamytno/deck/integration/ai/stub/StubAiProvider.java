package net.pamytno.deck.integration.ai.stub;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.integration.ai.GeneratedWord;
import net.pamytno.deck.integration.ai.VocabularyRules;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Детерминированная заглушка без сети: вопрос на каждое предложение, ответ — предложения контекста;
 * слово — на каждое выделение {@code **…**}, а без выделений — на каждую строку.
 * Для локального запуска без ключей API и для тестов. Включается {@code pamytno.deck.ai.provider=stub}.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "pamytno.deck.ai", name = "provider", havingValue = "stub", matchIfMissing = true)
public class StubAiProvider implements AiProvider {

    private static final Pattern SENTENCE_BREAK = Pattern.compile("(?<=[.!?…])\\s+|\\n+");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern TRANSLATION_SEPARATOR = Pattern.compile("\\s+[—–-]\\s+|\\t");
    private static final int MAX_ANSWER_SENTENCES = 10;
    private static final int MAX_QUESTION_LENGTH = 80;

    private final AiProperties properties;

    /**
     * Вопрос на каждое предложение фрагмента, не больше {@code questionsPerChunk}.
     *
     * @param text фрагмент материала
     * @return вопросы с предложением-источником
     */
    @Override
    public List<GeneratedQuestion> generateQuestions(String text) {
        return sentences(text).stream()
                .limit(properties.questionsPerChunk())
                .map(sentence -> new GeneratedQuestion(shorten(sentence),
                        sentence))
                .toList();
    }

    /**
     * Карточка: вопрос как есть, ответ — первые предложения контекста.
     *
     * @param question вопрос
     * @param context  контекст
     * @return карточка
     */
    @Override
    public GeneratedCard generateCard(String question, String context) {
        var answer = sentences(context).stream().limit(MAX_ANSWER_SENTENCES).collect(Collectors.joining(" "));
        return new GeneratedCard(question, answer);
    }

    /**
     * Карточка на каждое выделенное жирным выражение или, если выделений нет, на каждую строку;
     * у пары «слово — перевод» берётся слово. Инструкция не учитывается.
     *
     * @param instruction что взять из текста
     * @param text        слова, список или текст
     * @return выражения с шаблонными объяснением, примером и переводами
     */
    @Override
    public List<GeneratedWord> generateVocabulary(String instruction, String text) {
        var bold = BOLD.matcher(text).results().map(match -> match.group(1)).toList();
        var words = bold.isEmpty() ? text.lines().map(line -> TRANSLATION_SEPARATOR.split(line, 2)[0]).toList() : bold;
        return words.stream().map(String::strip).filter(word -> !word.isEmpty()).distinct()
                .limit(VocabularyRules.MAX_WORDS)
                .map(word -> new GeneratedWord(word, "перевод: " + word, "The meaning of this word.",
                        "I often use _____ in class.", "Я часто использую «" + word + "» на уроке."))
                .toList();
    }

    /**
     * Делит текст на непустые предложения и строки.
     *
     * @param text текст
     * @return предложения
     */
    private static List<String> sentences(String text) {
        return SENTENCE_BREAK.splitAsStream(text).map(String::strip).filter(part -> !part.isEmpty()).toList();
    }

    /**
     * Сокращает предложение для текста вопроса.
     *
     * @param sentence предложение
     * @return предложение не длиннее {@value #MAX_QUESTION_LENGTH} символов
     */
    private static String shorten(String sentence) {
        return sentence.length() <= MAX_QUESTION_LENGTH ? sentence : sentence.substring(0, MAX_QUESTION_LENGTH) + "…";
    }
}
