package net.pamytno.topic.repository;

import net.pamytno.topic.domain.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище источников.
 */
public interface SourceRepository extends JpaRepository<Source, UUID> {

    /**
     * Источники темы в порядке добавления.
     *
     * @param topicId идентификатор темы
     * @return источники темы
     */
    List<Source> findAllByTopicIdOrderByCreatedAtAsc(UUID topicId);

    /**
     * Источник пользователя.
     *
     * @param id     идентификатор источника
     * @param userId владелец
     * @return источник, если он есть и принадлежит пользователю
     */
    Optional<Source> findByIdAndUserId(UUID id, UUID userId);
}
