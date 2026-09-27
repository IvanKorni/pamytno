package net.pamytno.learning.service;

import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.domain.CardRef;
import net.pamytno.learning.domain.CardSnapshot;
import net.pamytno.learning.domain.LearningSession;
import net.pamytno.learning.domain.ReviewResult;
import net.pamytno.learning.exception.CardProgressNotFoundException;
import net.pamytno.learning.exception.LearningSessionTopicMismatchException;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.LearningSessionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link ReviewService}.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Mock
    private CardProgressRepository progressRepository;
    @Mock
    private LearningSessionRepository sessionRepository;

    private ReviewService service;
    private CardProgress progress;

    @BeforeEach
    void setUp() {
        service = new ReviewService(progressRepository, sessionRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        var ref = new CardRef(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        progress = new CardProgress(ref, new CardSnapshot("Вопрос", "Ответ"), NOW);
    }

    @Test
    @DisplayName("Ответ двигает карточку и учитывается в сессии той же темы")
    void review_updatesProgressAndSession() {
        var session = new LearningSession(progress.getUserId(), progress.getTopicId(), 1, NOW);
        when(progressRepository.findByCardIdAndUserId(progress.getCardId(), progress.getUserId()))
                .thenReturn(Optional.of(progress));
        when(sessionRepository.findByIdAndUserId(session.getId(), progress.getUserId()))
                .thenReturn(Optional.of(session));

        var outcome = service.review(progress.getCardId(), progress.getUserId(), ReviewResult.FORGOT, session.getId());

        assertThat(outcome.returnToSession()).isTrue();
        assertThat(outcome.progress().getTotalForgotten()).isEqualTo(1);
        assertThat(session.getCardsForgotten()).isEqualTo(1);
    }

    @Test
    @DisplayName("Карточка вне повторения пользователя — 404 CARD_PROGRESS_NOT_FOUND")
    void review_throws_whenCardUnknown() {
        var cardId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(progressRepository.findByCardIdAndUserId(cardId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.review(cardId, userId, ReviewResult.REMEMBER, null))
                .isInstanceOf(CardProgressNotFoundException.class);
    }

    @Test
    @DisplayName("Сессия другой темы отклоняется, карточка не меняется")
    void review_throws_whenSessionOfOtherTopic() {
        var session = new LearningSession(progress.getUserId(), UUID.randomUUID(), 1, NOW);
        when(progressRepository.findByCardIdAndUserId(progress.getCardId(), progress.getUserId()))
                .thenReturn(Optional.of(progress));
        when(sessionRepository.findByIdAndUserId(session.getId(), progress.getUserId()))
                .thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.review(progress.getCardId(), progress.getUserId(), ReviewResult.REMEMBER,
                session.getId())).isInstanceOf(LearningSessionTopicMismatchException.class);
        assertThat(progress.getTotalReviews()).isZero();
    }
}
