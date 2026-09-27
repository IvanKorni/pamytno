package net.pamytno.learning.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.common.event.deck.FlashcardDeleted;
import net.pamytno.common.event.deck.FlashcardUpdated;
import net.pamytno.learning.service.CardProgressRegistry;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Синхронизирует прогресс повторения с карточками модуля {@code deck}.
 */
@Component
@RequiredArgsConstructor
public class FlashcardEventsListener {

    private final CardProgressRegistry registry;

    /**
     * Новая карточка попадает в повторение.
     *
     * @param event карточка создана
     */
    @ApplicationModuleListener
    public void on(FlashcardCreated event) {
        registry.register(event);
    }

    /**
     * Изменённый текст карточки обновляется в копии.
     *
     * @param event карточка изменена
     */
    @ApplicationModuleListener
    public void on(FlashcardUpdated event) {
        registry.update(event);
    }

    /**
     * Удалённая карточка уходит из повторения.
     *
     * @param event карточка удалена
     */
    @ApplicationModuleListener
    public void on(FlashcardDeleted event) {
        registry.remove(event);
    }
}
