package net.pamytno.deck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.CapturedEvents;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code deck}: генерация карточек по одобренным вопросам и событие {@link FlashcardCreated}.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CardGenerationModuleTest {

    private final MockMvc mockMvc;
    private final DeckTestApi api;
    private final CapturedEvents events;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     * @param materials    сервис фрагментов
     * @param events       записанные события
     */
    CardGenerationModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials,
                             CapturedEvents events) {
        this.mockMvc = mockMvc;
        this.api = new DeckTestApi(mockMvc, objectMapper, materials);
        this.events = events;
    }

    @Test
    @DisplayName("Карточки создаются только по одобренным вопросам и публикуют FlashcardCreated")
    void generateCards_createsCardsForApprovedQuestions(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var questions = api.prepareQuestions(scenario, topicId, userId, "JVM выполняет байткод. GC чистит память.");
        approve(userId, questions.get(0));

        var jobId = api.idOf(generateCards(userId, topicId)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("CARDS")));

        assertThat(api.awaitJob(userId, jobId).get("itemsCreated").asInt()).isEqualTo(1);
        var event = events.await(FlashcardCreated.class, created -> created.topicId().equals(topicId));
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.back()).startsWith("JVM выполняет байткод.");
        assertThat(api.getJson(userId, "/api/topics/{id}/questions?status=CARD_CREATED", topicId)).hasSize(1);
    }

    @Test
    @DisplayName("Без одобренных вопросов генерация карточек отвечает 409 NO_APPROVED_QUESTIONS")
    void generateCards_rejects_whenNothingApproved(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        api.prepareQuestions(scenario, topicId, userId, "Текст.");

        generateCards(userId, topicId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_APPROVED_QUESTIONS"));
    }

    /**
     * Одобряет вопрос.
     *
     * @param userId   владелец
     * @param question JSON вопроса
     * @throws Exception при ошибке MockMvc
     */
    private void approve(UUID userId, JsonNode question) throws Exception {
        mockMvc.perform(post("/api/questions/{id}/approve", question.get("id").asText()).with(TestJwt.user(userId)))
                .andExpect(status().isOk());
    }

    /**
     * Запускает генерацию карточек.
     *
     * @param userId  владелец
     * @param topicId тема
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions generateCards(UUID userId, UUID topicId) throws Exception {
        return mockMvc.perform(post("/api/topics/{id}/cards/generate", topicId).with(TestJwt.user(userId)));
    }
}
