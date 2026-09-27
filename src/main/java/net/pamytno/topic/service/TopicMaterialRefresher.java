package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.TopicStatusPolicy;
import net.pamytno.topic.repository.SourceRepository;
import net.pamytno.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Приводит тему в соответствие с её источниками: пересчитывает статус.
 * Строка темы блокируется, чтобы параллельные обработки не мешали друг другу.
 */
@Service
@RequiredArgsConstructor
public class TopicMaterialRefresher {

    private final TopicRepository topicRepository;
    private final SourceRepository sourceRepository;
    private final Clock clock;

    /**
     * Пересчитывает состояние темы. Удалённая тема пропускается.
     *
     * @param topicId идентификатор темы
     */
    @Transactional
    public void refresh(UUID topicId) {
        topicRepository.findByIdForUpdate(topicId).ifPresent(topic -> {
            var statuses = sourceRepository.findAllByTopicIdOrderByCreatedAtAsc(topicId).stream()
                    .map(Source::getStatus)
                    .toList();
            topic.changeStatus(TopicStatusPolicy.resolve(statuses), clock.instant());
        });
    }
}
