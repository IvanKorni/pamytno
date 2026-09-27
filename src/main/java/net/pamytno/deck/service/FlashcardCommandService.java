package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.deck.FlashcardDeleted;
import net.pamytno.common.event.deck.FlashcardUpdated;
import net.pamytno.deck.domain.Flashcard;
import net.pamytno.deck.repository.FlashcardRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Редактирование и удаление карточек с уведомлением модуля обучения.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardCommandService {

    private final FlashcardQueryService flashcardQueryService;
    private final FlashcardRepository flashcardRepository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Меняет переданные стороны карточки и публикует {@link FlashcardUpdated}.
     *
     * @param cardId идентификатор карточки
     * @param userId владелец
     * @param front  новый вопрос или {@code null}
     * @param back   новый ответ или {@code null}
     * @return изменённая карточка
     */
    @Transactional
    public Flashcard update(UUID cardId, UUID userId, String front, String back) {
        var card = flashcardQueryService.getOwned(cardId, userId);
        card.edit(front, back, clock.instant());
        events.publishEvent(new FlashcardUpdated(card.getId(), card.getTopicId(), userId, card.getFront(),
                card.getBack()));
        return card;
    }

    /**
     * Удаляет карточку и публикует {@link FlashcardDeleted}.
     *
     * @param cardId идентификатор карточки
     * @param userId владелец
     */
    @Transactional
    public void delete(UUID cardId, UUID userId) {
        var card = flashcardQueryService.getOwned(cardId, userId);
        flashcardRepository.delete(card);
        events.publishEvent(new FlashcardDeleted(card.getId(), card.getTopicId(), userId));
        log.info("Карточка [{}] удалена пользователем [{}]", cardId, userId);
    }
}
