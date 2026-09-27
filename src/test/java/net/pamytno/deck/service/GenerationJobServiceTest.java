package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobStatus;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.exception.GenerationInProgressException;
import net.pamytno.deck.exception.GenerationJobNotFoundException;
import net.pamytno.deck.repository.GenerationJobRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link GenerationJobService}.
 */
@ExtendWith(MockitoExtension.class)
class GenerationJobServiceTest {

    @Mock
    private GenerationJobRepository jobRepository;

    private GenerationJobService service;

    @BeforeEach
    void setUp() {
        service = new GenerationJobService(jobRepository, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Задача запускается, если такая же генерация по теме не идёт")
    void start_savesProcessingJob() {
        var topic = DeckFixtures.topic();
        when(jobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var job = service.start(topic, GenerationJobType.QUESTIONS);

        assertThat(job.getStatus()).isEqualTo(GenerationJobStatus.PROCESSING);
        assertThat(job.topic()).isEqualTo(topic);
    }

    @Test
    @DisplayName("Вторая генерация того же вида по теме отклоняется с GENERATION_IN_PROGRESS")
    void start_throws_whenSameGenerationRunning() {
        var topic = DeckFixtures.topic();
        when(jobRepository.existsByTopicIdAndTypeAndStatus(topic.topicId(), GenerationJobType.CARDS,
                GenerationJobStatus.PROCESSING)).thenReturn(true);

        assertThatThrownBy(() -> service.start(topic, GenerationJobType.CARDS))
                .isInstanceOf(GenerationInProgressException.class);
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("Чужая задача не находится")
    void getOwned_throws_forForeignJob() {
        var jobId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwned(jobId, userId)).isInstanceOf(GenerationJobNotFoundException.class);
    }

    @Test
    @DisplayName("Завершение и ошибка применяются к существующей задаче")
    void completeAndFail_updateJob() {
        var job = new GenerationJob(DeckFixtures.topic(), GenerationJobType.QUESTIONS, Instant.EPOCH);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.complete(job.getId(), 3);
        assertThat(job.getStatus()).isEqualTo(GenerationJobStatus.READY);

        service.fail(job.getId(), 1, "ошибка");
        assertThat(job.getStatus()).isEqualTo(GenerationJobStatus.ERROR);
    }
}
