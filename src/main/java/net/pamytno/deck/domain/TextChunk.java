package net.pamytno.deck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Фрагмент единого текста темы определённой версии. Неизменяем: на него ссылаются вопросы.
 */
@Getter
@Entity
@Table(name = "text_chunks", schema = "deck")
public class TextChunk extends BaseEntity {

    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "content_version", nullable = false, updatable = false)
    private int contentVersion;
    @Column(name = "sequence_number", nullable = false, updatable = false)
    private int sequenceNumber;
    @Column(name = "start_offset", nullable = false, updatable = false)
    private int startOffset;
    @Column(name = "end_offset", nullable = false, updatable = false)
    private int endOffset;
    @Column(name = "content", nullable = false, updatable = false, columnDefinition = "text")
    private String content;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Конструктор для JPA.
     */
    protected TextChunk() {
    }

    /**
     * Создаёт фрагмент.
     *
     * @param topic   тема и владелец
     * @param version версия единого текста
     * @param span    положение и текст фрагмента
     * @param now     момент создания
     */
    public TextChunk(TopicRef topic, int version, ChunkSpan span, Instant now) {
        super(UUID.randomUUID());
        this.topicId = topic.topicId();
        this.userId = topic.userId();
        this.contentVersion = version;
        this.sequenceNumber = span.sequenceNumber();
        this.startOffset = span.startOffset();
        this.endOffset = span.endOffset();
        this.content = span.content();
        this.createdAt = now;
    }
}
