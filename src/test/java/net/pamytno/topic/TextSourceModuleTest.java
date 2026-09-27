package net.pamytno.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code topic}: текстовые источники, асинхронная обработка и статус темы.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TextSourceModuleTest {

    private final MockMvc mockMvc;
    private final TopicTestApi api;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     */
    TextSourceModuleTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.api = new TopicTestApi(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Текст принимается со статусом UPLOADED, обрабатывается и делает тему READY")
    void addText_processesSourceAndMarksTopicReady() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "JVM");

        api.addText(userId, topicId, "TEXT", "JVM   выполняет\n\n байткод")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("UPLOADED"))
                .andExpect(jsonPath("$.type").value("TEXT"));

        var sourceId = UUID.fromString(api.getJson(userId, "/api/topics/{id}/sources", topicId)
                .get(0).get("id").asText());
        assertThat(api.awaitProcessed(userId, sourceId).get("status").asText()).isEqualTo("READY");
        assertThat(api.getJson(userId, "/api/topics/{id}", topicId).get("status").asText()).isEqualTo("READY");
    }

    @Test
    @DisplayName("Исходный текст сохраняется без изменений, извлечённый — очищенным")
    void sourceText_keepsOriginalAndCleanedVersions() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Слова");
        var sourceId = api.addTextSource(userId, topicId, "  cat — кот  \n\n dog — собака ");
        api.awaitProcessed(userId, sourceId);

        var texts = api.getJson(userId, "/api/sources/{id}/text", sourceId);

        assertThat(texts.get("originalText").asText()).isEqualTo("  cat — кот  \n\n dog — собака ");
        assertThat(texts.get("extractedText").asText()).isEqualTo("cat — кот\ndog — собака");
    }

    @Test
    @DisplayName("Текст из одних пробелов даёт ошибку TEXT_EXTRACTION_FAILED и тему в ERROR")
    void addText_marksSourceError_whenNoTextExtracted() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Пустота");
        var sourceId = api.addTextSource(userId, topicId, "   \n   ");

        var source = api.awaitProcessed(userId, sourceId);

        assertThat(source.get("status").asText()).isEqualTo("ERROR");
        assertThat(source.get("errorCode").asText()).isEqualTo("TEXT_EXTRACTION_FAILED");
        assertThat(api.getJson(userId, "/api/topics/{id}", topicId).get("status").asText()).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Список слов принимается как вид WORD_LIST")
    void addWordList_createsWordListSource() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Английский");

        api.addText(userId, topicId, "WORD_LIST", "apple — яблоко")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("WORD_LIST"));
    }

    @Test
    @DisplayName("В чужую тему источник не добавляется, чужой источник не виден")
    void sources_areIsolatedBetweenUsers() throws Exception {
        var owner = UUID.randomUUID();
        var stranger = UUID.randomUUID();
        var topicId = api.createTopic(owner, "Приватная тема");
        var sourceId = api.addTextSource(owner, topicId, "текст");

        api.addText(stranger, topicId, "TEXT", "чужой текст")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TOPIC_NOT_FOUND"));
        mockMvc.perform(delete("/api/sources/{id}", sourceId).with(TestJwt.user(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Удаление единственного источника возвращает тему в DRAFT")
    void deleteSource_recalculatesTopicStatus() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Временная");
        var sourceId = api.addTextSource(userId, topicId, "текст");
        api.awaitProcessed(userId, sourceId);

        mockMvc.perform(delete("/api/sources/{id}", sourceId).with(TestJwt.user(userId)))
                .andExpect(status().isNoContent());

        assertThat(api.getJson(userId, "/api/topics/{id}/sources", topicId)).isEmpty();
        assertThat(api.getJson(userId, "/api/topics/{id}", topicId).get("status").asText()).isEqualTo("DRAFT");
    }
}
