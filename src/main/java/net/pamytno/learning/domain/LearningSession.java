package net.pamytno.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;
import net.pamytno.learning.exception.LearningSessionCompletedException;

import java.time.Instant;
import java.util.UUID;

/**
 * Учебная сессия по теме: сколько карточек было к повторению на старте и сколько из них вспомнили
 * или забыли. Нужна для статистики.
 */
@Getter
@Entity
@Table(name = "learning_sessions", schema = "learning")
public class LearningSession extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "cards_total", nullable = false, updatable = false)
    private int cardsTotal;
    @Column(name = "cards_remembered", nullable = false)
    private int cardsRemembered;
    @Column(name = "cards_forgotten", nullable = false)
    private int cardsForgotten;

    /**
     * Конструктор для JPA.
     */
    protected LearningSession() {
    }

    /**
     * Начинает сессию.
     *
     * @param userId     пользователь
     * @param topicId    тема
     * @param cardsTotal сколько карточек к повторению на старте
     * @param now        момент начала
     */
    public LearningSession(UUID userId, UUID topicId, int cardsTotal, Instant now) {
        super(UUID.randomUUID());
        this.userId = userId;
        this.topicId = topicId;
        this.cardsTotal = cardsTotal;
        this.startedAt = now;
    }

    /**
     * Учитывает ответ по карточке. CONTINUE в статистику не попадает.
     *
     * @param result ответ пользователя
     * @throws LearningSessionCompletedException если сессия уже завершена
     */
    public void register(ReviewResult result) {
        if (isCompleted()) {
            throw new LearningSessionCompletedException(getId());
        }
        if (result == ReviewResult.REMEMBER) {
            cardsRemembered++;
        } else if (result == ReviewResult.FORGOT) {
            cardsForgotten++;
        }
    }

    /**
     * Завершает сессию; повторное завершение ничего не меняет.
     *
     * @param now момент завершения
     */
    public void complete(Instant now) {
        if (!isCompleted()) {
            completedAt = now;
        }
    }

    /**
     * Проверяет, завершена ли сессия.
     *
     * @return {@code true}, если сессия завершена
     */
    public boolean isCompleted() {
        return completedAt != null;
    }
}
