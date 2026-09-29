package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.topic.TopicCreated;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.repository.TopicRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Создание и редактирование тем. О новой теме сообщает событием {@link TopicCreated}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicCommandService {

    private final TopicRepository topicRepository;
    private final TopicQueryService topicQueryService;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Создаёт тему и публикует {@link TopicCreated}.
     *
     * @param userId      владелец
     * @param title       название
     * @param description описание, может быть {@code null}
     * @return созданная тема
     */
    @Transactional
    public Topic create(UUID userId, String title, String description) {
        var topic = topicRepository.save(Topic.create(userId, title, description, clock.instant()));
        events.publishEvent(new TopicCreated(topic.getId(), userId));
        log.info("Тема [{}] создана пользователем [{}]", topic.getId(), userId);
        return topic;
    }

    /**
     * Меняет переданные поля темы; {@code null} означает «не менять».
     *
     * @param topicId     идентификатор темы
     * @param userId      владелец
     * @param title       новое название или {@code null}
     * @param description новое описание или {@code null}
     * @return обновлённая тема
     */
    @Transactional
    public Topic update(UUID topicId, UUID userId, String title, String description) {
        var topic = topicQueryService.getOwned(topicId, userId);
        var now = clock.instant();
        if (title != null) {
            topic.rename(title, now);
        }
        if (description != null) {
            topic.describe(description, now);
        }
        return topic;
    }
}
