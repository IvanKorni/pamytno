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
 * Источник материала темы. Исходный материал ({@code original*}) задаётся при создании и больше
 * не меняется; результат обработки хранится отдельно в {@code extractedText}.
 */
@Getter
@Entity
@Table(name = "sources", schema = "topic")
public class Source extends BaseEntity {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 20)
    private SourceType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SourceStatus status;
    @Column(name = "original_name", updatable = false)
    private String originalName;
    @Column(name = "original_url", updatable = false, length = 2048)
    private String originalUrl;
    @Column(name = "original_text", updatable = false, columnDefinition = "text")
    private String originalText;
    @Column(name = "storage_key", updatable = false, length = 500)
    private String storageKey;
    @Column(name = "extracted_text", columnDefinition = "text")
    private String extractedText;
    @Enumerated(EnumType.STRING)
    @Column(name = "error_code", length = 50)
    private SourceErrorCode errorCode;
    @Column(name = "error_message", length = MAX_ERROR_MESSAGE_LENGTH)
    private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected Source() {
    }

    /**
     * Создаёт источник в статусе {@link SourceStatus#UPLOADED}.
     *
     * @param topic тема
     * @param type  вид источника
     * @param name  название, может быть {@code null}
     * @param now   момент создания
     */
    private Source(Topic topic, SourceType type, String name, Instant now) {
        super(UUID.randomUUID());
        this.topicId = topic.getId();
        this.userId = topic.getUserId();
        this.type = type;
        this.originalName = name;
        this.status = SourceStatus.UPLOADED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Источник из текста или списка слов. Текст сохраняется без изменений.
     *
     * @param topic тема
     * @param type  {@link SourceType#TEXT} или {@link SourceType#WORD_LIST}
     * @param name  название, может быть {@code null}
     * @param text  исходный текст
     * @param now   момент создания
     * @return новый источник
     */
    public static Source text(Topic topic, SourceType type, String name, String text, Instant now) {
        if (!type.isTextual()) {
            throw new IllegalArgumentException("Текстовым может быть только источник TEXT или WORD_LIST");
        }
        var source = new Source(topic, type, name, now);
        source.originalText = text;
        return source;
    }

    /**
     * Начинает обработку.
     *
     * @param now момент начала
     */
    public void startProcessing(Instant now) {
        this.status = SourceStatus.PROCESSING;
        this.updatedAt = now;
    }

    /**
     * Сохраняет извлечённый текст и завершает обработку успешно.
     *
     * @param text извлечённый и очищенный текст
     * @param now  момент завершения
     */
    public void completeExtraction(String text, Instant now) {
        this.extractedText = text;
        this.status = SourceStatus.READY;
        this.errorCode = null;
        this.errorMessage = null;
        this.updatedAt = now;
    }

    /**
     * Завершает обработку ошибкой.
     *
     * @param code    причина
     * @param message описание для пользователя
     * @param now     момент завершения
     */
    public void failExtraction(SourceErrorCode code, String message, Instant now) {
        this.status = SourceStatus.ERROR;
        this.errorCode = code;
        this.errorMessage = message == null || message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
        this.updatedAt = now;
    }

    /**
     * Проверяет, что текст источника готов для единого текста темы.
     *
     * @return {@code true}, если статус READY
     */
    public boolean isReady() {
        return status == SourceStatus.READY;
    }
}
