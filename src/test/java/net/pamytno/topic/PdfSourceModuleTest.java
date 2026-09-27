package net.pamytno.topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import net.pamytno.support.TestStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code topic}: загрузка PDF, извлечение текста и очистка хранилища.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PdfSourceModuleTest {

    private final MockMvc mockMvc;
    private final TopicTestApi api;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     */
    PdfSourceModuleTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.api = new TopicTestApi(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("PDF с текстом обрабатывается: источник READY, текст попадает в единый текст темы")
    void uploadPdf_extractsText() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "JVM");

        var sourceId = uploadedSourceId(upload(userId, topicId, PdfFixtures.withText("JVM executes bytecode"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("PDF"))
                .andExpect(jsonPath("$.originalName").value("notes.pdf")));

        assertThat(api.awaitProcessed(userId, sourceId).get("status").asText()).isEqualTo("READY");
        assertThat(api.getJson(userId, "/api/topics/{id}/content", topicId).get("content").asText())
                .isEqualTo("JVM executes bytecode");
    }

    @Test
    @DisplayName("PDF без текстового слоя получает ошибку TEXT_EXTRACTION_FAILED")
    void uploadPdf_marksError_whenNoTextLayer() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Скан");

        var sourceId = uploadedSourceId(upload(userId, topicId, PdfFixtures.withoutText()));

        var source = api.awaitProcessed(userId, sourceId);
        assertThat(source.get("status").asText()).isEqualTo("ERROR");
        assertThat(source.get("errorCode").asText()).isEqualTo("TEXT_EXTRACTION_FAILED");
    }

    @Test
    @DisplayName("Не-PDF отклоняется с UNSUPPORTED_FILE_TYPE")
    void uploadPdf_rejectsOtherFiles() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Не PDF");

        upload(userId, topicId, "обычный текст".getBytes(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_TYPE"));
    }

    @Test
    @DisplayName("Файл хранится в хранилище и удаляется вместе с источником")
    void deleteSource_removesStoredFile() throws Exception {
        var userId = UUID.randomUUID();
        var topicId = api.createTopic(userId, "Хранилище");
        var sourceId = uploadedSourceId(upload(userId, topicId, PdfFixtures.withText("text")));
        var file = TestStorage.root().resolve(topicId + "/" + sourceId + ".pdf");
        assertThat(file).exists();
        api.awaitProcessed(userId, sourceId);

        mockMvc.perform(delete("/api/sources/{id}", sourceId).with(TestJwt.user(userId)))
                .andExpect(status().isNoContent());

        await().untilAsserted(() -> assertThat(file).doesNotExist());
    }

    /**
     * Загружает файл как PDF-источник.
     *
     * @param userId  владелец
     * @param topicId тема
     * @param content содержимое файла
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions upload(UUID userId, UUID topicId, byte[] content) throws Exception {
        var file = new MockMultipartFile("file", "notes.pdf", "application/pdf", content);
        return mockMvc.perform(multipart("/api/topics/{id}/sources/pdf", topicId).file(file)
                .with(TestJwt.user(userId)));
    }

    /**
     * Достаёт идентификатор источника из ответа 202.
     *
     * @param result результат запроса загрузки
     * @return идентификатор источника
     * @throws Exception при ошибке разбора ответа
     */
    private UUID uploadedSourceId(ResultActions result) throws Exception {
        var body = result.andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(api.objectMapper().readTree(body).get("id").asText());
    }
}
