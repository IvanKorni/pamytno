package net.pamytno.learning.service;

import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.domain.CardRef;
import net.pamytno.learning.domain.CardSnapshot;
import net.pamytno.learning.repository.CardProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link CardProgressRegistry}.
 */
@ExtendWith(MockitoExtension.class)
class CardProgressRegistryTest {

    @Mock
    private CardProgressRepository progressRepository;

    private CardProgressRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CardProgressRegistry(progressRepository, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Новая карточка получает прогресс")
    void register_createsProgress() {
        var event = new FlashcardCreated(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Вопрос", "Ответ");
        when(progressRepository.findByCardId(event.cardId())).thenReturn(Optional.empty());

        registry.register(event);

        verify(progressRepository).save(any(CardProgress.class));
    }

    @Test
    @DisplayName("Повторная доставка события не создаёт второй прогресс")
    void register_isIdempotent() {
        var event = new FlashcardCreated(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Вопрос", "Ответ");
        var existing = new CardProgress(new CardRef(event.cardId(), event.topicId(), event.userId()),
                new CardSnapshot("Вопрос", "Ответ"), Instant.EPOCH);
        when(progressRepository.findByCardId(event.cardId())).thenReturn(Optional.of(existing));

        registry.register(event);

        verify(progressRepository, never()).save(any());
    }
}
