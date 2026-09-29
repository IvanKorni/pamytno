package net.pamytno.deck.integration.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.anthropic.models.messages.StructuredTextBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.integration.ai.GeneratedWord;
import net.pamytno.deck.integration.ai.VocabularyRules;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Провайдер Claude через официальный Anthropic Java SDK. Ответ ограничен JSON-схемой
 * (structured outputs), поэтому разбирать свободный текст не нужно.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "pamytno.deck.ai", name = "provider", havingValue = "anthropic")
public class AnthropicAiProvider implements AiProvider {

    private static final String FALLBACK_BETA = "server-side-fallback-2026-07-01";

    private final AnthropicClient client;
    private final AiProperties properties;

    /**
     * Вопросы по фрагменту материала.
     *
     * @param text фрагмент единого текста темы
     * @return вопросы с цитатами
     */
    @Override
    public List<GeneratedQuestion> generateQuestions(String text) {
        var system = AnthropicPrompts.QUESTIONS_SYSTEM.formatted(properties.questionsPerChunk());
        var payload = call(params(system, AnthropicPrompts.questionsMessage(text), QuestionsPayload.class));
        return payload.questions() == null ? List.of() : payload.questions().stream()
                .map(item -> new GeneratedQuestion(item.text(), item.sourceFragment()))
                .toList();
    }

    /**
     * Карточка по вопросу и контексту.
     *
     * @param question вопрос
     * @param context  фрагмент материала
     * @return карточка
     */
    @Override
    public GeneratedCard generateCard(String question, String context) {
        var payload = call(params(AnthropicPrompts.CARD_SYSTEM, AnthropicPrompts.cardMessage(question, context),
                CardPayload.class));
        return new GeneratedCard(payload.front(), payload.back());
    }

    /**
     * Карточки английских слов по инструкции ученика.
     *
     * @param instruction что взять из текста
     * @param text        слова, список или текст
     * @return выражения для карточек
     */
    @Override
    public List<GeneratedWord> generateVocabulary(String instruction, String text) {
        var payload = call(params(VocabularyRules.RULES, VocabularyRules.message(instruction, text),
                VocabularyPayload.class));
        return payload.cards() == null ? List.of() : payload.cards().stream()
                .map(item -> new GeneratedWord(item.word(), item.translation(), item.definition(), item.example(),
                        item.answer(), item.exampleTranslation()))
                .toList();
    }

    /**
     * Параметры запроса со схемой ответа, моделью, лимитами и серверным fallback при отказе.
     *
     * @param system      инструкция
     * @param userMessage сообщение с материалом
     * @param schema      класс схемы ответа
     * @param <T>         тип ответа
     * @return параметры запроса
     */
    private <T> StructuredMessageCreateParams<T> params(String system, String userMessage, Class<T> schema) {
        var settings = properties.anthropic();
        var outputConfig = StructuredOutputConfig.<T>builder().format(schema);
        if (settings.effort() != null && !settings.effort().isBlank()) {
            outputConfig.effort(OutputConfig.Effort.of(settings.effort()));
        }
        var builder = MessageCreateParams.builder()
                .model(settings.model())
                .maxTokens(settings.maxTokens())
                .system(system)
                .addUserMessage(userMessage);
        if (settings.refusalFallback()) {
            builder.putAdditionalHeader("anthropic-beta", FALLBACK_BETA)
                    .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"));
        }
        return builder.outputConfig(outputConfig.build()).build();
    }

    /**
     * Выполняет запрос и достаёт типизированный ответ.
     *
     * @param params параметры запроса
     * @param <T>    тип ответа
     * @return ответ модели по схеме
     * @throws AiGenerationException при ошибке API, отказе, обрезанном или пустом ответе
     */
    private <T> T call(StructuredMessageCreateParams<T> params) {
        try {
            var message = client.messages().create(params);
            requireCompleted(message);
            log.info("Claude ответил: модель [{}], токены вход/выход [{}/{}]", message.model().asString(),
                    message.usage().inputTokens(), message.usage().outputTokens());
            return message.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(StructuredTextBlock::text)
                    .findFirst()
                    .orElseThrow(() -> new AiGenerationException("Модель не вернула ответ"));
        } catch (AnthropicException e) {
            log.warn("Ошибка Anthropic API: {}", e.getMessage());
            throw new AiGenerationException("Ошибка Anthropic API: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет, что модель ответила полностью, а не отказала и не упёрлась в лимит.
     *
     * @param message ответ API
     */
    private static void requireCompleted(StructuredMessage<?> message) {
        var stopReason = message.stopReason().orElse(null);
        if (StopReason.REFUSAL.equals(stopReason)) {
            throw new AiGenerationException("Модель отказалась отвечать на этот материал");
        }
        if (StopReason.MAX_TOKENS.equals(stopReason)) {
            throw new AiGenerationException("Ответ модели обрезан лимитом токенов (AI_MAX_TOKENS)");
        }
    }
}
