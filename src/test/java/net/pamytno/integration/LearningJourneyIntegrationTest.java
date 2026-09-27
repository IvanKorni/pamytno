package net.pamytno.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.support.IntegrationTest;
import net.pamytno.support.TestClock;
import net.pamytno.support.TestWireMock;
import net.pamytno.topic.PdfFixtures;
import net.pamytno.topic.YoutubeStubs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

/**
 * Сквозной сценарий из ТЗ §28 по настоящему HTTP через все модули: тема → PDF, YouTube и текст →
 * единый текст → вопросы → одобрение → карточки → обучение → «Вспомнил» / «Не вспомнил» →
 * правильная дата следующего повторения → через день карточка снова к повторению.
 */
@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LearningJourneyIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String TEXT = "JVM выполняет байткод. Сборщик мусора освобождает память.";
    private static final String PDF_TEXT = "JIT compiles hot methods into machine code.";
    private static final String TRANSCRIPT = "Потоки выполняются параллельно";

    private final JourneyClient client;
    private final TestClock clock;
    private final YoutubeStubs youtube = new YoutubeStubs(TestWireMock.started());

    /**
     * Создаёт тест.
     *
     * @param port         порт приложения
     * @param objectMapper JSON-маппер
     * @param clock        управляемые часы
     */
    LearningJourneyIntegrationTest(@LocalServerPort int port, ObjectMapper objectMapper, TestClock clock) {
        this.client = new JourneyClient(port, objectMapper);
        this.clock = clock;
    }

    @BeforeEach
    void signUp() {
        client.signUp("journey-" + UUID.randomUUID() + "@example.com", "correct-horse-battery");
    }

    @AfterEach
    void resetClock() {
        clock.reset();
    }

    @Test
    @DisplayName("Полный путь: источники → вопросы → карточки → ответы → через день карточка снова к повторению")
    void journey_schedulesCardsAfterAnswers() {
        // given
        var topicId = createTopicWithSources();
        var cardIds = createCards(topicId, approveGeneratedQuestions(topicId));
        var sessionId = client.post("/api/topics/{id}/learning-sessions", null, topicId).text("id");

        // when
        var remembered = review(cardIds.get(0), "REMEMBER", sessionId);
        var forgotten = review(cardIds.get(1), "FORGOT", sessionId);

        // then
        var expectedNext = clock.instant().plus(Duration.ofDays(1));
        assertThat(Instant.parse(remembered.get("nextReviewAt").asText()))
                .isCloseTo(expectedNext, within(1, ChronoUnit.MINUTES));
        assertThat(forgotten.get("returnToSession").asBoolean()).isTrue();
        assertThat(due(topicId)).contains(cardIds.get(1)).doesNotContain(cardIds.get(0));
        var session = client.post("/api/learning-sessions/{id}/complete", null, sessionId).body();
        assertThat(session.get("cardsRemembered").asInt()).isEqualTo(1);
        assertThat(session.get("cardsForgotten").asInt()).isEqualTo(1);

        clock.advance(Duration.ofDays(1).plusMinutes(1));
        assertThat(due(topicId)).contains(cardIds.get(0), cardIds.get(1));
    }

    @Test
    @DisplayName("Удаление темы убирает её карточки из повторения и из прогресса")
    void deleteTopic_removesCardsFromLearning() {
        // given
        var topicId = createTopicWithSources();
        createCards(topicId, approveGeneratedQuestions(topicId));

        // when
        assertThat(client.delete("/api/topics/{id}", topicId).status()).isEqualTo(204);

        // then
        await().atMost(TIMEOUT).untilAsserted(() ->
                assertThat(client.get("/api/dashboard").body().get("totalCards").asInt()).isZero());
        assertThat(client.get("/api/topics/{id}/cards", topicId).body()).isEmpty();
    }

    /**
     * Создаёт тему с текстом, PDF и YouTube и ждёт единый текст из всех трёх источников.
     *
     * @return идентификатор темы
     */
    private String createTopicWithSources() {
        var topicId = client.post("/api/topics", Map.of("title", "Java")).text("id");
        client.post("/api/topics/{id}/sources/text", Map.of("text", TEXT), topicId);
        client.upload("/api/topics/{id}/sources/pdf", "jit.pdf", PdfFixtures.withText(PDF_TEXT), topicId);
        var videoId = YoutubeStubs.randomVideoId();
        youtube.videoWithCaptions(videoId, "ru", "<transcript><text>" + TRANSCRIPT + "</text></transcript>");
        client.post("/api/topics/{id}/sources/youtube",
                Map.of("url", "https://www.youtube.com/watch?v=" + videoId), topicId);
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(client.get("/api/topics/{id}/content", topicId)
                .body().path("content").asText()).contains("байткод", "JIT compiles", TRANSCRIPT));
        return topicId;
    }

    /**
     * Генерирует вопросы по единому тексту и одобряет все.
     *
     * @param topicId тема
     * @return сколько вопросов одобрено
     */
    private int approveGeneratedQuestions(String topicId) {
        var job = await().atMost(TIMEOUT)
                .until(() -> client.post("/api/topics/{id}/questions/generate", null, topicId),
                        response -> response.status() == 202);
        awaitJobReady(job.text("id"));
        var questionIds = ids(client.get("/api/topics/{id}/questions", topicId).body(), "id");
        client.post("/api/questions/decisions", Map.of("questionIds", questionIds, "decision", "APPROVE"));
        return questionIds.size();
    }

    /**
     * Создаёт карточки по одобренным вопросам и ждёт, пока они попадут в очередь повторения.
     *
     * @param topicId  тема
     * @param approved сколько вопросов одобрено
     * @return идентификаторы карточек
     */
    private List<String> createCards(String topicId, int approved) {
        var job = client.post("/api/topics/{id}/cards/generate", null, topicId);
        assertThat(job.status()).isEqualTo(202);
        awaitJobReady(job.text("id"));
        var cardIds = ids(client.get("/api/topics/{id}/cards", topicId).body(), "id");
        assertThat(cardIds).hasSize(approved).hasSizeGreaterThanOrEqualTo(2);
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(due(topicId)).hasSameSizeAs(cardIds));
        return cardIds;
    }

    /**
     * Ждёт успешного завершения задачи генерации.
     *
     * @param jobId задача
     */
    private void awaitJobReady(String jobId) {
        await().atMost(TIMEOUT).untilAsserted(() ->
                assertThat(client.get("/api/generation-jobs/{id}", jobId).text("status")).isEqualTo("READY"));
    }

    /**
     * Отвечает по карточке.
     *
     * @param cardId    карточка
     * @param result    ответ
     * @param sessionId учебная сессия
     * @return новое состояние карточки
     */
    private JsonNode review(String cardId, String result, String sessionId) {
        var response = client.post("/api/cards/{id}/review", Map.of("result", result, "sessionId", sessionId),
                cardId);
        assertThat(response.status()).isEqualTo(200);
        return response.body();
    }

    /**
     * Карточки темы, которые пора повторить.
     *
     * @param topicId тема
     * @return идентификаторы карточек
     */
    private List<String> due(String topicId) {
        return ids(client.get("/api/topics/{id}/reviews/due", topicId).body(), "cardId");
    }

    /**
     * Значения поля из JSON-массива.
     *
     * @param array JSON-массив
     * @param field поле
     * @return значения
     */
    private static List<String> ids(JsonNode array, String field) {
        return StreamSupport.stream(array.spliterator(), false).map(node -> node.get(field).asText()).toList();
    }
}
