package net.pamytno.topic.domain;

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
 * Тема — набор материалов, объединённых пользователем для изучения (например, «Spring Security»).
 */
@Getter
@Entity
@Table(name = "topics", schema = "topic")
public class Topic extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TopicStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected Topic() {
    }

    /**
     * Создаёт тему в статусе {@link TopicStatus#DRAFT}.
     *
     * @param userId владелец
     * @param title  название
     * @param now    момент создания
     */
    private Topic(UUID userId, String title, Instant now) {
        super(UUID.randomUUID());
        this.userId = userId;
        this.title = title;
        this.status = TopicStatus.DRAFT;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Создаёт новую тему.
     *
     * @param userId      владелец
     * @param title       название
     * @param description описание, может быть пустым
     * @param now         момент создания
     * @return новая тема в статусе DRAFT
     */
    public static Topic create(UUID userId, String title, String description, Instant now) {
        var topic = new Topic(userId, title, now);
        topic.describe(description, now);
        return topic;
    }

    /**
     * Переименовывает тему.
     *
     * @param newTitle новое название
     * @param now      момент изменения
     */
    public void rename(String newTitle, Instant now) {
        this.title = newTitle;
        this.updatedAt = now;
    }

    /**
     * Меняет описание; пустое описание удаляет его.
     *
     * @param newDescription новое описание
     * @param now            момент изменения
     */
    public void describe(String newDescription, Instant now) {
        this.description = newDescription == null || newDescription.isBlank() ? null : newDescription;
        this.updatedAt = now;
    }

    /**
     * Устанавливает статус, вычисленный по источникам.
     *
     * @param newStatus новый статус
     * @param now       момент изменения
     */
    public void changeStatus(TopicStatus newStatus, Instant now) {
        if (status != newStatus) {
            this.status = newStatus;
            this.updatedAt = now;
        }
    }
}
