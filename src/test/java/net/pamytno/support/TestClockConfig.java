package net.pamytno.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Подменяет часы приложения управляемыми {@link TestClock} во всех модульных и интеграционных тестах.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestClockConfig {

    /**
     * Управляемые часы.
     *
     * @return часы, которые тест может перевести вперёд
     */
    @Bean
    @Primary
    public TestClock testClock() {
        return new TestClock();
    }
}
