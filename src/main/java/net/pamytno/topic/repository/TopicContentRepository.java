package net.pamytno.topic.repository;

import net.pamytno.topic.domain.TopicContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище версий единого текста темы.
 */
public interface TopicContentRepository extends JpaRepository<TopicContent, UUID> {

    /**
     * Последняя версия текста темы.
     *
     * @param topicId идентификатор темы
     * @return последняя версия, если текст уже собирался
     */
    Optional<TopicContent> findFirstByTopicIdOrderByVersionDesc(UUID topicId);
}
