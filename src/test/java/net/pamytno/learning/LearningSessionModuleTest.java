package net.pamytno.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code learning}: учебные сессии через HTTP.
 */
@ModuleTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LearningSessionModuleTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CardProgressRepository progressRepository;

    @Test
    @DisplayName("Сессия стартует с числом карточек к повторению и завершается")
    void session_startsWithDueCountAndCompletes(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое JVM?");
        LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Что такое GC?");

        var body = mockMvc.perform(post("/api/topics/{id}/learning-sessions", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardsTotal").value(2))
                .andExpect(jsonPath("$.completedAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        var sessionId = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(post("/api/learning-sessions/{id}/complete", sessionId).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Чужая сессия не находится — 404")
    void session_isInvisibleToOtherUser() throws Exception {
        var body = mockMvc.perform(post("/api/topics/{id}/learning-sessions", UUID.randomUUID())
                        .with(TestJwt.user(UUID.randomUUID())))
                .andReturn().getResponse().getContentAsString();
        var sessionId = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(get("/api/learning-sessions/{id}", sessionId).with(TestJwt.user(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LEARNING_SESSION_NOT_FOUND"));
    }
}
