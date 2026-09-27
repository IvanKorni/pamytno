package net.pamytno.deck.repository;

import jakarta.persistence.LockModeType;
import net.pamytno.deck.domain.TopicMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище проекций единого текста тем.
 */
public interface TopicMaterialRepository extends JpaRepository<TopicMaterial, UUID> {

    /**
     * Проекция темы пользователя.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return проекция, если текст темы уже приходил
     */
    Optional<TopicMaterial> findByIdAndUserId(UUID topicId, UUID userId);

    /**
     * Проекция с блокировкой строки — чтобы параллельные события не записали фрагменты дважды.
     *
     * @param topicId идентификатор темы
     * @return проекция, если есть
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from TopicMaterial m where m.id = :topicId")
    Optional<TopicMaterial> findByIdForUpdate(UUID topicId);
}
