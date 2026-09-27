package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.deck.domain.Flashcard;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.integration.ai.GeneratedCard;
import net.pamytno.deck.repository.FlashcardRepository;
import net.pamytno.deck.repository.QuestionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Сохраняет карточку, составленную AI, отмечает вопрос и сообщает модулю обучения.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardWriter {

    private final QuestionRepository questionRepository;
    private final FlashcardRepository flashcardRepository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Создаёт карточку, если вопрос всё ещё одобрен, а ответ модели не пуст.
     *
     * @param questionId идентификатор вопроса
     * @param generated  ответ модели
     * @return {@code true}, если карточка создана
     */
    @Transactional
    public boolean create(UUID questionId, GeneratedCard generated) {
        var question = questionRepository.findById(questionId)
                .filter(found -> found.getStatus() == QuestionStatus.APPROVED);
        if (question.isEmpty() || isBlank(generated.back())) {
            log.warn("Карточка для вопроса [{}] не создана: вопрос изменён или ответ AI пуст", questionId);
            return false;
        }
        var now = clock.instant();
        var front = isBlank(generated.front()) ? question.get().getText() : generated.front().strip();
        var card = flashcardRepository.save(new Flashcard(question.get(), front, generated.back().strip(), now));
        question.get().markCardCreated(now);
        events.publishEvent(new FlashcardCreated(card.getId(), card.getTopicId(), card.getUserId(),
                card.getFront(), card.getBack()));
        return true;
    }

    /**
     * Проверяет строку на пустоту.
     *
     * @param value строка
     * @return {@code true}, если строки нет или она из пробелов
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
