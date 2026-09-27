package net.pamytno.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.learning.domain.ReviewSchedule;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code learning}: прогресс темы и dashboard через HTTP.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProgressModuleTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CardProgressRepository progressRepository;

    /**
     * Создаёт тест.
     *
     * @param mockMvc            MockMvc
     * @param objectMapper       JSON-маппер
     * @param progressRepository хранилище прогресса
     */
    ProgressModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, CardProgressRepository progressRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.progressRepository = progressRepository;
    }

    @Test
    @DisplayName("Прогресс темы: новые, на изучении, изученные, к повторению и процент изученных")
    void topicProgress_countsCardsByState(Scenario scenario) throws Exception {
        var userId = UUID.randomUUID();
        var topicId = UUID.randomUUID();
        var mastered = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое JVM?");
        var learning = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое GC?");
        LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое JIT?");
        rememberTimes(userId, mastered, ReviewSchedule.MASTERED_STAGE);
        rememberTimes(userId, learning, 1);

        mockMvc.perform(get("/api/topics/{id}/progress", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards").value(3))
                .andExpect(jsonPath("$.newCards").value(1))
                .andExpect(jsonPath("$.learningCards").value(1))
                .andExpect(jsonPath("$.masteredCards").value(1))
                .andExpect(jsonPath("$.dueCards").value(1))
                .andExpect(jsonPath("$.dueToday").value(1))
                .andExpect(jsonPath("$.progress").value(33.3));
    }

    @Test
    @DisplayName("Dashboard суммирует темы пользователя и не видит чужие карточки")
    void dashboard_sumsOwnTopics(Scenario scenario) throws Exception {
        var userId = UUID.randomUUID();
        var firstTopic = UUID.randomUUID();
        var card = LearningTestEvents.cardCreated(scenario, progressRepository, firstTopic, userId, "Вопрос 1?");
        LearningTestEvents.cardCreated(scenario, progressRepository, UUID.randomUUID(), userId, "Вопрос 2?");
        LearningTestEvents.cardCreated(scenario, progressRepository, firstTopic, UUID.randomUUID(), "Чужой?");
        rememberTimes(userId, card, ReviewSchedule.MASTERED_STAGE);

        mockMvc.perform(get("/api/dashboard").with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards").value(2))
                .andExpect(jsonPath("$.masteredCards").value(1))
                .andExpect(jsonPath("$.dueCards").value(1))
                .andExpect(jsonPath("$.progress").value(50.0))
                .andExpect(jsonPath("$.topics.length()").value(2));
    }

    @Test
    @DisplayName("Тема без карточек отдаёт нулевой прогресс")
    void topicProgress_returnsZeros_whenNoCards() throws Exception {
        mockMvc.perform(get("/api/topics/{id}/progress", UUID.randomUUID()).with(TestJwt.user(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards").value(0))
                .andExpect(jsonPath("$.progress").value(0.0));
    }

    /**
     * Отвечает «вспомнил» по карточке несколько раз подряд.
     *
     * @param userId пользователь
     * @param cardId карточка
     * @param times  сколько раз
     * @throws Exception при ошибке MockMvc
     */
    private void rememberTimes(UUID userId, UUID cardId, int times) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("result", "REMEMBER"));
        for (var i = 0; i < times; i++) {
            mockMvc.perform(post("/api/cards/{id}/review", cardId).with(TestJwt.user(userId))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk());
        }
    }
}
