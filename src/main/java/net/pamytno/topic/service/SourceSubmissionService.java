package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.domain.TopicStatus;
import net.pamytno.topic.repository.SourceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Принимает новые источники: сохраняет исходный материал, переводит тему в PROCESSING
 * и отправляет источник на асинхронную обработку.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceSubmissionService {

    private final TopicQueryService topicQueryService;
    private final SourceRepository sourceRepository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Добавляет текст или список слов.
     *
     * @param userId  владелец темы
     * @param topicId идентификатор темы
     * @param type    {@link SourceType#TEXT} или {@link SourceType#WORD_LIST}
     * @param name    название, может быть {@code null}
     * @param text    исходный текст
     * @return сохранённый источник в статусе UPLOADED
     */
    @Transactional
    public Source submitText(UUID userId, UUID topicId, SourceType type, String name, String text) {
        var topic = topicQueryService.getOwned(topicId, userId);
        return submit(topic, Source.text(topic, type, name, text, clock.instant()));
    }

    /**
     * Сохраняет источник и публикует его на обработку.
     *
     * @param topic  тема
     * @param source новый источник
     * @return сохранённый источник
     */
    private Source submit(Topic topic, Source source) {
        sourceRepository.save(source);
        topic.changeStatus(TopicStatus.PROCESSING, source.getCreatedAt());
        events.publishEvent(new SourceSubmitted(source.getId()));
        log.info("Источник [{}] вида [{}] добавлен в тему [{}]", source.getId(), source.getType(), topic.getId());
        return source;
    }
}
