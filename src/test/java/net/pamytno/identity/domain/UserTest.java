package net.pamytno.identity.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты сущности {@link User}.
 */
class UserTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");

    @Test
    @DisplayName("Email нормализуется: без пробелов по краям и в нижнем регистре")
    void normalizeEmail_trimsAndLowercases() {
        assertThat(User.normalizeEmail("  Student@Example.COM ")).isEqualTo("student@example.com");
    }

    @Test
    @DisplayName("Регистрация создаёт нового пользователя с идентификатором и датами")
    void register_createsNewUser() {
        // when
        var user = User.register("Student@Example.com", "hash", NOW);

        // then
        assertThat(user.getId()).isNotNull();
        assertThat(user.isNew()).isTrue();
        assertThat(user.getEmail()).isEqualTo("student@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getCreatedAt()).isEqualTo(NOW);
        assertThat(user.getUpdatedAt()).isEqualTo(NOW);
    }
}
