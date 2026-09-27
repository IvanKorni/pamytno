package net.pamytno.learning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestClock;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code learning}: очередь повторения и ответы по карточкам через HTTP.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ReviewModuleTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CardProgressRepository progressRepository;
    private final TestClock clock;

    /**
     * Создаёт тест.
     *
     * @param mockMvc            MockMvc
     * @param objectMapper       JSON-маппер
     * @param progressRepository хранилище прогресса
     * @param clock              управляемые часы
     */
    ReviewModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, CardProgressRepository progressRepository,
                     TestClock clock) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.progressRepository = progressRepository;
        this.clock = clock;
    }

    @AfterEach
    void resetClock() {
        clock.reset();
    }

    @Test
    @DisplayName("REMEMBER убирает карточку из очереди на день, через день она снова к повторению")
    void remember_schedulesNextReviewInOneDay(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var cardId = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое JVM?");
        LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое GC?");
        assertThat(due(userId, topicId)).hasSize(2);

        review(userId, cardId, "REMEMBER", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stage").value(1))
                .andExpect(jsonPath("$.mastered").value(false))
                .andExpect(jsonPath("$.returnToSession").value(false));

        assertThat(due(userId, topicId)).hasSize(1);
        clock.advance(Duration.ofDays(1).plusMinutes(1));
        assertThat(due(userId, topicId)).hasSize(2);
    }

    @Test
    @DisplayName("FORGOT оставляет карточку в очереди и просит вернуть её в сессию; сессия считает ответы")
    void forgot_keepsCardDueAndCountsInSession(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var cardId = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое JIT?");
        var sessionId = startSession(userId, topicId);

        review(userId, cardId, "FORGOT", sessionId)
                .andExpect(jsonPath("$.stage").value(0))
                .andExpect(jsonPath("$.returnToSession").value(true));
        review(userId, cardId, "CONTINUE", sessionId).andExpect(jsonPath("$.stage").value(0));

        assertThat(due(userId, topicId)).hasSize(1);
        mockMvc.perform(get("/api/learning-sessions/{id}", sessionId).with(TestJwt.user(userId)))
                .andExpect(jsonPath("$.cardsForgotten").value(1))
                .andExpect(jsonPath("$.cardsRemembered").value(0));
    }

    @Test
    @DisplayName("Ответ в завершённой сессии отклоняется с LEARNING_SESSION_COMPLETED")
    void review_rejects_whenSessionCompleted(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var cardId = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Вопрос?");
        var sessionId = startSession(userId, topicId);
        mockMvc.perform(post("/api/learning-sessions/{id}/complete", sessionId).with(TestJwt.user(userId)));

        review(userId, cardId, "REMEMBER", sessionId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LEARNING_SESSION_COMPLETED"));
    }

    @Test
    @DisplayName("Чужую карточку нельзя повторить, чужая очередь пуста")
    void reviews_areIsolatedBetweenUsers(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var owner = UUID.randomUUID();
        var cardId = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, owner, "Вопрос?");
        var stranger = UUID.randomUUID();

        review(stranger, cardId, "REMEMBER", null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CARD_PROGRESS_NOT_FOUND"));
        assertThat(due(stranger, topicId)).isEmpty();
    }

    /**
     * Очередь повторения темы.
     *
     * @param userId  пользователь
     * @param topicId тема
     * @return JSON-массив карточек
     * @throws Exception при ошибке MockMvc
     */
    private JsonNode due(UUID userId, UUID topicId) throws Exception {
        var body = mockMvc.perform(get("/api/topics/{id}/reviews/due", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    /**
     * Отправляет ответ по карточке.
     *
     * @param userId    пользователь
     * @param cardId    карточка
     * @param result    ответ
     * @param sessionId сессия или {@code null}
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions review(UUID userId, UUID cardId, String result, String sessionId) throws Exception {
        var body = new HashMap<String, Object>();
        body.put("result", result);
        body.put("sessionId", sessionId);
        return mockMvc.perform(post("/api/cards/{id}/review", cardId).with(TestJwt.user(userId))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Начинает учебную сессию.
     *
     * @param userId  пользователь
     * @param topicId тема
     * @return идентификатор сессии
     * @throws Exception при ошибке MockMvc
     */
    private String startSession(UUID userId, UUID topicId) throws Exception {
        var body = mockMvc.perform(post("/api/topics/{id}/learning-sessions", topicId).with(TestJwt.user(userId)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }
}
