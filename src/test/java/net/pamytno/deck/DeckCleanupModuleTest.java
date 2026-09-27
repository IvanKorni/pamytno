package net.pamytno.deck;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.deck.service.FlashcardQueryService;
import net.pamytno.deck.service.QuestionQueryService;
import net.pamytno.deck.service.TopicMaterialService;
import net.pamytno.support.ModuleTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульный тест {@code deck}: удаление темы в модуле {@code topic} удаляет вопросы, карточки и фрагменты.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DeckCleanupModuleTest {

    private final DeckTestApi api;
    private final TopicMaterialService materials;
    private final QuestionQueryService questions;
    private final FlashcardQueryService cards;

    /**
     * Создаёт тест.
     *
     * @param mockMvc      MockMvc
     * @param objectMapper JSON-маппер
     * @param materials    сервис фрагментов
     * @param questions    чтение вопросов
     * @param cards        чтение карточек
     */
    DeckCleanupModuleTest(MockMvc mockMvc, ObjectMapper objectMapper, TopicMaterialService materials,
                          QuestionQueryService questions, FlashcardQueryService cards) {
        this.api = new DeckTestApi(mockMvc, objectMapper, materials);
        this.materials = materials;
        this.questions = questions;
        this.cards = cards;
    }

    @Test
    @DisplayName("TopicDeleted удаляет карточки, вопросы и фрагменты темы")
    void topicDeleted_removesEverythingOfTopic(Scenario scenario) throws Exception {
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        api.prepareCards(scenario, topicId, userId, "JVM выполняет байткод. GC чистит память.");

        scenario.publish(new TopicDeleted(topicId, userId))
                .andWaitForStateChange(() -> cards.list(topicId, userId).isEmpty());

        assertThat(questions.list(topicId, userId, null)).isEmpty();
        assertThat(materials.latestChunks(topicId, userId)).isEmpty();
    }
}
