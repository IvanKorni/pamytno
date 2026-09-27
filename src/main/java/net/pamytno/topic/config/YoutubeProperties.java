package net.pamytno.topic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Настройки получения субтитров YouTube ({@code pamytno.topic.youtube}).
 *
 * @param baseUrl            адрес YouTube; в тестах подменяется на WireMock
 * @param preferredLanguages языки субтитров в порядке предпочтения
 * @param timeout            таймаут одного HTTP-запроса
 * @param clientName         имя клиента innertube API
 * @param clientVersion      версия клиента innertube API
 */
@ConfigurationProperties("pamytno.topic.youtube")
public record YoutubeProperties(
        @DefaultValue("https://www.youtube.com") String baseUrl,
        @DefaultValue({"ru", "en"}) List<String> preferredLanguages,
        @DefaultValue("15s") Duration timeout,
        @DefaultValue("ANDROID") String clientName,
        @DefaultValue("20.10.38") String clientVersion
) {
}
