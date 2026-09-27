package net.pamytno.topic.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.integration.storage.LocalFileStorage;
import net.pamytno.topic.service.StoragePathObsolete;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.UncheckedIOException;

/**
 * Удаляет из хранилища файлы удалённых источников и тем после коммита.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageCleanupListener {

    private final LocalFileStorage storage;

    /**
     * Удаляет ненужный файл или каталог. Ошибка удаления только логируется.
     *
     * @param event событие с путём
     */
    @TransactionalEventListener
    public void on(StoragePathObsolete event) {
        try {
            storage.delete(event.key());
        } catch (UncheckedIOException e) {
            log.error("Не удалось удалить из хранилища [{}]", event.key(), e);
        }
    }
}
