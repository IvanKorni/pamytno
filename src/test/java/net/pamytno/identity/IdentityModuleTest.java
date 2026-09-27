package net.pamytno.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.pamytno.support.ModuleTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Модульный тест {@code identity}: регистрация и вход через HTTP с настоящей БД и JWT.
 */
@ModuleTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IdentityModuleTest {

    private static final String PASSWORD = "correct-horse-battery";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Test
    @DisplayName("Регистрация возвращает 201 и Bearer-токен")
    void register_returnsCreatedWithToken() throws Exception {
        register(uniqueEmail(), PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").isNotEmpty());
    }

    @Test
    @DisplayName("Повторная регистрация того же email (в другом регистре) даёт 409")
    void register_returnsConflict_whenEmailTaken() throws Exception {
        var email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        register(email.toUpperCase(), PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    @DisplayName("Короткий пароль и неверный email отклоняются с VALIDATION_FAILED")
    void register_returnsBadRequest_whenInvalid() throws Exception {
        register("not-an-email", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("Вход с верным паролем возвращает токен, с неверным — 401")
    void login_checksPassword() throws Exception {
        var email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        login(email, "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    /**
     * Отправляет запрос регистрации.
     *
     * @param email    email
     * @param password пароль
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions register(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))));
    }

    /**
     * Отправляет запрос входа.
     *
     * @param email    email
     * @param password пароль
     * @return результат запроса
     * @throws Exception при ошибке MockMvc
     */
    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))));
    }

    /**
     * Генерирует уникальный email, чтобы тесты не мешали друг другу в общей БД.
     *
     * @return уникальный email
     */
    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
