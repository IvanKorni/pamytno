package net.pamytno.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Регистрирует системные часы. Код получает текущее время только через {@link Clock},
 * чтобы тесты могли подменять время.
 */
@Configuration
@EnableConfigurationProperties(TimeProperties.class)
public class ClockConfig {

    /**
     * Часы приложения в часовом поясе из настроек.
     *
     * @param properties настройки времени
     * @return системные часы
     */
    @Bean
    public Clock clock(TimeProperties properties) {
        return Clock.system(properties.zone());
    }
}
