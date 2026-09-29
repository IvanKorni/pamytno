package net.pamytno.deck.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.topic.TopicCreated;
import net.pamytno.deck.service.TopicMaterialService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Запоминает владельца новой темы, чтобы по ней можно было составлять карточки ещё до появления текста.
 */
@Component
@RequiredArgsConstructor
public class TopicCreatedListener {

    private final TopicMaterialService topicMaterialService;

    /**
     * Регистрирует тему в проекции модуля.
     *
     * @param event событие модуля {@code topic}
     */
    @ApplicationModuleListener
    public void on(TopicCreated event) {
        topicMaterialService.register(event);
    }
}
