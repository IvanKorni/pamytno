package net.pamytno.deck.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.deck.service.TopicMaterialService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Строит фрагменты для генерации вопросов, когда модуль {@code topic} собрал новую версию текста.
 */
@Component
@RequiredArgsConstructor
public class TopicContentPreparedListener {

    private final TopicMaterialService topicMaterialService;

    /**
     * Принимает новую версию единого текста темы.
     *
     * @param event событие модуля {@code topic}
     */
    @ApplicationModuleListener
    public void on(TopicContentPrepared event) {
        topicMaterialService.accept(event);
    }
}
