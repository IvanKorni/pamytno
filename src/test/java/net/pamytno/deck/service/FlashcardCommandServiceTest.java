package net.pamytno.deck.service;

import net.pamytno.common.event.deck.FlashcardDeleted;
import net.pamytno.common.event.deck.FlashcardUpdated;
import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.Flashcard;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.repository.FlashcardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link FlashcardCommandService}.
 */
@ExtendWith(MockitoExtension.class)
class FlashcardCommandServiceTest {

    @Mock
    private FlashcardQueryService flashcardQueryService;
    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private ApplicationEventPublisher events;

    private FlashcardCommandService service;
    private Flashcard card;

    @BeforeEach
    void setUp() {
        service = new FlashcardCommandService(flashcardQueryService, flashcardRepository, events,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var question = new Question(DeckFixtures.chunk(DeckFixtures.topic(), "JVM."), "Что?", "JVM.", Instant.EPOCH);
        card = new Flashcard(question, "Что?", "Ответ.", Instant.EPOCH);
        when(flashcardQueryService.getOwned(card.getId(), card.getUserId())).thenReturn(card);
    }

    @Test
    @DisplayName("Изменение карточки публикует FlashcardUpdated с актуальным текстом")
    void update_publishesFlashcardUpdated() {
        service.update(card.getId(), card.getUserId(), "Что такое JVM?", null);

        verify(events).publishEvent(new FlashcardUpdated(card.getId(), card.getTopicId(), card.getUserId(),
                "Что такое JVM?", "Ответ."));
    }

    @Test
    @DisplayName("Удаление карточки публикует FlashcardDeleted")
    void delete_publishesFlashcardDeleted() {
        service.delete(card.getId(), card.getUserId());

        verify(flashcardRepository).delete(card);
        verify(events).publishEvent(new FlashcardDeleted(card.getId(), card.getTopicId(), card.getUserId()));
    }
}
