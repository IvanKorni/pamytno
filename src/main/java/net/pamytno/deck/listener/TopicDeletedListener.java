package net.pamytno.deck.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.deck.service.DeckCleanupService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Удаляет вопросы и карточки темы, когда модуль {@code topic} удалил тему.
 */
@Component("deckTopicDeletedListener")
@RequiredArgsConstructor
public class TopicDeletedListener {

    private final DeckCleanupService cleanupService;

    /**
     * Удаляет данные удалённой темы.
     *
     * @param event событие модуля {@code topic}
     */
    @ApplicationModuleListener
    public void on(TopicDeleted event) {
        cleanupService.deleteTopic(event.topicId());
    }
}
