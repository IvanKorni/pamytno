package net.pamytno.deck;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.deck.repository.QuestionRepository;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code deck}: генерация вопросов через HTTP с заглушкой AI.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class QuestionGenerationModuleTest {

    private final MockMvc mockMvc;
    private final DeckTestApi api;
    private final QuestionRepository questionRepository;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc            MockMvc
     * @param objectMapper       JSON-маппер
     * @param materials          сервис фрагментов
     * @param questionRepository хранилище вопросов
     */
    QuestionGenerationModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials,
                                 QuestionRepository questionRepository) {
        this.mockMvc = mockMvc;
        this.api = new DeckTestApi(mockMvc, objectMapper, materials);
        this.questionRepository = questionRepository;
    }

    @Test
    @DisplayName("Генерация отвечает 202, задача завершается READY с вопросом на каждое предложение")
    void generateQuestions_createsQuestionsAsynchronously(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        api.prepareContent(scenario, topicId, userId, "JVM выполняет байткод. GC освобождает память.");

        var jobId = api.idOf(api.generateQuestions(userId, topicId)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("QUESTIONS"))
                .andExpect(jsonPath("$.status").value("PROCESSING")));

        var job = api.awaitJob(userId, jobId);
        assertThat(job.get("status").asText()).isEqualTo("READY");
        assertThat(job.get("itemsCreated").asInt()).isEqualTo(2);
        assertThat(questionRepository.findAllByTopicIdAndUserIdOrderBySeqAsc(topicId, userId))
                .allSatisfy(question -> assertThat(question.getChunkId()).isNotNull());
    }

    @Test
    @DisplayName("Без текста темы генерация отвечает 409 TOPIC_CONTENT_NOT_READY")
    void generateQuestions_rejects_whenNoContent() throws Exception {
        api.generateQuestions(UUID.randomUUID(), UUID.randomUUID())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TOPIC_CONTENT_NOT_READY"));
    }

    @Test
    @DisplayName("Чужая задача генерации не видна — 404")
    void generationJob_isInvisibleToOtherUser(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        api.prepareContent(scenario, topicId, userId, "Текст.");
        var jobId = api.idOf(api.generateQuestions(userId, topicId));

        mockMvc.perform(get("/api/generation-jobs/{id}", jobId).with(TestJwt.user(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GENERATION_JOB_NOT_FOUND"));
    }
}
