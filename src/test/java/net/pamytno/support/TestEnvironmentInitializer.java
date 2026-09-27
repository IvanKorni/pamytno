package net.pamytno.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Готовит внешнее окружение для Spring-контекста теста: общий контейнер PostgreSQL,
 * временный каталог файлового хранилища и WireMock вместо YouTube.
 */
public class TestEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Прописывает в окружение параметры подключения к контейнеру, каталог хранилища и адрес WireMock.
     *
     * @param context инициализируемый контекст
     */
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var postgres = PostgresContainer.started();
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "pamytno.topic.storage.root=" + TestStorage.root(),
                "pamytno.topic.youtube.base-url=" + TestWireMock.started().baseUrl()
        ).applyTo(context.getEnvironment());
    }
}
