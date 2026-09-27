package net.pamytno.deck.listener;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.service.QuestionGenerationRequested;
import net.pamytno.deck.service.QuestionGenerationWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Запускает генерацию вопросов асинхронно после коммита задачи.
 */
@Component
@RequiredArgsConstructor
public class QuestionGenerationRequestedListener {

    private final QuestionGenerationWorker worker;

    /**
     * Выполняет генерацию вопросов.
     *
     * @param request запрос на генерацию
     */
    @Async
    @TransactionalEventListener
    public void on(QuestionGenerationRequested request) {
        worker.generate(request);
    }
}
