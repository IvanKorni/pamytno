package net.pamytno.learning;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.event.deck.FlashcardCreated;
import net.pamytno.common.event.deck.FlashcardDeleted;
import net.pamytno.common.event.deck.FlashcardUpdated;
import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.support.ModuleTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульный тест {@code learning}: прогресс карточек следует за событиями модуля {@code deck}.
 */
@ModuleTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CardProgressModuleTest {

    private final CardProgressRepository progressRepository;

    @Test
    @DisplayName("Карточка появляется в повторении, её текст обновляется и она уходит после удаления")
    void progressFollowsFlashcardEvents(Scenario scenario) {
        var cardId = UUID.randomUUID();
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        scenario.publish(new FlashcardCreated(cardId, topicId, userId, "Что такое JVM?", "Машина."))
                .andWaitForStateChange(() -> progressRepository.findByCardId(cardId));
        var created = progressRepository.findByCardId(cardId).orElseThrow();
        assertThat(created.getStage()).isZero();
        assertThat(created.getNextReviewAt()).isNotNull();

        scenario.publish(new FlashcardUpdated(cardId, topicId, userId, "Что такое JVM?", "Виртуальная машина."))
                .andWaitForStateChange(() -> progressRepository.findByCardId(cardId)
                        .map(CardProgress::getCard)
                        .filter(card -> card.back().equals("Виртуальная машина.")));

        scenario.publish(new FlashcardDeleted(cardId, topicId, userId))
                .andWaitForStateChange(() -> progressRepository.findByCardId(cardId).isEmpty());
    }
}
