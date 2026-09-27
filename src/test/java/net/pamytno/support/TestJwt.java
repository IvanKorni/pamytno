package net.pamytno.support;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

/**
 * JWT-аутентификация для MockMvc-запросов модульных тестов.
 */
public final class TestJwt {

    /**
     * Запрещает создание экземпляров.
     */
    private TestJwt() {
    }

    /**
     * Запрос от имени пользователя: claim {@code sub} равен его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пост-процессор запроса MockMvc
     */
    public static RequestPostProcessor user(UUID userId) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userId.toString()));
    }
}
