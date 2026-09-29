package net.pamytno.deck.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.service.VocabularyGenerationRequested;
import net.pamytno.deck.service.VocabularyGenerationWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Запускает составление карточек слов асинхронно после коммита задачи.
 */
@Component
@RequiredArgsConstructor
public class VocabularyGenerationRequestedListener {

    private final VocabularyGenerationWorker worker;

    /**
     * Выполняет составление карточек слов.
     *
     * @param request запрос на генерацию
     */
    @Async
    @TransactionalEventListener
    public void on(VocabularyGenerationRequested request) {
        worker.generate(request);
    }
}
