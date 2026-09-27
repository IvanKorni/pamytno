package net.pamytno.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.support.CapturedEvents;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code topic}: сборка единого текста и событие {@link TopicContentPrepared}.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TopicContentModuleTest {

    private final MockMvc mockMvc;
    private final TopicTestApi api;
    private final CapturedEvents events;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     * @param events       записанные события
     */
    TopicContentModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, CapturedEvents events) {
        this.mockMvc = mockMvc;
        this.api = new TopicTestApi(mockMvc, objectMapper);
        this.events = events;
    }

    @Test
    @DisplayName("Пока нет готовых источников, единого текста нет — 404")
    void content_isAbsent_beforeAnySourceProcessed() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Пустая тема");

        mockMvc.perform(get("/api/topics/{id}/content", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TOPIC_CONTENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("Готовый источник публикует TopicContentPrepared с версией 1 и текстом")
    void processedSource_publishesTopicContentPrepared() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "JVM");

        api.addTextSource(userId, topicId, "JVM выполняет байткод");

        var event = events.await(TopicContentPrepared.class, prepared -> prepared.topicId().equals(topicId));
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.version()).isEqualTo(1);
        assertThat(event.content()).isEqualTo("JVM выполняет байткод");
    }

    @Test
    @DisplayName("Единый текст объединяет источники по порядку, удаление источника даёт новую версию")
    void content_joinsSourcesAndIsRebuiltAfterDeletion() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Spring");
        var first = api.addTextSource(userId, topicId, "Первый");
        api.awaitProcessed(userId, first);
        var second = api.addTextSource(userId, topicId, "Второй");
        api.awaitProcessed(userId, second);

        var content = api.getJson(userId, "/api/topics/{id}/content", topicId);
        assertThat(content.get("content").asText()).isEqualTo("Первый\n\nВторой");
        assertThat(content.get("version").asInt()).isEqualTo(2);

        mockMvc.perform(delete("/api/sources/{id}", first).with(TestJwt.user(userId)))
                .andExpect(status().isNoContent());
        var rebuilt = api.getJson(userId, "/api/topics/{id}/content", topicId);
        assertThat(rebuilt.get("content").asText()).isEqualTo("Второй");
        assertThat(rebuilt.get("version").asInt()).isEqualTo(3);
    }
}
