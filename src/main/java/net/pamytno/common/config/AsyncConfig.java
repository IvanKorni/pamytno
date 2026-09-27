package net.pamytno.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Включает {@code @Async}: долгие операции (разбор PDF, YouTube, генерация AI) и обработчики
 * событий выполняются в пуле {@code applicationTaskExecutor}, настраиваемом через {@code spring.task.execution}.
 */
@EnableAsync
@Configuration
public class AsyncConfig {
}
