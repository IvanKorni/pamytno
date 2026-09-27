package net.pamytno.deck;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.deck.FlashcardDeleted;
import net.pamytno.common.event.deck.FlashcardUpdated;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.ModuleTest;
import net.pamytno.support.TestJwt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code deck}: просмотр, редактирование и удаление карточек.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class FlashcardModuleTest {

    private final MockMvc mockMvc;
    private final DeckTestApi api;

    /**
     * Создаёт тест с клиентом API модуля.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     * @param materials    сервис фрагментов
     */
    FlashcardModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials) {
        this.mockMvc = mockMvc;
        this.api = new DeckTestApi(mockMvc, objectMapper, materials);
    }

    @Test
    @DisplayName("Карточки темы видны списком и по id вместе с цитатой-источником")
    void cards_areListedAndFetched(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var cards = api.prepareCards(scenario, topicId, userId, "JVM выполняет байткод. GC чистит память.");
        assertThat(cards).hasSize(2);

        mockMvc.perform(get("/api/cards/{id}", cards.get(0).get("id").asText()).with(TestJwt.user(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topicId").value(topicId.toString()))
                .andExpect(jsonPath("$.sourceFragment").value("JVM выполняет байткод."));
    }

    @Test
    @DisplayName("PATCH меняет ответ карточки и публикует FlashcardUpdated")
    void updateCard_changesBack(Scenario scenario, AssertablePublishedEvents events) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var cardId = api.prepareCards(scenario, topicId, userId, "JVM выполняет байткод.").get(0).get("id").asText();

        mockMvc.perform(patch("/api/cards/{id}", cardId).with(TestJwt.user(userId))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"back\":\"Виртуальная машина Java.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.back").value("Виртуальная машина Java."));

        assertThat(events).contains(FlashcardUpdated.class)
                .matching(event -> event.cardId().toString().equals(cardId))
                .matching(FlashcardUpdated::back, "Виртуальная машина Java.");
    }

    @Test
    @DisplayName("Удалённая карточка публикует FlashcardDeleted и больше не находится")
    void deleteCard_removesCard(Scenario scenario, AssertablePublishedEvents events) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var cardId = api.prepareCards(scenario, topicId, userId, "JVM выполняет байткод.").get(0).get("id").asText();

        mockMvc.perform(delete("/api/cards/{id}", cardId).with(TestJwt.user(userId)))
                .andExpect(status().isNoContent());

        assertThat(events).contains(FlashcardDeleted.class)
                .matching(event -> event.cardId().toString().equals(cardId));
        mockMvc.perform(get("/api/cards/{id}", cardId).with(TestJwt.user(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CARD_NOT_FOUND"));
    }

    @Test
    @DisplayName("Чужую карточку не прочитать и не изменить")
    void cards_areIsolatedBetweenUsers(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var cardId = api.prepareCards(scenario, topicId, UUID.randomUUID(), "Текст.").get(0).get("id").asText();
        var stranger = UUID.randomUUID();

        mockMvc.perform(patch("/api/cards/{id}", cardId).with(TestJwt.user(stranger))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"back\":\"взлом\"}"))
                .andExpect(status().isNotFound());
        assertThat(api.getJson(stranger, "/api/topics/{id}/cards", topicId)).isEmpty();
    }
}
