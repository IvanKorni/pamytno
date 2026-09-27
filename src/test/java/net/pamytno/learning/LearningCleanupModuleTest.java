package net.pamytno.learning;

import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.LearningSessionRepository;
import net.pamytno.learning.service.LearningSessionService;
import net.pamytno.support.ModuleTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульный тест {@code learning}: удаление темы в модуле {@code topic} удаляет прогресс и сессии.
 */
@ModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LearningCleanupModuleTest {

    private final CardProgressRepository progressRepository;
    private final LearningSessionRepository sessionRepository;
    private final LearningSessionService sessionService;

    /**
     * Создаёт тест.
     *
     * @param progressRepository хранилище прогресса
     * @param sessionRepository  хранилище сессий
     * @param sessionService     сервис сессий
     */
    LearningCleanupModuleTest(CardProgressRepository progressRepository, LearningSessionRepository sessionRepository,
                              LearningSessionService sessionService) {
        this.progressRepository = progressRepository;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
    }

    @Test
    @DisplayName("TopicDeleted удаляет прогресс и сессии темы, другие темы не трогает")
    void topicDeleted_removesProgressAndSessionsOfTopic(Scenario scenario) {
        var userId = UUID.randomUUID();
        var topicId = UUID.randomUUID();
        var otherTopicId = UUID.randomUUID();
        var cardId = LearningTestEvents.cardCreated(scenario, progressRepository, topicId, userId, "Вопрос?");
        var otherCardId = LearningTestEvents.cardCreated(scenario, progressRepository, otherTopicId, userId, "Ещё?");
        var session = sessionService.start(userId, topicId);

        scenario.publish(new TopicDeleted(topicId, userId))
                .andWaitForStateChange(() -> progressRepository.findByCardId(cardId).isEmpty());

        assertThat(sessionRepository.findById(session.getId())).isEmpty();
        assertThat(progressRepository.findByCardId(otherCardId)).isPresent();
    }
}
