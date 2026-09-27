package net.pamytno.deck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Асинхронная задача генерации вопросов или карточек. Фронтенд опрашивает её статус.
 */
@Getter
@Entity
@Table(name = "generation_jobs", schema = "deck")
public class GenerationJob extends BaseEntity {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 20)
    private GenerationJobType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GenerationJobStatus status;
    @Column(name = "items_created", nullable = false)
    private int itemsCreated;
    @Column(name = "error_message", length = MAX_ERROR_MESSAGE_LENGTH)
    private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Конструктор для JPA.
     */
    protected GenerationJob() {
    }

    /**
     * Запускает задачу в статусе {@link GenerationJobStatus#PROCESSING}.
     *
     * @param topic тема и владелец
     * @param type  что генерируется
     * @param now   момент запуска
     */
    public GenerationJob(TopicRef topic, GenerationJobType type, Instant now) {
        super(UUID.randomUUID());
        this.topicId = topic.topicId();
        this.userId = topic.userId();
        this.type = type;
        this.status = GenerationJobStatus.PROCESSING;
        this.createdAt = now;
    }

    /**
     * Завершает задачу успешно.
     *
     * @param created сколько элементов создано
     * @param now     момент завершения
     */
    public void complete(int created, Instant now) {
        this.status = GenerationJobStatus.READY;
        this.itemsCreated = created;
        this.completedAt = now;
    }

    /**
     * Завершает задачу ошибкой.
     *
     * @param created сколько элементов успели создать
     * @param message описание ошибки для пользователя
     * @param now     момент завершения
     */
    public void fail(int created, String message, Instant now) {
        this.status = GenerationJobStatus.ERROR;
        this.itemsCreated = created;
        this.errorMessage = message == null || message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
        this.completedAt = now;
    }

    /**
     * Ссылка на тему задачи.
     *
     * @return тема и владелец
     */
    public TopicRef topic() {
        return new TopicRef(topicId, userId);
    }
}
