package net.pamytno.deck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.TestJwt;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Клиент REST API модуля {@code deck} для модульных тестов. Текст темы подаётся событием
 * {@link TopicContentPrepared}, как это сделал бы модуль {@code topic}.
 *
 * @param mockMvc      MockMvc контекста модуля
 * @param objectMapper JSON-маппер
 * @param materials    сервис фрагментов — чтобы дождаться приёма текста
 */
record DeckTestApi(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials) {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    /**
     * Публикует текст темы и ждёт, пока генерация вопросов станет возможной.
     *
     * @param scenario сценарий Spring Modulith
     * @param topicId  тема
     * @param userId   владелец
     * @param content  единый текст темы
     */
    void prepareContent(Scenario scenario, UUID topicId, UUID userId, String content) {
        scenario.publish(new TopicContentPrepared(topicId, userId, 1, content))
                .andWaitForStateChange(() -> materials.latestChunks(topicId, userId), chunks -> !chunks.isEmpty());
    }

    /**
     * Запускает генерацию вопросов.
     *
     * @param userId  владелец
     * @param topicId тема
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    ResultActions generateQuestions(UUID userId, UUID topicId) throws Exception {
        return mockMvc.perform(post("/api/topics/{id}/questions/generate", topicId).with(TestJwt.user(userId)));
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
     * Достаёт идентификатор из JSON ответа.
     *
     * @param result результат запроса
     * @return значение поля {@code id}
     * @throws Exception при ошибке разбора
     */
    UUID idOf(ResultActions result) throws Exception {
        return UUID.fromString(objectMapper.readTree(result.andReturn().getResponse().getContentAsString())
                .get("id").asText());
    }

    /**
     * Ждёт завершения задачи генерации.
     *
     * @param userId владелец
     * @param jobId  задача
     * @return JSON задачи в статусе READY или ERROR
     */
    JsonNode awaitJob(UUID userId, UUID jobId) {
        return await().atMost(TIMEOUT).until(() -> getJson(userId, "/api/generation-jobs/{id}", jobId),
                job -> !"PROCESSING".equals(job.get("status").asText()));
    }

    /**
     * Готовит тему с текстом и сгенерированными заглушкой вопросами.
     *
     * @param scenario сценарий Spring Modulith
     * @param topicId  тема
     * @param userId   владелец
     * @param content  единый текст темы; по вопросу на предложение
     * @return вопросы темы
     * @throws Exception при ошибке MockMvc
     */
    JsonNode prepareQuestions(Scenario scenario, UUID topicId, UUID userId, String content) throws Exception {
        prepareContent(scenario, topicId, userId, content);
        awaitJob(userId, idOf(generateQuestions(userId, topicId)));
        return getJson(userId, "/api/topics/{id}/questions", topicId);
    }

    /**
     * Готовит тему с карточками: вопросы от заглушки, все одобрены, карточки созданы.
     *
     * @param scenario сценарий Spring Modulith
     * @param topicId  тема
     * @param userId   владелец
     * @param content  единый текст темы; по карточке на предложение
     * @return карточки темы
     * @throws Exception при ошибке MockMvc
     */
    JsonNode prepareCards(Scenario scenario, UUID topicId, UUID userId, String content) throws Exception {
        var questions = prepareQuestions(scenario, topicId, userId, content);
        var questionIds = StreamSupport.stream(questions.spliterator(), false)
                .map(question -> question.get("id").asText())
                .toList();
        mockMvc.perform(post("/api/questions/decisions").with(TestJwt.user(userId)).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("questionIds", questionIds,
                                "decision", "APPROVE"))))
                .andExpect(status().isOk());
        var job = mockMvc.perform(post("/api/topics/{id}/cards/generate", topicId).with(TestJwt.user(userId)));
        awaitJob(userId, idOf(job));
        return getJson(userId, "/api/topics/{id}/cards", topicId);
    }
}
