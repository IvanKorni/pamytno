package net.pamytno.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Подключает Spring-контекст теста к общему контейнеру PostgreSQL.
 */
public class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Прописывает в окружение параметры подключения к контейнеру.
     *
     * @param context инициализируемый контекст
     */
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var postgres = PostgresContainer.started();
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword()
        ).applyTo(context.getEnvironment());
    }
}
