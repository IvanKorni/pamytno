package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.topic.domain.MasterTextBuilder;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.domain.TopicContent;
import net.pamytno.topic.repository.TopicContentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Собирает новую версию единого текста темы, если материалы изменились,
 * и публикует {@link TopicContentPrepared}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicContentService {

    private final TopicContentRepository contentRepository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Пересобирает единый текст. Вызывается под блокировкой темы.
     *
     * @param topic   тема
     * @param sources все источники темы в порядке добавления
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void rebuild(Topic topic, List<Source> sources) {
        var text = MasterTextBuilder.build(sources);
        var latest = contentRepository.findFirstByTopicIdOrderByVersionDesc(topic.getId());
        if (isUnchanged(latest, text)) {
            return;
        }
        var content = contentRepository.save(latest.map(previous -> previous.next(text, clock.instant()))
                .orElseGet(() -> TopicContent.first(topic.getId(), text, clock.instant())));
        events.publishEvent(new TopicContentPrepared(topic.getId(), topic.getUserId(), content.getVersion(), text));
        log.info("Единый текст темы [{}] собран: версия [{}], [{}] символов",
                topic.getId(), content.getVersion(), text.length());
    }

    /**
     * Проверяет, нужна ли новая версия: текст тот же или его ещё не было и он пуст.
     *
     * @param latest последняя версия
     * @param text   новый текст
     * @return {@code true}, если новая версия не нужна
     */
    private static boolean isUnchanged(Optional<TopicContent> latest, String text) {
        return latest.map(content -> content.hasContent(text)).orElse(text.isEmpty());
    }
}
