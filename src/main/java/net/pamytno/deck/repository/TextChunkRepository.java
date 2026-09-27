package net.pamytno.deck.repository;

import net.pamytno.deck.domain.TextChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Хранилище фрагментов единого текста.
 */
public interface TextChunkRepository extends JpaRepository<TextChunk, UUID> {

    /**
     * Фрагменты версии текста темы по порядку.
     *
     * @param topicId        идентификатор темы
     * @param contentVersion версия текста
     * @return фрагменты версии
     */
    List<TextChunk> findAllByTopicIdAndContentVersionOrderBySequenceNumber(UUID topicId, int contentVersion);

    /**
     * Удаляет все фрагменты темы.
     *
     * @param topicId идентификатор темы
     * @return сколько записей удалено
     */
    @Modifying
    @Query("delete from TextChunk c where c.topicId = :topicId")
    int deleteAllByTopic(UUID topicId);
}
