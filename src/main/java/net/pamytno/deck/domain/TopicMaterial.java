package net.pamytno.deck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Последняя известная модулю {@code deck} версия единого текста темы. Идентификатор равен
 * идентификатору темы. Нужна, чтобы отличить «текста нет» от «текст стал пустым» и знать владельца темы:
 * тема регистрируется при создании с версией {@link #NO_CONTENT}.
 */
@Getter
@Entity
@Table(name = "topic_materials", schema = "deck")
public class TopicMaterial extends BaseEntity {

    /** Версия темы, у которой ещё не было текста: настоящие версии начинаются с 1. */
    public static final int NO_CONTENT = 0;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "content_version", nullable = false)
    private int contentVersion;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected TopicMaterial() {
    }

    /**
     * Создаёт проекцию для темы.
     *
     * @param topic   тема и владелец
     * @param version версия текста
     * @param now     момент получения
     */
    public TopicMaterial(TopicRef topic, int version, Instant now) {
        super(topic.topicId());
        this.userId = topic.userId();
        this.contentVersion = version;
        this.updatedAt = now;
    }

    /**
     * Проверяет, новее ли версия уже известной.
     *
     * @param version версия из события
     * @return {@code true}, если версия больше текущей
     */
    public boolean isOlderThan(int version) {
        return contentVersion < version;
    }

    /**
     * Запоминает новую версию текста.
     *
     * @param version новая версия
     * @param now     момент получения
     */
    public void advanceTo(int version, Instant now) {
        this.contentVersion = version;
        this.updatedAt = now;
    }

    /**
     * Ссылка на тему.
     *
     * @return тема и владелец
     */
    public TopicRef topic() {
        return new TopicRef(getId(), userId);
    }
}
