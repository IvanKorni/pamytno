package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Приём текста и списка слов.
 */
@Service
@RequiredArgsConstructor
public class TextSourceSubmissionService {

    private final TopicQueryService topicQueryService;
    private final SourceRegistrar sourceRegistrar;
    private final Clock clock;

    /**
     * Добавляет текст или список слов. Текст сохраняется без изменений.
     *
     * @param userId  владелец темы
     * @param topicId идентификатор темы
     * @param type    {@link SourceType#TEXT} или {@link SourceType#WORD_LIST}
     * @param name    название, может быть {@code null}
     * @param text    исходный текст
     * @return сохранённый источник в статусе UPLOADED
     */
    @Transactional
    public Source submit(UUID userId, UUID topicId, SourceType type, String name, String text) {
        var topic = topicQueryService.getOwned(topicId, userId);
        return sourceRegistrar.register(topic, Source.text(topic, type, name, text, clock.instant()));
    }
}
