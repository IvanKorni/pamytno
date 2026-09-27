package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.deck.repository.FlashcardRepository;
import net.pamytno.deck.repository.GenerationJobRepository;
import net.pamytno.deck.repository.QuestionRepository;
import net.pamytno.deck.repository.TextChunkRepository;
import net.pamytno.deck.repository.TopicMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Удаляет всё, что модуль построил по теме: карточки, вопросы, задачи, фрагменты и проекцию текста.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeckCleanupService {

    private final FlashcardRepository flashcardRepository;
    private final QuestionRepository questionRepository;
    private final GenerationJobRepository jobRepository;
    private final TextChunkRepository chunkRepository;
    private final TopicMaterialRepository materialRepository;

    /**
     * Удаляет данные темы. Повторный вызов безопасен.
     *
     * @param topicId идентификатор удалённой темы
     */
    @Transactional
    public void deleteTopic(UUID topicId) {
        var cards = flashcardRepository.deleteAllByTopic(topicId);
        var questions = questionRepository.deleteAllByTopic(topicId);
        jobRepository.deleteAllByTopic(topicId);
        chunkRepository.deleteAllByTopic(topicId);
        materialRepository.deleteById(topicId);
        log.info("Тема [{}] удалена из модуля карточек: [{}] карточек, [{}] вопросов", topicId, cards, questions);
    }
}
