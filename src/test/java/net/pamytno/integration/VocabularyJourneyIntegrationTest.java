package net.pamytno.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Сквозной сценарий карточек слов по настоящему HTTP: новая тема без материалов → вставленный список слов →
 * карточки слов → они сразу к повторению и в прогрессе темы.
 */
@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class VocabularyJourneyIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final JourneyClient client;

    /**
     * Создаёт тест.
     *
     * @param port         порт приложения
     * @param objectMapper JSON-маппер
     */
    VocabularyJourneyIntegrationTest(@LocalServerPort int port, ObjectMapper objectMapper) {
        this.client = new JourneyClient(port, objectMapper);
    }

    @BeforeEach
    void signUp() {
        client.signUp("words-" + UUID.randomUUID() + "@example.com", "correct-horse-battery");
    }

    @Test
    @DisplayName("Список слов в новой теме становится карточками к повторению без вопросов и материалов")
    void vocabulary_createsCardsForLearning() {
        // given
        var topicId = client.post("/api/topics", Map.of("title", "English Unit 5")).text("id");
        var request = Map.of("text", "contract — договор\nreliable — надёжный", "instruction", "");

        // when
        var job = await().atMost(TIMEOUT)
                .until(() -> client.post("/api/topics/{id}/vocabulary/generate", request, topicId),
                        response -> response.status() == 202);

        // then
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(client.get("/api/generation-jobs/{id}",
                job.text("id")).text("status")).isEqualTo("READY"));
        var cards = client.get("/api/topics/{id}/cards", topicId).body();
        assertThat(cards).hasSize(2);
        assertThat(cards.get(1).get("back").asText()).startsWith("**reliable**\n");
        await().atMost(TIMEOUT).untilAsserted(() ->
                assertThat(client.get("/api/topics/{id}/reviews/due", topicId).body()).hasSize(2));
        assertThat(client.get("/api/topics/{id}/questions", topicId).body()).isEmpty();
    }
}
