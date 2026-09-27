package net.pamytno.deck.repository;

import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище вопросов.
 */
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    /**
     * Все вопросы темы пользователя в порядке генерации.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return вопросы темы
     */
    List<Question> findAllByTopicIdAndUserIdOrderBySeqAsc(UUID topicId, UUID userId);

    /**
     * Вопросы темы пользователя в статусе в порядке генерации.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @param status  статус
     * @return вопросы темы в статусе
     */
    List<Question> findAllByTopicIdAndUserIdAndStatusOrderBySeqAsc(UUID topicId, UUID userId, QuestionStatus status);

    /**
     * Вопрос пользователя.
     *
     * @param id     идентификатор вопроса
     * @param userId владелец
     * @return вопрос, если есть и принадлежит пользователю
     */
    Optional<Question> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Вопросы пользователя из списка.
     *
     * @param ids    идентификаторы вопросов
     * @param userId владелец
     * @return найденные вопросы пользователя
     */
    List<Question> findAllByIdInAndUserId(Collection<UUID> ids, UUID userId);

    /**
     * Удаляет все вопросы темы.
     *
     * @param topicId идентификатор темы
     * @return сколько записей удалено
     */
    @Modifying
    @Query("delete from Question q where q.topicId = :topicId")
    int deleteAllByTopic(UUID topicId);
}
