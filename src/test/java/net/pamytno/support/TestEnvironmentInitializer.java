package net.pamytno.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Готовит внешнее окружение для Spring-контекста теста: общий контейнер PostgreSQL
 * и временный каталог файлового хранилища.
 */
public class TestEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Прописывает в окружение параметры подключения к контейнеру и каталог хранилища.
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
                "pamytno.topic.storage.root=" + TestStorage.root()
        ).applyTo(context.getEnvironment());
    }
}
