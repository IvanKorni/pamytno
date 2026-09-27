package net.pamytno.topic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * HTTP-клиент для YouTube с таймаутами из настроек.
 */
@Configuration
public class YoutubeClientConfig {

    /**
     * Клиент YouTube.
     *
     * @param properties настройки YouTube
     * @return RestClient с базовым адресом YouTube
     */
    @Bean
    public RestClient youtubeRestClient(YoutubeProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeout());
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                .build();
    }
}
