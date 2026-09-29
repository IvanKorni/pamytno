package net.pamytno.deck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Карточка для изучения: вопрос на лицевой стороне, ответ на оборотной. Составляется по одобренному вопросу
 * или, для слов, прямо по тексту — тогда вопроса нет. Карточка, составленная AI, всегда редактируема.
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
    @Column(name = "seq", insertable = false, updatable = false)
    private Long seq;
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

    /**
     * Создаёт карточку без вопроса — например, карточку слова, составленную прямо по тексту.
     *
     * @param topic тема и владелец
     * @param front лицевая сторона
     * @param back  оборотная сторона
     * @param now   момент создания
     */
    public Flashcard(TopicRef topic, String front, String back, Instant now) {
        super(UUID.randomUUID());
        this.topicId = topic.topicId();
        this.userId = topic.userId();
        this.front = front;
        this.back = back;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Меняет переданные стороны карточки; {@code null} означает «не менять».
     *
     * @param newFront новый вопрос или {@code null}
     * @param newBack  новый ответ или {@code null}
     * @param now      момент изменения
     */
    public void edit(String newFront, String newBack, Instant now) {
        if (newFront != null) {
            this.front = newFront;
        }
        if (newBack != null) {
            this.back = newBack;
        }
        this.updatedAt = now;
    }
}
