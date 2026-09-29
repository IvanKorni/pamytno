package net.pamytno.deck.integration.ai.anthropic;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.AiFormatRules;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.integration.ai.GeneratedWord;
import net.pamytno.deck.integration.ai.VocabularyRules;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тест {@link AnthropicAiProvider} против WireMock, имитирующего Anthropic Messages API.
 * Реальная сеть и ключи не используются.
 */
class AnthropicAiProviderTest {

    private static final WireMockServer ANTHROPIC = new WireMockServer(options().dynamicPort());

    private AnthropicAiProvider provider;

    @BeforeAll
    static void startServer() {
        ANTHROPIC.start();
    }

    @AfterAll
    static void stopServer() {
        ANTHROPIC.stop();
    }

    @BeforeEach
    void setUp() {
        ANTHROPIC.resetAll();
        var client = AnthropicOkHttpClient.builder()
                .apiKey("test-key")
                .baseUrl(ANTHROPIC.baseUrl())
                .maxRetries(0)
                .build();
        var properties = new AiProperties("anthropic", 5,
                new AiProperties.Anthropic("claude-opus-5", 16000, "medium", true),
                new AiProperties.Cli("codex", "gpt-5.6-luna", 180));
        provider = new AnthropicAiProvider(client, properties);
    }

    @Test
    @DisplayName("Вопросы запрашиваются со схемой ответа и fallback, ответ разбирается в GeneratedQuestion")
    void generateQuestions_sendsStructuredRequestAndParsesAnswer() {
        stubAnswer("end_turn", "{\\\"questions\\\":[{\\\"text\\\":\\\"Что выполняет JVM?\\\","
                + "\\\"sourceFragment\\\":\\\"JVM выполняет байткод.\\\"}]}");

        var questions = provider.generateQuestions("JVM выполняет байткод.");

        assertThat(questions).containsExactly(new GeneratedQuestion("Что выполняет JVM?", "JVM выполняет байткод."));
        ANTHROPIC.verify(postRequestedFor(urlEqualTo("/v1/messages"))
                .withHeader("x-api-key", equalTo("test-key"))
                .withHeader("anthropic-beta", equalTo("server-side-fallback-2026-07-01"))
                .withRequestBody(matchingJsonPath("$.model", equalTo("claude-opus-5")))
                .withRequestBody(matchingJsonPath("$.fallbacks", equalTo("default")))
                .withRequestBody(matchingJsonPath("$.output_config.effort", equalTo("medium")))
                .withRequestBody(matchingJsonPath("$.output_config.format.type", equalTo("json_schema")))
                .withRequestBody(matchingJsonPath("$.system", containing(AiFormatRules.QUESTIONS)))
                .withRequestBody(matchingJsonPath("$.messages[0].content",
                        equalTo("<material>\nJVM выполняет байткод.\n</material>"))));
    }

    @Test
    @DisplayName("Карточка запрашивается с правилами оформления и разбирается из JSON-ответа модели")
    void generateCard_parsesAnswer() {
        stubAnswer("end_turn", "{\\\"front\\\":\\\"Что такое JVM?\\\",\\\"back\\\":\\\"Виртуальная машина Java.\\\"}");

        var card = provider.generateCard("Что такое JVM?", "JVM — виртуальная машина Java.");

        assertThat(card).isEqualTo(new GeneratedCard("Что такое JVM?", "Виртуальная машина Java."));
        ANTHROPIC.verify(postRequestedFor(urlEqualTo("/v1/messages"))
                .withRequestBody(matchingJsonPath("$.system", containing(AiFormatRules.CARD))));
    }

    @Test
    @DisplayName("Слова запрашиваются с общими правилами, инструкцией и текстом и разбираются в GeneratedWord")
    void generateVocabulary_sendsRulesAndParsesAnswer() {
        // given
        stubAnswer("end_turn", "{\\\"cards\\\":[{\\\"word\\\":\\\"contract\\\","
                + "\\\"translation\\\":\\\"договор\\\",\\\"definition\\\":\\\"A formal agreement.\\\","
                + "\\\"example\\\":\\\"We signed a contract.\\\",\\\"answer\\\":\\\"contract\\\","
                + "\\\"exampleTranslation\\\":\\\"Мы подписали договор.\\\"}]}");

        // when
        var words = provider.generateVocabulary("коллокации", "We signed a contract.");

        // then
        assertThat(words).containsExactly(new GeneratedWord("contract", "договор", "A formal agreement.",
                "We signed a contract.", "contract", "Мы подписали договор."));
        ANTHROPIC.verify(postRequestedFor(urlEqualTo("/v1/messages"))
                .withRequestBody(matchingJsonPath("$.system", equalTo(VocabularyRules.RULES)))
                .withRequestBody(matchingJsonPath("$.messages[0].content",
                        equalTo(VocabularyRules.message("коллокации", "We signed a contract.")))));
    }

    @Test
    @DisplayName("Отказ модели и обрезанный ответ превращаются в AiGenerationException")
    void refusalAndMaxTokens_areErrors() {
        stubAnswer("refusal", "{}");
        assertThatThrownBy(() -> provider.generateCard("Вопрос?", "Материал."))
                .isInstanceOf(AiGenerationException.class).hasMessageContaining("отказалась");

        stubAnswer("max_tokens", "{\\\"front\\\":\\\"Вопрос?\\\"");
        assertThatThrownBy(() -> provider.generateCard("Вопрос?", "Материал."))
                .isInstanceOf(AiGenerationException.class).hasMessageContaining("обрезан");
    }

    @Test
    @DisplayName("Ошибка HTTP Anthropic API превращается в AiGenerationException")
    void httpError_isAiGenerationException() {
        ANTHROPIC.stubFor(post(urlEqualTo("/v1/messages")).willReturn(aResponse().withStatus(529)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":\"error\",\"error\":{\"type\":\"overloaded_error\","
                        + "\"message\":\"Overloaded\"}}")));

        assertThatThrownBy(() -> provider.generateQuestions("Материал."))
                .isInstanceOf(AiGenerationException.class).hasMessageContaining("Anthropic API");
    }

    /**
     * Ответ Messages API с одним текстовым блоком.
     *
     * @param stopReason причина остановки
     * @param jsonText   текст блока, уже экранированный для вставки в JSON-строку
     */
    private static void stubAnswer(String stopReason, String jsonText) {
        ANTHROPIC.stubFor(post(urlEqualTo("/v1/messages")).willReturn(okJson("""
                {"id":"msg_test","type":"message","role":"assistant","model":"claude-opus-5",
                 "content":[{"type":"text","text":"%s"}],
                 "stop_reason":"%s","stop_sequence":null,
                 "usage":{"input_tokens":12,"output_tokens":34}}""".formatted(jsonText, stopReason))));
    }
}
