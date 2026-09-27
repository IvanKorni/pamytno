package net.pamytno.support;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Единственный на все тесты контейнер PostgreSQL. Запускается один раз при первом обращении
 * и переиспользуется всеми Spring-контекстами модульных и интеграционных тестов.
 */
public final class PostgresContainer {

    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("pamytno")
            .withUsername("pamytno")
            .withPassword("pamytno");

    /**
     * Запрещает создание экземпляров.
     */
    private PostgresContainer() {
    }

    /**
     * Возвращает запущенный контейнер, при необходимости запуская его.
     *
     * @return запущенный контейнер PostgreSQL
     */
    public static synchronized PostgreSQLContainer<?> started() {
        if (!INSTANCE.isRunning()) {
            INSTANCE.start();
        }
        return INSTANCE;
    }
}
