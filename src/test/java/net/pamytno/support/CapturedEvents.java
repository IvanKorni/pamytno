package net.pamytno.support;

import org.awaitility.Awaitility;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;

import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Записывает все события приложения из любых потоков. Нужен для асинхронных сценариев:
 * {@code Scenario} и {@code PublishedEvents} Spring Modulith привязаны к потоку теста
 * и теряют события, опубликованные в уже созданных потоках пула {@code @Async}.
 */
public class CapturedEvents implements ApplicationListener<PayloadApplicationEvent<?>> {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final Queue<Object> events = new ConcurrentLinkedQueue<>();

    /**
     * Сохраняет событие.
     *
     * @param event событие Spring с полезной нагрузкой
     */
    @Override
    public void onApplicationEvent(PayloadApplicationEvent<?> event) {
        events.add(event.getPayload());
    }

    /**
     * Ждёт событие нужного типа, удовлетворяющее условию.
     *
     * @param type   тип события
     * @param filter условие
     * @param <T>    тип события
     * @return первое подходящее событие
     */
    public <T> T await(Class<T> type, Predicate<T> filter) {
        return Awaitility.await().atMost(TIMEOUT).until(() -> find(type, filter), Optional::isPresent).orElseThrow();
    }

    /**
     * Ищет уже записанное событие.
     *
     * @param type   тип события
     * @param filter условие
     * @param <T>    тип события
     * @return событие, если оно было опубликовано
     */
    public <T> Optional<T> find(Class<T> type, Predicate<T> filter) {
        return events.stream().filter(type::isInstance).map(type::cast).filter(filter).findFirst();
    }
}
