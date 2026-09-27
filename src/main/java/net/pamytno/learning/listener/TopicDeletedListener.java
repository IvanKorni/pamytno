package net.pamytno.learning.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.learning.service.LearningCleanupService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Удаляет прогресс и сессии темы, когда модуль {@code topic} удалил тему.
 * Модуль {@code deck} удаляет карточки темы пачкой, без {@code FlashcardDeleted}, поэтому нужен отдельный приём.
 */
@Component("learningTopicDeletedListener")
@RequiredArgsConstructor
public class TopicDeletedListener {

    private final LearningCleanupService cleanupService;

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
