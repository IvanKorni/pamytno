package net.pamytno.deck.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.service.CardGenerationRequested;
import net.pamytno.deck.service.CardGenerationWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Запускает генерацию карточек асинхронно после коммита задачи.
 */
@Component
@RequiredArgsConstructor
public class CardGenerationRequestedListener {

    private final CardGenerationWorker worker;

    /**
     * Выполняет генерацию карточек.
     *
     * @param request запрос на генерацию
     */
    @Async
    @TransactionalEventListener
    public void on(CardGenerationRequested request) {
        worker.generate(request);
    }
}
