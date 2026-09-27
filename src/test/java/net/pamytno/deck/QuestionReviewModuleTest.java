package net.pamytno.deck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code deck}: отбор вопросов пользователем через HTTP.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class QuestionReviewModuleTest {

    private static final String CONTENT = "JVM выполняет байткод. GC освобождает память. JIT ускоряет код.";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DeckTestApi api;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     * @param materials    сервис фрагментов
     */
    QuestionReviewModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.api = new DeckTestApi(mockMvc, objectMapper, materials);
    }

    @Test
    @DisplayName("Вопросы одобряются и отклоняются поштучно, список фильтруется по статусу")
    void approveAndReject_changeStatusAndFilter(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var questions = api.prepareQuestions(scenario, topicId, userId, CONTENT);
        assertThat(questions).hasSize(3)
                .allSatisfy(question -> assertThat(question.get("status").asText()).isEqualTo("GENERATED"));

        mockMvc.perform(post("/api/questions/{id}/approve", id(questions, 0)).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(post("/api/questions/{id}/reject", id(questions, 1)).with(TestJwt.user(userId)))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        var approved = api.getJson(userId, "/api/topics/{id}/questions?status=APPROVED", topicId);
        assertThat(approved).singleElement()
                .satisfies(question -> assertThat(question.get("id").asText()).isEqualTo(id(questions, 0)));
    }

    @Test
    @DisplayName("Массовое решение меняет все вопросы сразу")
    void decideQuestions_appliesToAll(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var questions = api.prepareQuestions(scenario, topicId, userId, CONTENT);
        var body = Map.of("questionIds", List.of(id(questions, 0), id(questions, 2)), "decision", "APPROVE");

        mockMvc.perform(post("/api/questions/decisions").with(TestJwt.user(userId))
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].status", everyItem(is("APPROVED"))));
    }

    @Test
    @DisplayName("Формулировка вопроса меняется, пустая отклоняется")
    void updateQuestion_changesText(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var questionId = id(api.prepareQuestions(scenario, topicId, userId, CONTENT), 0);

        mockMvc.perform(patch("/api/questions/{id}", questionId).with(TestJwt.user(userId))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Что выполняет JVM?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Что выполняет JVM?"));
        mockMvc.perform(patch("/api/questions/{id}", questionId).with(TestJwt.user(userId))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Чужой вопрос не найти и не изменить — 404")
    void questions_areIsolatedBetweenUsers(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var owner = UUID.randomUUID();
        var questionId = id(api.prepareQuestions(scenario, topicId, owner, CONTENT), 0);
        var stranger = UUID.randomUUID();

        mockMvc.perform(post("/api/questions/{id}/approve", questionId).with(TestJwt.user(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUESTION_NOT_FOUND"));
        assertThat(api.getJson(stranger, "/api/topics/{id}/questions", topicId)).isEmpty();
    }

    /**
     * Идентификатор вопроса из списка.
     *
     * @param questions JSON-массив вопросов
     * @param index     позиция
     * @return идентификатор
     */
    private static String id(JsonNode questions, int index) {
        return questions.get(index).get("id").asText();
    }
}
