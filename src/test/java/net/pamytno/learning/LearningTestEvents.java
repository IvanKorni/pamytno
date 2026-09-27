package net.pamytno.learning;

import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.learning.repository.CardProgressRepository;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

/**
 * Имитирует модуль {@code deck} в модульных тестах {@code learning}: публикует карточки событиями.
 */
final class LearningTestEvents {

    /**
     * Запрещает создание экземпляров.
     */
    private LearningTestEvents() {
    }

    /**
     * Публикует новую карточку и ждёт, пока модуль заведёт её прогресс.
     *
     * @param scenario   сценарий Spring Modulith
     * @param repository хранилище прогресса
     * @param topicId    тема
     * @param userId     владелец
     * @param front      вопрос карточки
     * @return идентификатор карточки
     */
    static UUID cardCreated(Scenario scenario, CardProgressRepository repository, UUID topicId, UUID userId,
                            String front) {
        var cardId = UUID.randomUUID();
        scenario.publish(new FlashcardCreated(cardId, topicId, userId, front, "Ответ на «" + front + "»"))
                .andWaitForStateChange(() -> repository.findByCardId(cardId));
        return cardId;
    }
}
