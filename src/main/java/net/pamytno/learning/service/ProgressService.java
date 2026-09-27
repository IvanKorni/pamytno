package net.pamytno.learning.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.learning.domain.Dashboard;
import net.pamytno.learning.domain.ProgressStats;
import net.pamytno.learning.domain.ReviewSchedule;
import net.pamytno.learning.domain.TopicProgress;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.ProgressCounts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Прогресс изучения: по теме и общий. «Сегодня» считается в часовом поясе приложения.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    private final CardProgressRepository progressRepository;
    private final Clock clock;

    /**
     * Прогресс темы; для темы без карточек — нули.
     *
     * @param topicId тема
     * @param userId  пользователь
     * @return прогресс темы
     */
    public TopicProgress topic(UUID topicId, UUID userId) {
        return topics(userId).stream()
                .filter(progress -> progress.topicId().equals(topicId))
                .findFirst()
                .orElse(new TopicProgress(topicId, ProgressStats.EMPTY));
    }

    /**
     * Итоги по всем темам пользователя и прогресс каждой.
     *
     * @param userId пользователь
     * @return dashboard
     */
    public Dashboard dashboard(UUID userId) {
        var topics = topics(userId);
        var totals = topics.stream().map(TopicProgress::stats).reduce(ProgressStats.EMPTY, ProgressStats::plus);
        return new Dashboard(totals, topics);
    }

    /**
     * Прогресс всех тем пользователя, в которых есть карточки.
     *
     * @param userId пользователь
     * @return прогресс тем
     */
    private List<TopicProgress> topics(UUID userId) {
        var now = clock.instant();
        return progressRepository.countByTopic(userId, ReviewSchedule.MASTERED_STAGE, now, endOfToday())
                .stream()
                .map(ProgressService::toTopicProgress)
                .toList();
    }

    /**
     * Начало завтрашнего дня в поясе приложения.
     *
     * @return граница «сегодня»
     */
    private Instant endOfToday() {
        return LocalDate.now(clock).plusDays(1).atStartOfDay(clock.getZone()).toInstant();
    }

    /**
     * Прогресс темы из счётчиков.
     *
     * @param counts счётчики
     * @return прогресс темы
     */
    private static TopicProgress toTopicProgress(ProgressCounts counts) {
        return new TopicProgress(counts.topicId(), ProgressStats.of(Math.toIntExact(counts.total()),
                Math.toIntExact(counts.fresh()), Math.toIntExact(counts.mastered()), Math.toIntExact(counts.due()),
                Math.toIntExact(counts.dueToday())));
    }
}
