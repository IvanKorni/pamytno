package net.pamytno.learning.service;

import net.pamytno.learning.domain.ProgressStats;
import net.pamytno.learning.domain.ReviewSchedule;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.ProgressCounts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link ProgressService}.
 */
@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    /** 22:30 UTC — в Москве уже 01:30 следующего дня. */
    private static final Instant NOW = Instant.parse("2026-09-27T22:30:00Z");
    private static final Instant MOSCOW_END_OF_DAY = Instant.parse("2026-09-28T21:00:00Z");
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private CardProgressRepository progressRepository;

    private ProgressService service;

    @BeforeEach
    void setUp() {
        service = new ProgressService(progressRepository, Clock.fixed(NOW, ZoneId.of("Europe/Moscow")));
    }

    @Test
    @DisplayName("«Сегодня» заканчивается в полночь пояса приложения, а не UTC")
    void dashboard_countsDueTodayInAppZone() {
        var topicId = UUID.randomUUID();
        when(progressRepository.countByTopic(USER_ID, ReviewSchedule.MASTERED_STAGE, NOW, MOSCOW_END_OF_DAY))
                .thenReturn(List.of(new ProgressCounts(topicId, 2L, 1L, 1L, 1L, 1L)));

        var dashboard = service.dashboard(USER_ID);

        assertThat(dashboard.totals()).isEqualTo(new ProgressStats(2, 1, 0, 1, 1, 1, 50.0));
        assertThat(dashboard.topics()).singleElement().extracting("topicId").isEqualTo(topicId);
    }

    @Test
    @DisplayName("Тема без карточек — нулевой прогресс")
    void topic_returnsEmpty_whenNoCards() {
        var topicId = UUID.randomUUID();
        when(progressRepository.countByTopic(USER_ID, ReviewSchedule.MASTERED_STAGE, NOW, MOSCOW_END_OF_DAY))
                .thenReturn(List.of());

        var progress = service.topic(topicId, USER_ID);

        assertThat(progress.topicId()).isEqualTo(topicId);
        assertThat(progress.stats()).isEqualTo(ProgressStats.EMPTY);
    }
}
