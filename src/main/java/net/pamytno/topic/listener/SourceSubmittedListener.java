package net.pamytno.topic.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.service.SourceProcessingService;
import net.pamytno.topic.service.SourceSubmitted;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Запускает обработку источника после коммита транзакции, в которой он был сохранён.
 * Без собственной транзакции: извлечение текста может быть долгим.
 */
@Component
@RequiredArgsConstructor
public class SourceSubmittedListener {

    private final SourceProcessingService sourceProcessingService;

    /**
     * Асинхронно обрабатывает источник.
     *
     * @param event событие о новом источнике
     */
    @Async
    @TransactionalEventListener
    public void on(SourceSubmitted event) {
        sourceProcessingService.process(event.sourceId());
    }
}
