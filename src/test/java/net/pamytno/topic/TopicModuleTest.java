package net.pamytno.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.topic.TopicCreated;
import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code topic}: CRUD тем через HTTP, изоляция пользователей и событие удаления.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TopicModuleTest {

    private final MockMvc mockMvc;
    private final TopicTestApi api;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     */
    TopicModuleTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.api = new TopicTestApi(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Созданная тема в статусе DRAFT видна владельцу в списке и по id")
    void createTopic_returnsDraftTopic() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Spring Security");

        mockMvc.perform(get("/api/topics/{id}", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Security"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
        mockMvc.perform(get("/api/topics").with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(topicId.toString()));
    }

    @Test
    @DisplayName("Создание темы публикует TopicCreated с владельцем")
    void createTopic_publishesTopicCreated(AssertablePublishedEvents events) throws Exception {
        // given
        var userId = UUID.randomUUID();

        // when
        var topicId = api.createTopic(userId, "English Unit 5");

        // then
        assertThat(events).contains(TopicCreated.class)
                .matching(TopicCreated::topicId, topicId)
                .matching(TopicCreated::userId, userId);
    }

    @Test
    @DisplayName("Чужая тема отвечает 404 и не попадает в список")
    void topic_isInvisibleToOtherUser() throws Exception {
        var topicId = api.createTopic(UUID.randomUUID(), "Чужая тема");
        var stranger = UUID.randomUUID();

        mockMvc.perform(get("/api/topics/{id}", topicId).with(TestJwt.user(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TOPIC_NOT_FOUND"));
        mockMvc.perform(get("/api/topics").with(TestJwt.user(stranger)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PATCH меняет только переданные поля")
    void updateTopic_changesOnlyGivenFields() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "JVM");

        mockMvc.perform(patch("/api/topics/{id}", topicId).with(TestJwt.user(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Байткод и сборка мусора\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JVM"))
                .andExpect(jsonPath("$.description").value("Байткод и сборка мусора"));
    }

    @Test
    @DisplayName("Пустое название отклоняется с VALIDATION_FAILED")
    void createTopic_rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/topics").with(TestJwt.user(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("Удаление темы отвечает 204, публикует TopicDeleted, после чего тема не находится")
    void deleteTopic_publishesTopicDeleted(AssertablePublishedEvents events) throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Временная тема");

        mockMvc.perform(delete("/api/topics/{id}", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isNoContent());

        assertThat(events).contains(TopicDeleted.class)
                .matching(TopicDeleted::topicId, topicId)
                .matching(TopicDeleted::userId, userId);
        mockMvc.perform(get("/api/topics/{id}", topicId).with(TestJwt.user(userId)))
                .andExpect(status().isNotFound());
    }
}
