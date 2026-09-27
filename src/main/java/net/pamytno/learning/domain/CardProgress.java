package net.pamytno.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Состояние изучения одной карточки: этап расписания, дата следующего повторения и счётчики ответов.
 */
@Getter
@Entity
@Table(name = "card_progress", schema = "learning")
public class CardProgress extends BaseEntity {

    @Column(name = "card_id", nullable = false, unique = true, updatable = false)
    private UUID cardId;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Embedded
    private CardSnapshot card;
    @Column(name = "stage", nullable = false)
    private int stage;
    @Column(name = "next_review_at")
    private Instant nextReviewAt;
    @Column(name = "last_review_at")
    private Instant lastReviewAt;
    @Column(name = "consecutive_success", nullable = false)
    private int consecutiveSuccess;
    @Column(name = "total_reviews", nullable = false)
    private int totalReviews;
    @Column(name = "total_remembered", nullable = false)
    private int totalRemembered;
    @Column(name = "total_forgotten", nullable = false)
    private int totalForgotten;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected CardProgress() {
    }

    /**
     * Новая карточка: этап 0, к повторению сразу.
     *
     * @param ref  карточка, её тема и владелец
     * @param card текст карточки
     * @param now  момент создания
     */
    public CardProgress(CardRef ref, CardSnapshot card, Instant now) {
        super(UUID.randomUUID());
        this.cardId = ref.cardId();
        this.topicId = ref.topicId();
        this.userId = ref.userId();
        this.card = card;
        this.nextReviewAt = ReviewSchedule.nextReviewAt(0, now);
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * «Вспомнил»: следующий этап и дата по расписанию.
     *
     * @param now момент ответа
     */
    public void remember(Instant now) {
        stage = ReviewSchedule.afterRemember(stage);
        consecutiveSuccess++;
        totalRemembered++;
        registerReview(now, ReviewSchedule.nextReviewAt(stage, now));
    }

    /**
     * «Не вспомнил»: этап уменьшается, карточка сразу снова к повторению — возвращается в текущую сессию.
     *
     * @param now момент ответа
     */
    public void forget(Instant now) {
        stage = ReviewSchedule.afterForget(stage);
        consecutiveSuccess = 0;
        totalForgotten++;
        registerReview(now, now);
    }

    /**
     * Обновляет копию текста карточки.
     *
     * @param newCard новый текст
     * @param now     момент изменения
     */
    public void updateCard(CardSnapshot newCard, Instant now) {
        this.card = newCard;
        this.updatedAt = now;
    }

    /**
     * Проверяет, изучена ли карточка.
     *
     * @return {@code true} после успешного этапа 5
     */
    public boolean isMastered() {
        return stage >= ReviewSchedule.MASTERED_STAGE;
    }

    /**
     * Фиксирует ответ: счётчик, время ответа и дату следующего повторения.
     *
     * @param now  момент ответа
     * @param next дата следующего повторения или {@code null}
     */
    private void registerReview(Instant now, Instant next) {
        totalReviews++;
        lastReviewAt = now;
        nextReviewAt = next;
        updatedAt = now;
    }
}
