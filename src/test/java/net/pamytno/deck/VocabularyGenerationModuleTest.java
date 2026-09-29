package net.pamytno.deck;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.common.event.topic.TopicCreated;
import net.pamytno.deck.repository.TopicMaterialRepository;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.CapturedEvents;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code deck}: карточки английских слов по вставленному тексту для темы без материалов.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class VocabularyGenerationModuleTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DeckTestApi api;
    private final TopicMaterialRepository topics;
    private final CapturedEvents events;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     * @param materials    сервис фрагментов
     * @param topics       проекция тем модуля
     * @param events       записанные события
     */
    VocabularyGenerationModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials,
                                   TopicMaterialRepository topics, CapturedEvents events) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.api = new DeckTestApi(mockMvc, objectMapper, materials);
        this.topics = topics;
        this.events = events;
    }

    @Test
    @DisplayName("Слова, выделенные жирным, становятся карточками: объяснение и пропуск спереди, слово сзади")
    void generateVocabulary_createsWordCards(Scenario scenario) throws Exception {
        // given
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        registerTopic(scenario, topicId, userId);

        // when
        var jobId = api.idOf(generate(userId, topicId, "The **contract** was **reliable**.")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("VOCABULARY")));

        // then
        assertThat(api.awaitJob(userId, jobId).get("itemsCreated").asInt()).isEqualTo(2);
        var cards = api.getJson(userId, "/api/topics/{id}/cards", topicId);
        assertThat(cards).hasSize(2);
        assertThat(cards.get(0).get("front").asText()).endsWith("I often use _____ in class.");
        assertThat(cards.get(0).get("back").asText()).startsWith("**contract**\nперевод: contract\n\n");
        assertThat(cards.get(0).get("questionId").isNull()).isTrue();
        var event = events.await(FlashcardCreated.class, created -> created.topicId().equals(topicId));
        assertThat(event.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Чужая тема отвечает 404 TOPIC_NOT_FOUND")
    void generateVocabulary_rejectsForeignTopic(Scenario scenario) throws Exception {
        // given
        var topicId = UUID.randomUUID();
        registerTopic(scenario, topicId, UUID.randomUUID());

        // when / then
        generate(UUID.randomUUID(), topicId, "contract")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TOPIC_NOT_FOUND"));
    }

    @Test
    @DisplayName("Пустой текст отклоняется с VALIDATION_FAILED")
    void generateVocabulary_rejectsEmptyText(Scenario scenario) throws Exception {
        // given
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        registerTopic(scenario, topicId, userId);

        // when / then
        generate(userId, topicId, "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /**
     * Сообщает модулю о новой теме, как это сделал бы модуль {@code topic}.
     *
     * @param scenario сценарий Spring Modulith
     * @param topicId  тема
     * @param userId   владелец
     */
    private void registerTopic(Scenario scenario, UUID topicId, UUID userId) {
        scenario.publish(new TopicCreated(topicId, userId))
                .andWaitForStateChange(() -> topics.findByIdAndUserId(topicId, userId), found -> found.isPresent());
    }

    /**
     * Запускает составление карточек слов с инструкцией «слова, выделенные жирным».
     *
     * @param userId  пользователь
     * @param topicId тема
     * @param text    текст
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions generate(UUID userId, UUID topicId, String text) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of("text", text, "instruction", "слова, выделенные жирным"));
        return mockMvc.perform(post("/api/topics/{id}/vocabulary/generate", topicId).with(TestJwt.user(userId))
                .contentType(APPLICATION_JSON).content(body));
    }
}
