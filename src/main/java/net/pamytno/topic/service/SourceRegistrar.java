package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.domain.TopicStatus;
import net.pamytno.topic.repository.SourceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Общий шаг приёма любого источника: сохранить, перевести тему в PROCESSING
 * и отправить источник на асинхронную обработку.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceRegistrar {

    private final SourceRepository sourceRepository;
    private final ApplicationEventPublisher events;

    /**
     * Регистрирует новый источник в транзакции вызывающего сервиса.
     *
     * @param topic  тема
     * @param source новый источник
     * @return сохранённый источник
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Source register(Topic topic, Source source) {
        sourceRepository.save(source);
        topic.changeStatus(TopicStatus.PROCESSING, source.getCreatedAt());
        events.publishEvent(new SourceSubmitted(source.getId()));
        log.info("Источник [{}] вида [{}] добавлен в тему [{}]", source.getId(), source.getType(), topic.getId());
        return source;
    }
}
