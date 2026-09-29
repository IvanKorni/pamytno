package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.domain.WordCard;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedWord;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Выполняет задачу составления карточек слов: один вызов модели на весь текст, затем карточка на каждое
 * выражение. Работает вне транзакции — вызов AI долгий. Неполные ответы модели пропускаются.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyGenerationWorker {

    private final AiProvider aiProvider;
    private final FlashcardWriter flashcardWriter;
    private final GenerationJobService jobService;
    private final Clock clock;

    /**
     * Составляет карточки слов и завершает задачу.
     *
     * @param request запрос на генерацию
     */
    public void generate(VocabularyGenerationRequested request) {
        var startedAt = clock.instant();
        log.info("Составление карточек слов [{}] для темы [{}] запущено: текст [{}] символов",
                request.jobId(), request.topicId(), request.text().length());
        try {
            var created = save(new TopicRef(request.topicId(), request.userId()),
                    aiProvider.generateVocabulary(request.instruction(), request.text()));
            jobService.complete(request.jobId(), created);
            log.info("Составление карточек слов [{}] завершено: [{}] карточек за [{}] мс", request.jobId(),
                    created, Duration.between(startedAt, clock.instant()).toMillis());
        } catch (AiGenerationException e) {
            log.warn("Составление карточек слов [{}] прервано ошибкой AI: {}", request.jobId(), e.getMessage());
            jobService.fail(request.jobId(), 0, "AI не смог составить карточки слов: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Непредвиденная ошибка составления карточек слов [{}]", request.jobId(), e);
            jobService.fail(request.jobId(), 0, "Не удалось составить карточки слов");
        }
    }

    /**
     * Сохраняет карточку на каждое полное выражение.
     *
     * @param topic тема и владелец
     * @param words ответ модели
     * @return сколько карточек создано
     */
    private int save(TopicRef topic, List<GeneratedWord> words) {
        var cards = words.stream().map(VocabularyGenerationWorker::toCard).flatMap(Optional::stream).toList();
        if (cards.size() < words.size()) {
            log.warn("Тема [{}]: пропущено [{}] неполных карточек слов", topic.topicId(), words.size() - cards.size());
        }
        cards.forEach(card -> flashcardWriter.createWord(topic, card));
        return cards.size();
    }

    /**
     * Проверяет выражение от модели.
     *
     * @param word ответ модели
     * @return карточка слова или пустое значение, если ответ неполный
     */
    private static Optional<WordCard> toCard(GeneratedWord word) {
        return WordCard.of(word.word(), word.translation(), word.definition(), word.example(),
                word.exampleTranslation());
    }
}
