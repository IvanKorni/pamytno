package net.pamytno.deck.repository;

import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobStatus;
import net.pamytno.deck.domain.GenerationJobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище задач генерации.
 */
public interface GenerationJobRepository extends JpaRepository<GenerationJob, UUID> {

    /**
     * Есть ли у темы задача этого вида в указанном статусе.
     *
     * @param topicId идентификатор темы
     * @param type    вид задачи
     * @param status  статус
     * @return {@code true}, если есть
     */
    boolean existsByTopicIdAndTypeAndStatus(UUID topicId, GenerationJobType type, GenerationJobStatus status);

    /**
     * Задача пользователя.
     *
     * @param id     идентификатор задачи
     * @param userId владелец
     * @return задача, если есть и принадлежит пользователю
     */
    Optional<GenerationJob> findByIdAndUserId(UUID id, UUID userId);
}
