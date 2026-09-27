package net.pamytno.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import net.pamytno.support.TestWireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code topic}: видео YouTube с подменённым через WireMock YouTube.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class YoutubeSourceModuleTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TopicTestApi api;
    private final YoutubeStubs youtube = new YoutubeStubs(TestWireMock.started());

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     */
    YoutubeSourceModuleTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.api = new TopicTestApi(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Субтитры видео становятся извлечённым текстом, исходная ссылка сохраняется")
    void addYoutube_extractsTranscript() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Лекция");
        var videoId = YoutubeStubs.randomVideoId();
        youtube.videoWithCaptions(videoId, "ru", "<transcript><text>Первая фраза</text>"
                + "<text>Вторая фраза</text></transcript>");
        var url = "https://youtu.be/" + videoId;

        var sourceId = sourceIdOf(addYoutube(userId, topicId, url)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("YOUTUBE"))
                .andExpect(jsonPath("$.originalUrl").value(url)));

        assertThat(api.awaitProcessed(userId, sourceId).get("status").asText()).isEqualTo("READY");
        assertThat(api.getJson(userId, "/api/sources/{id}/text", sourceId).get("extractedText").asText())
                .isEqualTo("Первая фраза\nВторая фраза");
    }

    @Test
    @DisplayName("Видео без субтитров получает ошибку TRANSCRIPT_UNAVAILABLE")
    void addYoutube_marksError_whenNoCaptions() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Без субтитров");
        var videoId = YoutubeStubs.randomVideoId();
        youtube.videoWithoutCaptions(videoId);

        var sourceId = sourceIdOf(addYoutube(userId, topicId, "https://www.youtube.com/watch?v=" + videoId));

        var source = api.awaitProcessed(userId, sourceId);
        assertThat(source.get("status").asText()).isEqualTo("ERROR");
        assertThat(source.get("errorCode").asText()).isEqualTo("TRANSCRIPT_UNAVAILABLE");
    }

    @Test
    @DisplayName("Ссылка не на YouTube отклоняется с INVALID_YOUTUBE_URL")
    void addYoutube_rejectsInvalidUrl() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Не YouTube");

        addYoutube(userId, topicId, "https://vimeo.com/123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_YOUTUBE_URL"));
    }

    /**
     * Отправляет запрос на добавление видео.
     *
     * @param userId  владелец
     * @param topicId тема
     * @param url     ссылка
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions addYoutube(UUID userId, UUID topicId, String url) throws Exception {
        return mockMvc.perform(post("/api/topics/{id}/sources/youtube", topicId).with(TestJwt.user(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("url", url))));
    }

    /**
     * Достаёт идентификатор источника из ответа 202.
     *
     * @param result результат запроса
     * @return идентификатор источника
     * @throws Exception при ошибке разбора ответа
     */
    private UUID sourceIdOf(ResultActions result) throws Exception {
        var body = result.andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asText());
    }
}
