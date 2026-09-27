package net.pamytno.learning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.common.event.deck.FlashcardDeleted;
import net.pamytno.common.event.deck.FlashcardUpdated;
import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.domain.CardRef;
import net.pamytno.learning.domain.CardSnapshot;
import net.pamytno.learning.repository.CardProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Держит прогресс в соответствии с карточками модуля {@code deck}. Все операции идемпотентны:
 * события доставляются «как минимум один раз».
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardProgressRegistry {

    private final CardProgressRepository progressRepository;
    private final Clock clock;

    /**
     * Заводит прогресс новой карточки, если его ещё нет.
     *
     * @param event карточка создана
     */
    @Transactional
    public void register(FlashcardCreated event) {
        if (progressRepository.findByCardId(event.cardId()).isPresent()) {
            return;
        }
        var ref = new CardRef(event.cardId(), event.topicId(), event.userId());
        progressRepository.save(new CardProgress(ref, new CardSnapshot(event.front(), event.back()), clock.instant()));
        log.info("Карточка [{}] темы [{}] добавлена в повторение", event.cardId(), event.topicId());
    }

    /**
     * Обновляет копию текста карточки.
     *
     * @param event карточка изменена
     */
    @Transactional
    public void update(FlashcardUpdated event) {
        progressRepository.findByCardId(event.cardId()).ifPresent(progress ->
                progress.updateCard(new CardSnapshot(event.front(), event.back()), clock.instant()));
    }

    /**
     * Удаляет прогресс удалённой карточки.
     *
     * @param event карточка удалена
     */
    @Transactional
    public void remove(FlashcardDeleted event) {
        progressRepository.deleteByCardId(event.cardId());
    }
}
