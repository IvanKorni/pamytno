package net.pamytno.deck.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Клиент Anthropic API. Создаётся только при {@code pamytno.deck.ai.provider=anthropic};
 * ключ берётся из переменной окружения {@code ANTHROPIC_API_KEY} и в настройках приложения не хранится.
 */
@Configuration
@ConditionalOnProperty(prefix = "pamytno.deck.ai", name = "provider", havingValue = "anthropic")
public class AnthropicConfig {

    /**
     * Клиент Anthropic с учётными данными из окружения.
     *
     * @return клиент Anthropic API
     */
    @Bean
    public AnthropicClient anthropicClient() {
        return AnthropicOkHttpClient.fromEnv();
    }
}
