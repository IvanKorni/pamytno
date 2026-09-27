package net.pamytno.integration;

import net.pamytno.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест: приложение целиком стартует, миграции применяются, health-check отвечает UP.
 */
@IntegrationTest
class ApplicationStartupIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Приложение стартует и отвечает UP на health-check")
    void application_startsAndReportsUp() {
        // when
        var body = RestClient.create("http://localhost:" + port)
                .get().uri("/actuator/health")
                .retrieve()
                .body(String.class);

        // then
        assertThat(body).contains("\"status\":\"UP\"");
    }
}
