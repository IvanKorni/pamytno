package net.pamytno.deck.repository;

import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище вопросов.
 */
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    /**
     * Все вопросы темы пользователя по порядку появления.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return вопросы темы
     */
    List<Question> findAllByTopicIdAndUserIdOrderByCreatedAtAscIdAsc(UUID topicId, UUID userId);

    /**
     * Вопросы темы пользователя в статусе по порядку появления.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @param status  статус
     * @return вопросы темы в статусе
     */
    List<Question> findAllByTopicIdAndUserIdAndStatusOrderByCreatedAtAscIdAsc(UUID topicId, UUID userId,
                                                                              QuestionStatus status);

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
}
