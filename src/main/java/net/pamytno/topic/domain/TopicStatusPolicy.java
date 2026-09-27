package net.pamytno.topic.domain;

import java.util.Collection;

/**
 * Вычисляет статус темы по статусам её источников.
 *
 * <p>Нет источников — DRAFT; хоть один в обработке — PROCESSING; иначе READY, если есть готовый,
 * и ERROR, если все источники с ошибкой. Ошибка одного источника не ломает тему с готовыми источниками.
 */
public final class TopicStatusPolicy {

    /**
     * Запрещает создание экземпляров.
     */
    private TopicStatusPolicy() {
    }

    /**
     * Статус темы.
     *
     * @param sourceStatuses статусы всех источников темы
     * @return вычисленный статус
     */
    public static TopicStatus resolve(Collection<SourceStatus> sourceStatuses) {
        if (sourceStatuses.isEmpty()) {
            return TopicStatus.DRAFT;
        }
        if (sourceStatuses.stream().anyMatch(SourceStatus::isInProgress)) {
            return TopicStatus.PROCESSING;
        }
        return sourceStatuses.contains(SourceStatus.READY) ? TopicStatus.READY : TopicStatus.ERROR;
    }
}
