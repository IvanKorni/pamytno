package net.pamytno.common;

import lombok.RequiredArgsConstructor;
import net.pamytno.support.ModuleTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест общего ядра: безопасность и единый формат ошибок на уровне HTTP.
 */
@ModuleTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CommonModuleTest {

    private final MockMvc mockMvc;

    @Test
    @DisplayName("Запрос без токена получает 401 в едином формате ошибки")
    void protectedPath_returns401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Несуществующий путь с токеном получает 404 в едином формате ошибки")
    void unknownPath_returns404_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/unknown").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Health-check и документация API доступны без токена")
    void publicPaths_areAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
