package net.pamytno.learning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.LearningSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Удаляет всё, что модуль накопил по теме: прогресс карточек и учебные сессии.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningCleanupService {

    private final CardProgressRepository progressRepository;
    private final LearningSessionRepository sessionRepository;

    /**
     * Удаляет данные темы. Повторный вызов безопасен.
     *
     * @param topicId идентификатор удалённой темы
     */
    @Transactional
    public void deleteTopic(UUID topicId) {
        var cards = progressRepository.deleteAllByTopic(topicId);
        var sessions = sessionRepository.deleteAllByTopic(topicId);
        log.info("Тема [{}] удалена из обучения: [{}] карточек, [{}] сессий", topicId, cards, sessions);
    }
}
