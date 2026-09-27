package net.pamytno.deck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import net.pamytno.common.domain.BaseEntity;
import net.pamytno.deck.exception.QuestionAlreadyHasCardException;

import java.time.Instant;
import java.util.UUID;

/**
 * Вопрос для изучения, предложенный AI по фрагменту единого текста. Хранит цитату-источник,
 * чтобы показать её пользователю, проверить AI и позже перегенерировать карточку.
 */
@Getter
@Entity
@Table(name = "questions", schema = "deck")
public class Question extends BaseEntity {

    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "chunk_id", updatable = false)
    private UUID chunkId;
    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;
    @Column(name = "source_fragment", updatable = false, columnDefinition = "text")
    private String sourceFragment;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Конструктор для JPA.
     */
    protected Question() {
    }

    /**
     * Создаёт вопрос в статусе {@link QuestionStatus#GENERATED}.
     *
     * @param chunk          фрагмент, по которому создан вопрос
     * @param text           текст вопроса
     * @param sourceFragment цитата-источник
     * @param now            момент создания
     */
    public Question(TextChunk chunk, String text, String sourceFragment, Instant now) {
        super(UUID.randomUUID());
        this.topicId = chunk.getTopicId();
        this.userId = chunk.getUserId();
        this.chunkId = chunk.getId();
        this.text = text;
        this.sourceFragment = sourceFragment;
        this.status = QuestionStatus.GENERATED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Пользователь решил изучать вопрос.
     *
     * @param now момент решения
     */
    public void approve(Instant now) {
        changeStatus(QuestionStatus.APPROVED, now);
    }

    /**
     * Пользователь решил не изучать вопрос.
     *
     * @param now момент решения
     */
    public void reject(Instant now) {
        changeStatus(QuestionStatus.REJECTED, now);
    }

    /**
     * Меняет формулировку вопроса.
     *
     * @param newText новый текст
     * @param now     момент изменения
     */
    public void rephrase(String newText, Instant now) {
        requireNoCard();
        this.text = newText;
        this.updatedAt = now;
    }

    /**
     * Отмечает, что по вопросу создана карточка.
     *
     * @param now момент создания карточки
     */
    public void markCardCreated(Instant now) {
        this.status = QuestionStatus.CARD_CREATED;
        this.updatedAt = now;
    }

    /**
     * Меняет статус решения пользователя.
     *
     * @param newStatus новый статус
     * @param now       момент изменения
     */
    private void changeStatus(QuestionStatus newStatus, Instant now) {
        requireNoCard();
        this.status = newStatus;
        this.updatedAt = now;
    }

    /**
     * Запрещает менять вопрос, по которому уже есть карточка.
     */
    private void requireNoCard() {
        if (status == QuestionStatus.CARD_CREATED) {
            throw new QuestionAlreadyHasCardException(getId());
        }
    }
}
