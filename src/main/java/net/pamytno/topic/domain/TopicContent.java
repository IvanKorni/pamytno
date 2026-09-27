package net.pamytno.topic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Версия единого текста темы (Master Text). Версии неизменяемы: изменение материалов даёт новую версию.
 */
@Getter
@Entity
@Table(name = "topic_contents", schema = "topic")
public class TopicContent extends BaseEntity {

    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;

    @Column(name = "version", nullable = false, updatable = false)
    private int version;

    @Column(name = "content", nullable = false, updatable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Конструктор для JPA.
     */
    protected TopicContent() {
    }

    /**
     * Создаёт версию текста.
     *
     * @param topicId идентификатор темы
     * @param version номер версии
     * @param content текст
     * @param now     момент создания
     */
    private TopicContent(UUID topicId, int version, String content, Instant now) {
        super(UUID.randomUUID());
        this.topicId = topicId;
        this.version = version;
        this.content = content;
        this.createdAt = now;
    }

    /**
     * Первая версия текста темы.
     *
     * @param topicId идентификатор темы
     * @param content текст
     * @param now     момент создания
     * @return версия 1
     */
    public static TopicContent first(UUID topicId, String content, Instant now) {
        return new TopicContent(topicId, 1, content, now);
    }

    /**
     * Следующая версия текста.
     *
     * @param newContent новый текст
     * @param now        момент создания
     * @return версия с номером на единицу больше
     */
    public TopicContent next(String newContent, Instant now) {
        return new TopicContent(topicId, version + 1, newContent, now);
    }

    /**
     * Проверяет, совпадает ли текст версии с переданным.
     *
     * @param otherContent текст для сравнения
     * @return {@code true}, если тексты равны
     */
    public boolean hasContent(String otherContent) {
        return content.equals(otherContent);
    }
}
