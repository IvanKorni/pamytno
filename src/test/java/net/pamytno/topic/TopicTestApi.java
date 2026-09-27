package net.pamytno.topic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.support.TestJwt;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Клиент REST API модуля {@code topic} для модульных тестов: создаёт темы и источники,
 * ждёт завершения асинхронной обработки.
 *
 * @param mockMvc      MockMvc контекста модуля
 * @param objectMapper JSON-маппер
 */
record TopicTestApi(MockMvc mockMvc, ObjectMapper objectMapper) {

    private static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(15);

    /**
     * Создаёт тему.
     *
     * @param userId владелец
     * @param title  название
     * @return идентификатор темы
     * @throws Exception при ошибке MockMvc
     */
    UUID createTopic(UUID userId, String title) throws Exception {
        var body = mockMvc.perform(post("/api/topics").with(TestJwt.user(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", title))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asText());
    }

    /**
     * Отправляет запрос на добавление текстового источника.
     *
     * @param userId  владелец
     * @param topicId тема
     * @param type    TEXT или WORD_LIST
     * @param text    текст
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    ResultActions addText(UUID userId, UUID topicId, String type, String text) throws Exception {
        return mockMvc.perform(post("/api/topics/{id}/sources/text", topicId).with(TestJwt.user(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("type", type, "text", text))));
    }

    /**
     * Добавляет текстовый источник и возвращает его идентификатор.
     *
     * @param userId  владелец
     * @param topicId тема
     * @param text    текст
     * @return идентификатор источника
     * @throws Exception при ошибке MockMvc
     */
    UUID addTextSource(UUID userId, UUID topicId, String text) throws Exception {
        var body = addText(userId, topicId, "TEXT", text)
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asText());
    }

    /**
     * Читает JSON ответа GET-запроса.
     *
     * @param userId пользователь
     * @param path   путь с плейсхолдерами
     * @param args   значения плейсхолдеров
     * @return JSON ответа
     * @throws Exception при ошибке MockMvc
     */
    JsonNode getJson(UUID userId, String path, Object... args) throws Exception {
        var body = mockMvc.perform(get(path, args).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    /**
     * Ждёт, пока источник выйдет из обработки, и возвращает его.
     *
     * @param userId   владелец
     * @param sourceId источник
     * @return JSON источника в статусе READY или ERROR
     */
    JsonNode awaitProcessed(UUID userId, UUID sourceId) {
        return await().atMost(PROCESSING_TIMEOUT).until(
                () -> getJson(userId, "/api/sources/{id}", sourceId),
                source -> "READY".equals(source.get("status").asText())
                        || "ERROR".equals(source.get("status").asText()));
    }
}
