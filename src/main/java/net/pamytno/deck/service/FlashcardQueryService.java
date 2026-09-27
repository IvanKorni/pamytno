package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.Flashcard;
import net.pamytno.deck.exception.CardNotFoundException;
import net.pamytno.deck.repository.FlashcardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Чтение карточек пользователя.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlashcardQueryService {

    private final FlashcardRepository flashcardRepository;

    /**
     * Все карточки темы.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return карточки по порядку создания
     */
    public List<Flashcard> list(UUID topicId, UUID userId) {
        return flashcardRepository.findAllByTopicIdAndUserIdOrderBySeqAsc(topicId, userId);
    }

    /**
     * Карточка пользователя.
     *
     * @param cardId идентификатор карточки
     * @param userId владелец
     * @return карточка
     * @throws CardNotFoundException если карточки нет или она чужая
     */
    public Flashcard getOwned(UUID cardId, UUID userId) {
        return flashcardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }
}
