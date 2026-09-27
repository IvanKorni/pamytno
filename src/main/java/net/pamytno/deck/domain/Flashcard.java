package net.pamytno.deck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Карточка для изучения: вопрос на лицевой стороне, ответ на оборотной.
 * Карточка, составленная AI, всегда редактируема.
 */
@Getter
@Entity
@Table(name = "flashcards", schema = "deck")
public class Flashcard extends BaseEntity {

    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "question_id", updatable = false)
    private UUID questionId;
    @Column(name = "front", nullable = false, columnDefinition = "text")
    private String front;
    @Column(name = "back", nullable = false, columnDefinition = "text")
    private String back;
    @Column(name = "source_fragment", updatable = false, columnDefinition = "text")
    private String sourceFragment;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected Flashcard() {
    }

    /**
     * Создаёт карточку по вопросу; цитата-источник переходит от вопроса.
     *
     * @param question вопрос
     * @param front    лицевая сторона
     * @param back     оборотная сторона
     * @param now      момент создания
     */
    public Flashcard(Question question, String front, String back, Instant now) {
        super(UUID.randomUUID());
        this.topicId = question.getTopicId();
        this.userId = question.getUserId();
        this.questionId = question.getId();
        this.front = front;
        this.back = back;
        this.sourceFragment = question.getSourceFragment();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
